import axios from "axios";

const apiUrl = "http://localhost:8080";

export const getGuestOrders = async (userToken) => {
  try {
    const result = await axios.get(`${apiUrl}/guestorders/get`, {
      withCredentials: true,
      headers: {
        Authorization: `Bearer ${userToken}`,
      },
    });
    return result.data;
  } catch (error) {
    console.error("Error loading guest orders:", error);
    throw error;
  }
};

export const updateGuestOrder = async (idGuestOrder, data, userToken) => {
  try {
    const response = await axios.put(
      `${apiUrl}/guestorders/update/${idGuestOrder}`,
      data,
      {
        withCredentials: true,
        headers: {
          Authorization: `Bearer ${userToken}`,
        },
      }
    );

    return response.data;
  } catch (error) {
    console.error("Error updating guest order:", error);
    throw error;
  }
};

export const deleteGuestOrder = async (idGuestOrder, userToken) => {
  try {
    await axios.delete(`${apiUrl}/guestorders/delete/${idGuestOrder}`, {
      withCredentials: true,
      headers: {
        Authorization: `Bearer ${userToken}`,
      },
    });
  } catch (error) {
    console.error("Error deleting guestorder:", error);
    throw error;
  }
};

const guestOrderService = {
  getGuestOrders,
  updateGuestOrder,
  deleteGuestOrder,
};

export default guestOrderService;
