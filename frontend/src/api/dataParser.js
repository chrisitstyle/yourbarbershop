import { toZonedTime, fromZonedTime, format } from "date-fns-tz";

export const formatDate = (date) => {
  return format(new Date(date), "yyyy-MM-dd HH:mm:ss");
};

export const formatSelectedDateTime = (date, hour, minute) => {
  const selectedDateTime = new Date(date);
  selectedDateTime.setHours(hour);
  selectedDateTime.setMinutes(minute);

  const selectedDateTimeUTC = fromZonedTime(selectedDateTime, "Europe/Warsaw");

  return format(selectedDateTimeUTC, "yyyy-MM-dd'T'HH:mm:ss", {
    timeZone: "UTC",
  });
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
