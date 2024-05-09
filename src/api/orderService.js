import axios from "axios";

const apiUrl = "http://localhost:8080";

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

export const updateOrder = async (orderId, newData, token) => {
  try {
    const response = await axios.put(
      `http://localhost:8080/orders/update/${orderId}`,
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

const orderService = {
  getOrders,
  updateOrder,
  deleteOrder,
};

export default orderService;
