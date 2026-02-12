import React from "react";
import { useTranslation } from "react-i18next";

const LanguageSwitcher = () => {
  const { i18n } = useTranslation();

  const changeLanguage = (lng) => {
    i18n.changeLanguage(lng);
  };

  return (
    <div className="d-flex align-items-center">
      <button
        type="button"
        className={`btn btn-link text-decoration-none p-0 fw-bold ${
          i18n.language === "pl" ? "text-white" : "text-secondary"
        }`}
        onClick={() => changeLanguage("pl")}
        style={{ transition: "color 0.3s ease" }}
      >
        PL
      </button>

      <span className="text-secondary mx-2">|</span>

      <button
        type="button"
        className={`btn btn-link text-decoration-none p-0 fw-bold ${
          i18n.language === "en" ? "text-white" : "text-secondary"
        }`}
        onClick={() => changeLanguage("en")}
        style={{ transition: "color 0.3s ease" }}
      >
        EN
      </button>
    </div>
  );
};

export default LanguageSwitcher;
