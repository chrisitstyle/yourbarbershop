import { API_BASE_URL } from "./config.js";

let accessToken = null;
let refreshPromise = null;

export const setAccessToken = (token) => {
  accessToken = token;
};

export const clearAccessToken = () => {
  accessToken = null;
};

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

const getErrorMessage = (response, data) => {
  if (typeof data === "string" && data.trim()) {
    return data;
  }

  if (data && typeof data === "object") {
    return data.message || data.error || response.statusText;
  }

  return response.statusText || `HTTP error ${response.status}`;
};

export class ApiError extends Error {
  constructor(response, data) {
    super(getErrorMessage(response, data));
    this.name = "ApiError";
    this.status = response.status;
    this.response = {
      data,
      status: response.status,
      statusText: response.statusText,
      headers: Object.fromEntries(response.headers.entries()),
    };
  }
}

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

const rawApiRequest = async (
  url,
  { data, headers = {}, skipAuthRefresh = false, ...options } = {},
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

export const getAuthorizationHeaders = () => {
  if (!accessToken) {
    return {};
  }

  return {
    Authorization: `Bearer ${accessToken}`,
  };
};
