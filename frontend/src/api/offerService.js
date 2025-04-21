import axios from "axios";

const apiUrl = "http://localhost:8080";

export const addOffer = async (newOffer, userToken) => {
  try {
    await axios.post(`${apiUrl}/offers`, newOffer, {
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

export const getOffers = async () => {
  try {
    const result = await axios.get(`${apiUrl}/offers`);
    return result.data;
  } catch (error) {
    console.error("Error loading offers:", error);
    throw error;
  }
};

export const updateOffer = async (offerId, newData, token) => {
  try {
    const response = await axios.put(
      `http://localhost:8080/offers/${offerId}`,
      newData,
      {
        withCredentials: true,
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    return response.data;
  } catch (error) {
    throw error;
  }
};
export const deleteOffer = async (idOffer, userToken) => {
  try {
    await axios.delete(`${apiUrl}/offers/${idOffer}`, {
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

const offerService = {
  addOffer,
  getOffers,
  updateOffer,
  deleteOffer,
};

export default offerService;
