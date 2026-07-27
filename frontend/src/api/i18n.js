import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import plTranslation from "./locales/pl.json";
import enTranslation from "./locales/en.json";

// translation dictionaries
const resources = {
  pl: {
    translation: plTranslation,
  },
  en: {
    translation: enTranslation,
  },
};

i18n
  .use(LanguageDetector) // detects browser language
  .use(initReactI18next) // passes i18n down to react-i18next
  .init({
    resources,
    fallbackLng: "en", // default language if detected language is missing
    interpolation: {
      escapeValue: false, // react already protects from xss
    },
  });

export default i18n;
