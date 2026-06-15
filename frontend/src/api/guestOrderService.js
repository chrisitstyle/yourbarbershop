import { API_BASE_URL } from "./config.js";
import { apiRequest, getAuthorizationHeaders } from "./httpClient.js";

export const createGuestOrder = async (guestOrderCreationData) => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/guestorders`, {
      method: "POST",
      data: guestOrderCreationData,
    });

    return response.data;
  } catch (error) {
    console.error("Error creating guest order:", error);
    throw error;
  }
};

export const getGuestOrders = async (userToken) => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/guestorders`, {
      credentials: "include",
      headers: getAuthorizationHeaders(userToken),
    });

    return response.data;
  } catch (error) {
    console.error("Error loading guest orders:", error);
    throw error;
  }
};

export const updateGuestOrder = async (idGuestOrder, data, userToken) => {
  try {
    const response = await apiRequest(
      `${API_BASE_URL}/guestorders/${idGuestOrder}`,
      {
        method: "PUT",
        credentials: "include",
        headers: getAuthorizationHeaders(userToken),
        data,
      },
    );

    return response.data;
  } catch (error) {
    console.error("Error updating guest order:", error);
    throw error;
  }
};

export const deleteGuestOrder = async (idGuestOrder, userToken) => {
  try {
    await apiRequest(`${API_BASE_URL}/guestorders/${idGuestOrder}`, {
      method: "DELETE",
      credentials: "include",
      headers: getAuthorizationHeaders(userToken),
    });
  } catch (error) {
    console.error("Error deleting guest order:", error);
    throw error;
  }
};

const guestOrderService = {
  createGuestOrder,
  getGuestOrders,
  updateGuestOrder,
  deleteGuestOrder,
};

export default guestOrderService;
