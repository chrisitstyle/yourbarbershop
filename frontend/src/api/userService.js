import { API_BASE_URL } from "./config.js";
import { apiRequest, getAuthorizationHeaders } from "./httpClient.js";

export const addUser = async (newUser) => {
  try {
    await apiRequest(`${API_BASE_URL}/register`, {
      method: "POST",
      data: newUser,
    });
  } catch (error) {
    console.error("Error adding user:", error);
    throw error;
  }
};

export const getUsers = async (userToken) => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/users`, {
      credentials: "include",
      headers: getAuthorizationHeaders(userToken),
    });

    return response.data;
  } catch (error) {
    console.error("Error loading users:", error);
    throw error;
  }
};

export const updateUser = async (userId, newData, userToken) => {
  const response = await apiRequest(`${API_BASE_URL}/users/${userId}`, {
    method: "PUT",
    credentials: "include",
    headers: getAuthorizationHeaders(userToken),
    data: newData,
  });

  return response.data;
};

export const deleteUser = async (idUser, userToken) => {
  try {
    await apiRequest(`${API_BASE_URL}/users/${idUser}`, {
      method: "DELETE",
      credentials: "include",
      headers: getAuthorizationHeaders(userToken),
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

export const userResetPasswordRequest = async (token, newPassword) => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/reset-password`, {
      method: "POST",
      data: {
        token,
        newPassword,
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
  updateUser,
  deleteUser,
  userForgotPasswordRequest,
  userResetPasswordRequest,
};

export default userService;
