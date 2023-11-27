// api.js
import axios from "axios";

const apiUrl = "http://localhost:8080";

export const getOffers = async () => {
  try {
    const result = await axios.get(`${apiUrl}/offers/get`);
    return result.data;
  } catch (error) {
    console.error("Error loading offers:", error);
    throw error;
  }
};

export const addOffer = async (newOffer, userToken) => {
  try {
    await axios.post(`${apiUrl}/offers/add`, newOffer, {
      withCredentials: true,
      headers: {
        Authorization: `Bearer ${userToken}`,
      },
    });
  } catch (error) {
    console.error("Error adding offer:", error);
    throw error;
  }
};

export const deleteOffer = async (idOffer, userToken) => {
  try {
    await axios.delete(`${apiUrl}/offers/delete/${idOffer}`, {
      withCredentials: true,
      headers: {
        Authorization: `Bearer ${userToken}`,
      },
    });
  } catch (error) {
    console.error("Error deleting offer:", error);
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

export const addUser = async (newUser) => {
  try {
    await axios.post(`${apiUrl}/register`, newUser);
  } catch (error) {
    console.error("Error adding user:", error);
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

export const getOrders = async (userToken) => {
  try {
    const result = await axios.get(`${apiUrl}/orders/get`, {
      withCredentials: true,
      headers: {
        Authorization: `Bearer ${userToken}`,
      },
    });
    return result.data;
  } catch (error) {
    console.error("Error loading orders:", error);
    throw error;
  }
};

export const deleteOrder = async (idOrder, userToken) => {
  try {
    await axios.delete(`${apiUrl}/orders/delete/${idOrder}`, {
      withCredentials: true,
      headers: {
        Authorization: `Bearer ${userToken}`,
      },
    });
  } catch (error) {
    console.error("Error deleting order:", error);
    throw error;
  }
};
