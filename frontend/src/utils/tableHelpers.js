import { formatShortDate } from "../api/dataParser";
import i18n from "../api/i18n";

/**
 * Retrieves a value from an object based on a field name or a dot-notation path.
 * Applies specific formatting for costs and dates, and handles nested property access.
 *
 * @param {Object} obj - The source object (e.g., an order or user object).
 * @param {String} field - The field key or dot-notation path (e.g., "user.firstname") to retrieve.
 * @returns {String|Number} The retrieved and formatted value, or "brak" if the value is missing/null.
 */

export function getNestedValue(obj, field, lng = "pl") {
  const currentLang = i18n.language || "pl";
  const locale = currentLang.startsWith("en") ? "en-US" : "pl-PL";

  if (field === "offer.cost") return obj.offer ? obj.offer.cost : "brak";
  if (field === "orderDate")
    return obj.orderDate ? formatShortDate(obj.orderDate, locale) : "brak";
  if (field === "visitDate")
    return obj.visitDate ? formatShortDate(obj.visitDate, locale) : "brak";

  if (field.includes(".")) {
    const split = field.split(".");
    let value = obj;
    for (const key of split) {
      if (value && value[key]) {
        value = value[key];
      } else {
        return "brak";
      }
    }
    return value;
  }
  return obj[field] || "brak";
}
