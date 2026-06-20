import { API_BASE_URL } from "./config.js";
import { apiRequest } from "./httpClient.js";

export const registerUser = async (userData) =>
  apiRequest(`${API_BASE_URL}/register`, {
    method: "POST",
    data: userData,
  });

export const loginUser = async (email, password) => {
  const response = await apiRequest(`${API_BASE_URL}/login`, {
    method: "POST",
    data: {
      email,
      password,
    },
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
  });

  return response.data;
};

const authService = {
  registerUser,
  loginUser,
  requestEmailLoginCode,
  verifyEmailLoginCode,
};

export default authService;
