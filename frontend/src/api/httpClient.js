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
 * Performs an HTTP request using the native Fetch API.
 *
 * The function:
 * - automatically serializes `data` to JSON,
 * - sets the `Content-Type: application/json` header,
 * - parses JSON and text responses,
 * - handles empty responses such as `204 No Content`,
 * - throws `ApiError` for responses outside the 200-299 range.
 *
 * Standard Fetch API options such as `method`, `credentials`, `signal`,
 * and other request settings can be passed directly in the second argument.
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
 *   headers?: HeadersInit
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
export const apiRequest = async (
  url,
  { data, headers = {}, ...options } = {},
) => {
  const requestHeaders = new Headers(headers);
  const hasData = data !== undefined;

  if (hasData && !requestHeaders.has("Content-Type")) {
    requestHeaders.set("Content-Type", "application/json");
  }

  const response = await fetch(url, {
    ...options,
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
 * Creates an Authorization header for a JWT access token.
 *
 * @example
 * const headers = getAuthorizationHeaders(userToken);
 *
 * await apiRequest("/api/users", {
 *   headers,
 * });
 *
 * @param {string} token - User JWT access token.
 * @returns {{Authorization: string}} Bearer authorization header.
 */
export const getAuthorizationHeaders = (token) => ({
  Authorization: `Bearer ${token}`,
});
