import { API_BASE_URL } from "./config.js";
import { apiRequest, refreshAccessToken } from "./httpClient.js";

export const registerUser = async (userData) => {
  const response = await apiRequest(`${API_BASE_URL}/register`, {
    method: "POST",
    data: userData,
    skipAuthRefresh: true,
  });

  return response.data;
};

export const loginUser = async (email, password) => {
  const response = await apiRequest(`${API_BASE_URL}/login`, {
    method: "POST",
    data: {
      email,
      password,
    },
    skipAuthRefresh: true,
  });

  return response.data;
};

export const requestEmailLoginCode = async (email) => {
  const response = await apiRequest(
    `${API_BASE_URL}/login/email-code/request`,
    {
      method: "POST",
      data: {
        email,
      },
      skipAuthRefresh: true,
    },
  );

  return response.data;
};

export const verifyEmailLoginCode = async (email, code) => {
  const response = await apiRequest(`${API_BASE_URL}/login/email-code/verify`, {
    method: "POST",
    data: {
      email,
      code,
    },
    skipAuthRefresh: true,
  });

  return response.data;
};

export const refreshSession = async () => {
  return refreshAccessToken();
};

export const logoutUser = async () => {
  await apiRequest(`${API_BASE_URL}/auth/logout`, {
    method: "POST",
    skipAuthRefresh: true,
  });
};

const authService = {
  registerUser,
  loginUser,
  requestEmailLoginCode,
  verifyEmailLoginCode,
  refreshSession,
  logoutUser,
};

export default authService;
