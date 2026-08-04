import { API_BASE_URL } from "./config.js";
import { apiRequest } from "./httpClient.js";

export const createGuestOrder = async (
  guestOrderCreationData,
  idempotencyKey,
) => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/guestorders`, {
      method: "POST",
      headers: {
        "Idempotency-Key": idempotencyKey,
      },
      data: guestOrderCreationData,
    });

    return response.data;
  } catch (error) {
    console.error("Error creating guest order:", error);
    throw error;
  }
};

export const getGuestOrders = async () => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/guestorders`, {
      credentials: "include",
    });

    return response.data;
  } catch (error) {
    console.error("Error loading guest orders:", error);
    throw error;
  }
};

export const updateGuestOrder = async (idGuestOrder, data) => {
  try {
    const response = await apiRequest(
      `${API_BASE_URL}/guestorders/${idGuestOrder}`,
      {
        method: "PUT",
        credentials: "include",
        data,
      },
    );

    return response.data;
  } catch (error) {
    console.error("Error updating guest order:", error);
    throw error;
  }
};

export const deleteGuestOrder = async (idGuestOrder) => {
  try {
    await apiRequest(`${API_BASE_URL}/guestorders/${idGuestOrder}`, {
      method: "DELETE",
      credentials: "include",
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
