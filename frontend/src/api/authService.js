import axios from "axios";

const apiUrl = "http://localhost:8080";

export const registerUser = async (userData) => {
  try {
    const response = await axios.post(`${apiUrl}/register`, userData);
    return response;
  } catch (error) {
    throw error;
  }
};

export const loginUser = async (email, password) => {
  try {
    const response = await axios.post(`${apiUrl}/login`, {
      email,
      password,
    });

    return response.data;
  } catch (error) {
    throw error;
  }
};

const authService = {
  registerUser,
  loginUser,
};

export default authService;
