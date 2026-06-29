import { API_BASE_URL } from "./config.js";

let accessToken = null;
let refreshPromise = null;

/**
 * Stores the current JWT access token in memory.
 *
 * The access token is intentionally not persisted in localStorage or sessionStorage.
 * It is kept only in memory and restored by calling the refresh endpoint when
 * the application starts.
 *
 * @param {string | null} token - JWT access token returned by the backend.
 */
export const setAccessToken = (token) => {
  accessToken = token;
};

/**
 * Clears the in-memory access token.
 *
 * Should be called after logout, failed refresh, or when the session
 * should be treated as unauthenticated.
 */
export const clearAccessToken = () => {
  accessToken = null;
};

/**
 * Reads and processes the HTTP response body.
 *
 * If the response has no body, it returns `null`.
 * Responses with the `Content-Type: application/json` header are parsed
 * into JavaScript values. If JSON parsing fails, the raw response text
 * is returned instead.
 *
 * @param {Response} response - Response returned by the Fetch API.
 * @returns {Promise<unknown>} Parsed response data, raw text, or `null`.
 */
const parseResponseData = async (response) => {
  const responseText = await response.text();

  if (!responseText) {
    return null;
  }

  const contentType = response.headers.get("content-type") || "";

  if (contentType.includes("application/json")) {
    try {
      return JSON.parse(responseText);
    } catch {
      return responseText;
    }
  }

  return responseText;
};

/**
 * Resolves an error message based on the HTTP response
 * and the data returned by the backend.
 *
 * Message priority:
 * 1. Plain text returned by the backend.
 * 2. The `message` property from the response object.
 * 3. The `error` property from the response object.
 * 4. The HTTP status text.
 * 5. A fallback message containing the status code.
 *
 * @param {Response} response - HTTP response with an error status.
 * @param {unknown} data - Parsed response data returned by the backend.
 * @returns {string} Resolved error message.
 */
const getErrorMessage = (response, data) => {
  if (typeof data === "string" && data.trim()) {
    return data;
  }

  if (data && typeof data === "object") {
    return data.message || data.error || response.statusText;
  }

  return response.statusText || `HTTP error ${response.status}`;
};

/**
 * Represents an unsuccessful HTTP response.
 *
 * The class preserves an Axios-like error structure so existing code
 * can continue reading backend error data through:
 *
 * `error.response.data`
 *
 * @extends Error
 */
export class ApiError extends Error {
  /**
   * Creates an API error from an HTTP response.
   *
   * @param {Response} response - HTTP response with a non-success status.
   * @param {unknown} data - Error data returned by the backend.
   */
  constructor(response, data) {
    super(getErrorMessage(response, data));

    this.name = "ApiError";
    this.status = response.status;

    /**
     * Details of the failed HTTP response.
     *
     * The structure is compatible with the error handling previously
     * used with Axios.
     *
     * @type {{
     *   data: unknown,
     *   status: number,
     *   statusText: string,
     *   headers: Object.<string, string>
     * }}
     */
    this.response = {
      data,
      status: response.status,
      statusText: response.statusText,
      headers: Object.fromEntries(response.headers.entries()),
    };
  }
}

/**
 * Refreshes the access token using the HttpOnly refresh token cookie.
 *
 * Only one refresh request is allowed at a time. If multiple API calls receive
 * a 401 response at once, they reuse the same refresh promise instead of
 * sending multiple refresh requests.
 *
 * The refresh token itself is not available in JavaScript. It is sent
 * automatically by the browser as an HttpOnly cookie.
 *
 * @returns {Promise<unknown>} Auth response containing a new `accessToken`.
 * @throws {ApiError} When the refresh endpoint returns an error response.
 * @throws {Error} When the refresh response does not contain `accessToken`.
 */
