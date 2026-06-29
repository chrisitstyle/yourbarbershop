import { API_BASE_URL } from "./config.js";
import { apiRequest, getAuthorizationHeaders } from "./httpClient.js";

export const addOffer = async (newOffer, userToken) => {
  try {
    await apiRequest(`${API_BASE_URL}/offers`, {
      method: "POST",
      credentials: "include",
      data: newOffer,
    });
  } catch (error) {
    console.error("Error adding offer:", error);
    throw error;
  }
};

export const getOffers = async () => {
  try {
    const response = await apiRequest(`${API_BASE_URL}/offers`);

    return response.data;
  } catch (error) {
    console.error("Error loading offers:", error);
    throw error;
  }
};

export const updateOffer = async (offerId, newData, userToken) => {
  const response = await apiRequest(`${API_BASE_URL}/offers/${offerId}`, {
    method: "PUT",
    credentials: "include",
    data: newData,
  });

  return response.data;
};

export const deleteOffer = async (idOffer, userToken) => {
  try {
    await apiRequest(`${API_BASE_URL}/offers/${idOffer}`, {
      method: "DELETE",
      credentials: "include",
    });
  } catch (error) {
    console.error("Error deleting offer:", error);
    throw error;
  }
};

const offerService = {
  addOffer,
  getOffers,
  updateOffer,
  deleteOffer,
};

export default offerService;
