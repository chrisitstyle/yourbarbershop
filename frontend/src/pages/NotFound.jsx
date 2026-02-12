import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";

export default function NotFound() {
  const { t } = useTranslation();

  return (
    <div style={{ textAlign: "center", padding: "4rem" }}>
      <h1>404</h1>
      <h2>{t("notfound.subtitle")}</h2>
      <p>{t("notfound.text")}</p>
      <Link to="/" className="btn btn-dark mt-4">
        {t("notfound.backHome")}
      </Link>
    </div>
  );
}
