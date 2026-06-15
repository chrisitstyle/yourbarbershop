import { API_BASE_URL } from "./config.js";
import { apiRequest, getAuthorizationHeaders } from "./httpClient.js";

export const createOrder = async (orderCreationData, userToken) => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/orders`, {
      method: "POST",
      credentials: "include",
      headers: getAuthorizationHeaders(userToken),
      data: orderCreationData,
    });

    return response.data;
  } catch (error) {
    console.error("Error creating order:", error);
    throw error;
  }
};

export const getOrders = async (userToken) => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/orders`, {
      credentials: "include",
      headers: getAuthorizationHeaders(userToken),
    });

    return response.data;
  } catch (error) {
    console.error("Error loading orders:", error);
    throw error;
  }
};

export const updateOrder = async (orderId, newData, userToken) => {
  const response = await apiRequest(`${API_BASE_URL}/orders/${orderId}`, {
    method: "PUT",
    credentials: "include",
    headers: getAuthorizationHeaders(userToken),
    data: newData,
  });

  return response.data;
};

export const deleteOrder = async (idOrder, userToken) => {
  try {
    await apiRequest(`${API_BASE_URL}/orders/${idOrder}`, {
      method: "DELETE",
      credentials: "include",
      headers: getAuthorizationHeaders(userToken),
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
