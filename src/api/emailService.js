import axios from "axios";
import { formatSelectedDateTime } from "../api/dataParser";

export const sendConfirmationEmail = async (
  email,
  firstname,
  lastname,
  selectedDate,
  selectedHour,
  selectedMinute,
  selectedOfferName,
  selectedOfferCost
) => {
  try {
    await axios.post("http://localhost:8080/send-email", {
      to: email,
      subject: "Potwierdzenie umówienia wizyty w YourBarbershop",
      message: `Szanowny(a) ${firstname} ${lastname},\n\nDziękujemy za umówienie wizyty w naszym salonie YourBarbershop.\n\nData wizyty: ${formatSelectedDateTime(
        selectedDate,
        selectedHour,
        selectedMinute
      ).replace(
        "T",
        " "
      )}.\n\nWybrana oferta: ${selectedOfferName}\nKoszt usługi: ${selectedOfferCost} zł.\n\nZapraszamy w uzgodnionym terminie do nas!\n\nZ poważaniem,\nZespół YourBarbershop`,
    });
  } catch (error) {
    console.error("Błąd wysyłania e-maila:", error);
  }
};

export const sendCustomEmail = async (to, subject, message) => {
  try {
    await axios.post("http://localhost:8080/send-email", {
      to,
      subject,
      message,
    });
  } catch (error) {
    console.error("Błąd wysyłania e-maila:", error);
  }
};

const emailService = {
  sendConfirmationEmail,
  sendCustomEmail,
};

export default emailService;
