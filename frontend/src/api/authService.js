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

const authService = {
  registerUser,
  loginUser,
};

export default authService;
