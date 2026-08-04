import { API_BASE_URL } from "./config.js";
import { apiRequest } from "./httpClient.js";

export const createOrder = async (orderCreationData, idempotencyKey) => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/orders`, {
      method: "POST",
      credentials: "include",
      headers: {
        "Idempotency-Key": idempotencyKey,
      },
      data: orderCreationData,
    });

    return response.data;
  } catch (error) {
    console.error("Error creating order:", error);
    throw error;
  }
};

export const getOrders = async () => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/orders`, {
      credentials: "include",
    });

    return response.data;
  } catch (error) {
    console.error("Error loading orders:", error);
    throw error;
  }
};

export const updateOrder = async (orderId, newData) => {
  const response = await apiRequest(`${API_BASE_URL}/orders/${orderId}`, {
    method: "PUT",
    credentials: "include",
    data: newData,
  });

  return response.data;
};

export const deleteOrder = async (idOrder) => {
  try {
    await apiRequest(`${API_BASE_URL}/orders/${idOrder}`, {
      method: "DELETE",
      credentials: "include",
    });
  } catch (error) {
    console.error("Error deleting order:", error);
    throw error;
  }
};

const orderService = {
  createOrder,
  getOrders,
  updateOrder,
  deleteOrder,
};

export default orderService;
