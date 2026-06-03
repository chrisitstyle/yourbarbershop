import axios from "axios";
import { API_BASE_URL } from "./config.js";

export const createOrder = async (orderCreationData, userToken) => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/orders`,
      orderCreationData,
      {
        withCredentials: true,
        headers: {
          Authorization: `Bearer ${userToken}`,
        },
      },
    );

    return response.data;
  } catch (error) {
    console.error("Error creating order:", error);
    throw error;
  }
};

export const getOrders = async (userToken) => {
  try {
    const result = await axios.get(`${API_BASE_URL}/orders`, {
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

export const updateOrder = async (orderId, newData, token) => {
  try {
    const response = await axios.put(
      `${API_BASE_URL}/orders/${orderId}`,
      newData,
      {
        withCredentials: true,
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    );

    return response.data;
  } catch (error) {
    throw error;
  }
};

export const deleteOrder = async (idOrder, userToken) => {
  try {
    await axios.delete(`${API_BASE_URL}/orders/${idOrder}`, {
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

const orderService = {
  getOrders,
  updateOrder,
  deleteOrder,
  createOrder,
};

export default orderService;
