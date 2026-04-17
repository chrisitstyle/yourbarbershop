import axios from "axios";
import { API_BASE_URL } from "../api/config.js";

export const sendCustomEmail = async (to, subject, message) => {
  try {
    await axios.post(`${API_BASE_URL}/send-email`, {
      to,
      subject,
      message,
    });
  } catch (error) {
    console.error("Błąd wysyłania e-maila:", error);
  }
};

const emailService = {
  sendCustomEmail,
};

export default emailService;
