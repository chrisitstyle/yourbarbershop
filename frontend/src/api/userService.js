import { API_BASE_URL } from "./config.js";
import { apiRequest } from "./httpClient.js";

export const addUser = async (newUser) => {
  try {
    await apiRequest(`${API_BASE_URL}/users`, {
      method: "POST",
      data: newUser,
    });
  } catch (error) {
    console.error("Error adding user:", error);
    throw error;
  }
};

export const getCurrentUser = async () => {
  const response = await apiRequest(`${API_BASE_URL}/users/me`, {
    method: "GET",
  });

  return response.data;
};

export const getUsers = async () => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/users`, {
      credentials: "include",
    });

    return response.data;
  } catch (error) {
    console.error("Error loading users:", error);
    throw error;
  }
};

export const updateUser = async (userId, newData) => {
  const response = await apiRequest(`${API_BASE_URL}/users/${userId}`, {
    method: "PUT",
    credentials: "include",
    data: newData,
  });

  return response.data;
};

export const deleteUser = async (idUser) => {
  try {
    await apiRequest(`${API_BASE_URL}/users/${idUser}`, {
      method: "DELETE",
      credentials: "include",
    });
  } catch (error) {
    console.error("Error deleting user:", error);
    throw error;
  }
};

export const userForgotPasswordRequest = async (email, captchaToken) => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/forgot-password`, {
      method: "POST",
      data: {
        email,
        captchaToken,
      },
    });

    return response.data;
  } catch (error) {
    console.error("Error sending forgot password request:", error);

    throw error;
  }
};

export const userResetPasswordRequest = async (
  token,
  newPassword,
  confirmPassword,
) => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/reset-password`, {
      method: "POST",
      data: {
        token,
        newPassword,
        confirmPassword,
      },
    });

    return response.data;
  } catch (error) {
    console.error("Error resetting password:", error);

    throw error;
  }
};

const userService = {
  addUser,
  getUsers,
  getCurrentUser,
  updateUser,
  deleteUser,
  userForgotPasswordRequest,
  userResetPasswordRequest,
};

export default userService;
