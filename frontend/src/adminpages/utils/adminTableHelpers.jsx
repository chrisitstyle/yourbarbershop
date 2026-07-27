// shared helpers for admin tables
import { useTranslation } from "react-i18next";

/**
 * maps exact backend enum values to a bootstrap contextual color variant.
 * falls back to "secondary" for anything unknown so it never breaks.
 *
 * @type {Record<string, string>}
 */
const TONE_MAP = {
  // status (appointment lifecycle)
  NOWE: "info",
  ZREALIZOWANE: "success",
  ANULOWANE: "danger",
  // role
  USER: "primary",
  ADMIN: "danger",
  // payment method
  GOTOWKA: "secondary",
  KARTA_ONLINE: "info",
  KARTA_NA_MIEJSCU: "secondary",
  // payment status
  NIE_WYMAGANA: "secondary",
  OCZEKUJE_NA_PLATNOSC: "warning",
  OPLACONA: "success",
  NIEUDANA: "danger",
  WYGASLA: "dark",
  ZWROCONA: "info",
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
function humanize(value) {
  const s = String(value).replaceAll("_", " ").toLowerCase();
  return s.charAt(0).toUpperCase() + s.slice(1);
}

/**
 * small rounded pill component for statuses, roles, and payment values inside tables.
 * looks up a translated label under the "enums" namespace (e.g. enums.OCZEKUJE_NA_PLATNOSC).
 *
 * @param {object} props - component props
 * @param {string|null|undefined} props.value - raw enum value to display in badge
 * @returns {JSX.Element} styled badge element or fallback dash
 */
export function StatusBadge({ value }) {
  const { t } = useTranslation();
  if (value === null || value === undefined || value === "")
    return <span>—</span>;

  const key = String(value).trim().toUpperCase();
  const tone = toneForValue(key);
  const label = t(`enums.${key}`, { defaultValue: humanize(value) });

  return <span className={`rtable-badge text-bg-${tone}`}>{label}</span>;
}