export const refreshAccessToken = async () => {
  if (!refreshPromise) {
    refreshPromise = rawApiRequest(`${API_BASE_URL}/auth/refresh`, {
      method: "POST",
      skipAuthRefresh: true,
    })
      .then((response) => {
        const newAccessToken = response.data?.accessToken;

        if (!newAccessToken) {
          throw new Error("Refresh response does not contain accessToken");
        }

        setAccessToken(newAccessToken);
        return response.data;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
};

/**
 * Performs a raw HTTP request using the native Fetch API.
 *
 * The function:
 * - automatically serializes `data` to JSON,
 * - sets the `Content-Type: application/json` header when needed,
 * - adds the in-memory access token as a Bearer token when available,
 * - always sends cookies using `credentials: "include"`,
 * - parses JSON and text responses,
 * - handles empty responses such as `204 No Content`,
 * - throws `ApiError` for responses outside the 200-299 range.
 *
 * This function does not retry failed requests and does not refresh
 * the access token. Use `apiRequest` for normal application requests.
 *
 * @param {string | URL | Request} url - Resource URL.
 * @param {RequestInit & {
 *   data?: unknown,
 *   headers?: HeadersInit,
 *   skipAuthRefresh?: boolean
 * }} [config={}] - Request configuration.
 * @returns {Promise<{
 *   data: unknown,
 *   status: number,
 *   statusText: string,
 *   headers: Object.<string, string>
 * }>} HTTP response details with parsed response data.
 *
 * @throws {ApiError} When the backend returns a status outside 200-299.
 * @throws {TypeError} When a network error occurs or the URL is invalid.
 * @throws {DOMException} When the request is aborted using AbortController.
 */
const rawApiRequest = async (
  url,
  {
    data,
    headers = {},
    skipAuthRefresh: _skipAuthRefresh = false,
    ...options
  } = {},
) => {
  const requestHeaders = new Headers(headers);
  const hasData = data !== undefined;

  if (hasData && !requestHeaders.has("Content-Type")) {
    requestHeaders.set("Content-Type", "application/json");
  }

  if (accessToken && !requestHeaders.has("Authorization")) {
    requestHeaders.set("Authorization", `Bearer ${accessToken}`);
  }

  const response = await fetch(url, {
    ...options,
    credentials: "include",
    headers: requestHeaders,
    body: hasData ? JSON.stringify(data) : undefined,
  });

  const responseData = await parseResponseData(response);

  if (!response.ok) {
    throw new ApiError(response, responseData);
  }

  return {
    data: responseData,
    status: response.status,
    statusText: response.statusText,
    headers: Object.fromEntries(response.headers.entries()),
  };
};

/**
 * Performs an authenticated API request.
 *
 * The function:
 * - sends cookies automatically using `credentials: "include"`,
 * - adds the in-memory access token as a Bearer token when available,
 * - refreshes the access token once when the request fails with `401`,
 * - retries the original request after a successful refresh,
 * - avoids refresh attempts for login, register, and refresh requests.
 *
 * @example
 * const response = await apiRequest("/api/offers");
 * console.log(response.data);
 *
 * @example
 * const response = await apiRequest("/api/offers", {
 *   method: "POST",
 *   data: {
 *     kind: "Haircut",
 *     cost: 50,
 *   },
 * });
 *
 * @param {string | URL | Request} url - Resource URL.
 * @param {RequestInit & {
 *   data?: unknown,
 *   headers?: HeadersInit,
 *   skipAuthRefresh?: boolean
 * }} [config={}] - Request configuration.
 * @returns {Promise<{
 *   data: unknown,
 *   status: number,
 *   statusText: string,
 *   headers: Object.<string, string>
 * }>} HTTP response details with parsed response data.
 *
 * @throws {ApiError} When the backend returns a status outside 200-299.
 * @throws {TypeError} When a network error occurs or the URL is invalid.
 * @throws {DOMException} When the request is aborted using AbortController.
 */
export const apiRequest = async (url, config = {}) => {
  try {
    return await rawApiRequest(url, config);
  } catch (error) {
    const shouldTryRefresh =
      error instanceof ApiError &&
      error.status === 401 &&
      !config.skipAuthRefresh &&
      !String(url).includes("/auth/refresh") &&
      !String(url).includes("/login") &&
      !String(url).includes("/register");

    if (!shouldTryRefresh) {
      throw error;
    }

    await refreshAccessToken();

    return rawApiRequest(url, config);
  }
};

/**
 * Creates an Authorization header for the current in-memory access token.
 *
 * Prefer using `apiRequest` for normal API calls, because it automatically
 * attaches the access token and handles refresh on `401`.
 *
 * This helper is kept for places that still need to pass raw headers manually.
 *
 * @example
 * const headers = getAuthorizationHeaders();
 *
 * await apiRequest("/api/users", {
 *   headers,
 * });
 *
 * @returns {Record<string, string>} Bearer authorization header or an empty object.
 */
export const getAuthorizationHeaders = () => {
  if (!accessToken) {
    return {};
  }

  return {
    Authorization: `Bearer ${accessToken}`,
  };
};
