import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faGithub,
  faInstagram,
  faFacebook,
} from "@fortawesome/free-brands-svg-icons";
import { faScissors } from "@fortawesome/free-solid-svg-icons";
import "./css/Footer.css";

const SOCIAL_LINKS = [
  {
    icon: faInstagram,
    href: "https://instagram.com",
    label: "Instagram",
  },
  {
    icon: faFacebook,
    href: "https://facebook.com",
    label: "Facebook",
  },
  {
    icon: faGithub,
    href: "https://github.com/chrisitstyle",
    label: "GitHub",
  },
];

const Footer = () => {
  const { t } = useTranslation();
  const currentYear = new Date().getFullYear();
  const author = "Krzysztof Podjacki";

  return (
    <footer className="footer">
      <div className="container">
        {/* top section: brand + columns */}
        <div className="footer-top">
          {/* brand column */}
          <div className="footer-brand">
            <div className="footer-logo">
              <FontAwesomeIcon icon={faScissors} className="footer-logo-icon" />
              <span className="footer-logo-text">YourBarbershop</span>
            </div>
            <p className="footer-tagline">{t("footer.tagline")}</p>
            {/* social icons */}
            <div className="footer-social" aria-label="Social media">
              {SOCIAL_LINKS.map(({ icon, href, label }) => (
                <a
                  key={label}
                  href={href}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="footer-social-link"
                  aria-label={label}
                >
                  <FontAwesomeIcon icon={icon} />
                </a>
              ))}
            </div>
          </div>

          {/* quick links column */}
          <nav className="footer-col" aria-label={t("footer.nav.title")}>
            <h6 className="footer-col-heading">{t("footer.nav.title")}</h6>
            <ul className="footer-links">
              <li>
                <Link to="/">{t("footer.nav.home")}</Link>
              </li>
              <li>
                <Link to="/offer">{t("footer.nav.offer")}</Link>
              </li>
              <li>
                <Link to="/gallery">{t("footer.nav.gallery")}</Link>
              </li>
              <li>
                <Link to="/registerorder">{t("footer.nav.book")}</Link>
              </li>
            </ul>
          </nav>

          {/* contact column */}
          <div className="footer-col">
            <h6 className="footer-col-heading">
              {t("footer.contact.title", "Kontakt")}
            </h6>
            <ul className="footer-links">
              <li>{t("footer.contact.address")}</li>
              <li>
                <a href="tel:+48000000000">{t("footer.contact.phone")}</a>
              </li>
              <li>
                <a href="mailto:kontakt@yourbarbershop.pl">
                  {t("footer.contact.email", "kontakt@yourbarbershop.pl")}
                </a>
              </li>
              <li>{t("footer.contact.hours", "Pon–Sob: 9:00 – 20:00")}</li>
            </ul>
          </div>
        </div>

        {/* divider */}
        <hr className="footer-divider" />

        {/* bottom bar: copyright + author */}
        <div className="footer-bottom">
          <span>
            &copy; {currentYear} YourBarbershop. {t("footer.rights")}
          </span>
          <span>
            {t("footer.createdBy")}{" "}
            <a
              href="https://github.com/chrisitstyle"
              target="_blank"
              rel="noopener noreferrer"
            >
              {author}
            </a>
          </span>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
