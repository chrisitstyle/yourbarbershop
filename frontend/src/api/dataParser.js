import { toZonedTime, format } from "date-fns-tz";

export const formatDate = (date) => {
  return format(new Date(date), "yyyy-MM-dd HH:mm:ss");
};

export const formatSelectedDateTime = (date, hour, minute) => {
  const formattedHour = String(hour).padStart(2, "0");
  const formattedMinute = String(minute).padStart(2, "0");

  return `${date}T${formattedHour}:${formattedMinute}:00`;
};

export const formatShortDate = (date, locale = "pl-PL") => {
  if (!date) return null;

  return new Date(date).toLocaleDateString(locale, {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

export const getCurrentDateTime = () => {
  const currentDateTimeUTC = toZonedTime(new Date(), "Europe/Warsaw");

  return format(currentDateTimeUTC, "yyyy-MM-dd'T'HH:mm:ss");
};
