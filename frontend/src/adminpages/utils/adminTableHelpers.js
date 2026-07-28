// shared helpers for admin tables

/**
 * maps exact backend enum values to a bootstrap contextual color variant.
 * falls back to "secondary" for anything unknown so it never breaks.
 *
 * @type {Record<string, string>}
 */
const TONE_MAP = {
  // Status (appointment lifecycle)
  NOWE: "info",
  ZREALIZOWANE: "success",
  ANULOWANE: "danger",
  // Role
  USER: "primary",
  ADMIN: "danger",
  // PaymentMethod
  GOTOWKA: "secondary",
  KARTA_ONLINE: "info",
  KARTA_NA_MIEJSCU: "secondary",
  // PaymentStatus
  NIE_WYMAGANA: "secondary",
  OCZEKUJE_NA_PLATNOSC: "warning",
  OPLACONA: "success",
  NIEUDANA: "danger",
  WYGASLA: "dark",
  ZWROCONA: "info",
  // ActionType (audit log actions)
  ORDER_CREATED: "success",
  ORDER_UPDATED: "info",
  ORDER_DELETED: "danger",
  GUEST_ORDER_CREATED: "success",
  GUEST_ORDER_UPDATED: "info",
  GUEST_ORDER_DELETED: "danger",
  OFFER_CREATED: "success",
  OFFER_UPDATED: "info",
  OFFER_DELETED: "danger",
  USER_CREATED: "success",
  USER_UPDATED: "info",
  USER_DELETED: "danger",
  // EntityType (audit log entity types)
  ORDER: "info",
  GUEST_ORDER: "secondary",
  OFFER: "warning",
};

/**
 * returns the bootstrap contextual color tone for a given enum value string.
 *
 * @param {string|null|undefined} value - the enum string value to resolve
 * @returns {string} bootstrap variant name (e.g. "info", "success", "danger")
 */
export function toneForValue(value) {
  if (value === null || value === undefined) return "secondary";
  const key = String(value).trim().toUpperCase();
  return TONE_MAP[key] || "secondary";
}

/**
 * humanized fallback if an i18n key is missing (e.g. "OCZEKUJE_NA_PLATNOSC" -> "Oczekuje na platnosc")
 *
 * @param {string} value - raw enum value with underscores
 * @returns {string} capitalized readable string fallback
 */
export function humanize(value) {
  const s = String(value).replaceAll("_", " ").toLowerCase();
  return s.charAt(0).toUpperCase() + s.slice(1);
}
