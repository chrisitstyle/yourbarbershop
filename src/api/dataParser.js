import { utcToZonedTime, zonedTimeToUtc, format } from "date-fns-tz";

export const formatDate = (date) => {
  return format(new Date(date), "yyyy-MM-dd HH:mm:ss");
};

export const formatSelectedDateTime = (date, hour, minute) => {
  const selectedDateTime = new Date(date);
  selectedDateTime.setHours(hour);
  selectedDateTime.setMinutes(minute);

  const selectedDateTimeUTC = zonedTimeToUtc(selectedDateTime, "Europe/Warsaw");

  return format(selectedDateTimeUTC, "yyyy-MM-dd'T'HH:mm:ss", {
    timeZone: "UTC",
  });
};

export const getCurrentDateTime = () => {
  const currentDateTimeUTC = utcToZonedTime(new Date(), "Europe/Warsaw");
  return format(currentDateTimeUTC, "yyyy-MM-dd'T'HH:mm:ss");
};
