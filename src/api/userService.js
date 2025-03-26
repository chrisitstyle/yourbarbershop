import axios from "axios";

const apiUrl = "http://localhost:8080";

export const addUser = async (newUser) => {
  try {
    await axios.post(`${apiUrl}/register`, newUser);
  } catch (error) {
    console.error("Error adding user:", error);
    throw error;
  }
};

export const getUsers = async (userToken) => {
  try {
    const result = await axios.get(`${apiUrl}/users/get`, {
      withCredentials: true,
      headers: {
        Authorization: `Bearer ${userToken}`,
      },
    });
    return result.data;
  } catch (error) {
    console.error("Error loading users: ", error);
    throw error;
  }
};

export const updateUser = async (userId, newData, token) => {
  try {
    const response = await axios.put(
      `http://localhost:8080/users/update/${userId}`,
      newData,
      {
        withCredentials: true,
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    return response;
  } catch (error) {
    throw error;
  }
};

export const deleteUser = async (idUser, userToken) => {
  try {
    await axios.delete(`${apiUrl}/users/delete/${idUser}`, {
      withCredentials: true,
      headers: {
        Authorization: `Bearer ${userToken}`,
      },
    });
  } catch (error) {
    console.error("Error deleting user:", error);
    throw error;
  }
};

export const userForgotPasswordRequest = async (email) => {
  try {
    const response = await axios.post(`${apiUrl}/forgot-password`, { email });
    return response.data;
  } catch (error) {
    console.error("Error sending forgot password request:", error);
    throw error;
  }
};

export const userResetPasswordRequest = async (token, newPassword) => {
  try {
    const response = await axios.post(`${apiUrl}/reset-password`, {
      token,
      newPassword,
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
