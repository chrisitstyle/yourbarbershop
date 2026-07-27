import { useTranslation } from "react-i18next";
import {
  toneForValue,
  humanize,
} from "../../adminpages/utils/adminTableHelpers.js";

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

export default StatusBadge;
