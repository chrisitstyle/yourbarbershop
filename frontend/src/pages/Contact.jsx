import ContactInfo from "../components/ContactInfo";
import {
  faFacebook,
  faXTwitter,
  faInstagram,
  faYoutube,
  faTiktok,
} from "@fortawesome/free-brands-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import "./styles/contact.css";

const Contact = () => {
  return (
    <div className="contact-container container mt-5 py-4 px-3 shadow-lg rounded-4">
      <div className="row gy-4 align-items-center">
        {/* left column */}
        <div className="col-md-6">
          <ContactInfo />

          {/* SOCIAL MEDIA*/}
          <div>
            <div className="mb-1 fw-medium">
              Jesteśmy dostępni również w mediach społecznościowych:
            </div>
            <div className="social-icons mt-2">
              <a
                href="https://www.facebook.com"
                target="_blank"
                rel="noopener noreferrer"
                className="social-icon facebook"
                aria-label="Facebook"
              >
                <FontAwesomeIcon icon={faFacebook} />
              </a>
              <a
                href="https://twitter.com"
                target="_blank"
                rel="noopener noreferrer"
                className="social-icon twitter"
                aria-label="X / Twitter"
              >
                <FontAwesomeIcon icon={faXTwitter} />
              </a>
              <a
                href="https://www.instagram.com"
                target="_blank"
                rel="noopener noreferrer"
                className="social-icon instagram"
                aria-label="Instagram"
              >
                <FontAwesomeIcon icon={faInstagram} />
              </a>
              <a
                href="https://www.youtube.com"
                target="_blank"
                rel="noopener noreferrer"
                className="social-icon youtube"
                aria-label="YouTube"
              >
                <FontAwesomeIcon icon={faYoutube} />
              </a>
              <a
                href="https://www.tiktok.com"
                target="_blank"
                rel="noopener noreferrer"
                className="social-icon tiktok"
                aria-label="TikTok"
              >
                <FontAwesomeIcon icon={faTiktok} />
              </a>
            </div>
          </div>
        </div>

        {/* right column */}
        <div className="col-md-6">
          <h4 className="mb-3">Znajdź nas na mapie</h4>
          <div className="map-responsive rounded-4 overflow-hidden shadow-sm">
            <iframe
              title="Mapa"
              src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2420.123853947458!2d18.13079468350402!3d53.14336747259944!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x47bf7b48502b929f%3A0x1d8b9df1a26ee9ea!2sPolitechnika%20Bydgoska%20im.%20J%C3%B3zefa%20%C5%9Aniadeckiego!5e0!3m2!1sen!2sus!4v1672368772172!5m2!1sen!2sus"
              width="100%"
              height="300"
              style={{ border: 0 }}
              allowFullScreen=""
              loading="lazy"
            ></iframe>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Contact;
