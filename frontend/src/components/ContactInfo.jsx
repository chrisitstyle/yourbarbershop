import {
  faPhone,
  faEnvelope,
  faLocationDot,
  faClock,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import "./styles/contactinfo.css";
import { useTranslation } from "react-i18next";

const ContactInfo = () => {
  const { t } = useTranslation();
  const isOpen = (() => {
    const day = new Date().getDay();
    const hour = new Date().getHours();
    // Mon-Fri: 8-19, Sat: 10-14, Sun: closed
    if (day >= 1 && day <= 5 && hour >= 8 && hour < 19.5) return true;
    if (day === 6 && hour >= 10 && hour < 14) return true;
    return false;
  })();

  return (
    <div className="contactinfo-container bg-body">
      <h2 className="mb-3">{t("contact.info.title")}</h2>
      <div className="contactinfo-grid">
        <div className="contactinfo-item">
          <FontAwesomeIcon icon={faPhone} className="contactinfo-icon" />
          <div>
            <div className="contactinfo-label">{t("contact.info.phone")}</div>
            <a href="tel:+48123123123" className="contactinfo-value">
              +48 123 123 123
            </a>
          </div>
        </div>
        <div className="contactinfo-item">
          <FontAwesomeIcon icon={faEnvelope} className="contactinfo-icon" />
          <div>
            <div className="contactinfo-label">{t("contact.info.email")}</div>
            <a
              href="mailto:kontakt@yourbarbershop.com"
              className="contactinfo-value"
            >
              kontakt@yourbarbershop.com
            </a>
          </div>
        </div>
        <div className="contactinfo-item">
          <FontAwesomeIcon icon={faLocationDot} className="contactinfo-icon" />
          <div>
            <div className="contactinfo-label">{t("contact.info.address")}</div>
            <div className="contactinfo-value">
              ul. Testowa 123
              <br />
              85-796 Bydgoszcz
              {t("contact.info.country")}
            </div>
          </div>
        </div>
      </div>
      <div className="contactinfo-hours mt-4">
        <div className="contactinfo-hours-header">
          <FontAwesomeIcon icon={faClock} className="contactinfo-icon" />
          <span className="contactinfo-label">
            {t("contact.info.hoursTitle")}
          </span>
          {isOpen && (
            <span className="contactinfo-badge">
              {t("contact.info.openNow")}
            </span>
          )}
        </div>
        <div className="contactinfo-hours-table">
          <div>
            <span>{t("contact.info.monFri")}</span>{" "}
            <span className="contactinfo-hours-time">8:00 - 19:30</span>
          </div>
          <div>
            <span>{t("contact.info.sat")}</span>{" "}
            <span className="contactinfo-hours-time">10:00 - 14:00</span>
          </div>
          <div>
            <span>{t("contact.info.sun")}</span>{" "}
            <span className="contactinfo-hours-time closed">
              {t("contact.info.closed")}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContactInfo;
