import { formatDate } from "../api/dataParser";

/**
 * Retrieves a value from an object based on a field name or a dot-notation path.
 * Applies specific formatting for costs and dates, and handles nested property access.
 *
 * @param {Object} obj - The source object (e.g., an order or user object).
 * @param {String} field - The field key or dot-notation path (e.g., "user.firstname") to retrieve.
 * @returns {String|Number} The retrieved and formatted value, or "brak" if the value is missing/null.
 */

export function getNestedValue(obj, field) {
  if (field === "offer.cost")
    return obj.offer ? obj.offer.cost + " zł" : "brak";
  if (field === "orderDate")
    return obj.orderDate ? formatDate(obj.orderDate) : "brak";
  if (field === "visitDate")
    return obj.visitDate ? formatDate(obj.visitDate) : "brak";

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
