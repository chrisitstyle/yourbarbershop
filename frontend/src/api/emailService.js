import { API_BASE_URL } from "./config.js";
import { apiRequest } from "./httpClient.js";

export const sendCustomEmail = async (to, subject, message) => {
  try {
    await apiRequest(`${API_BASE_URL}/send-email`, {
      method: "POST",
      data: {
        to,
        subject,
        message,
      },
    });
  } catch (error) {
    console.error("Błąd wysyłania e-maila:", error);
    throw error;
  }
};

const emailService = {
  sendCustomEmail,
};

export default emailService;
