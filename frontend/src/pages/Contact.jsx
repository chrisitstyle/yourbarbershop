import React from "react";
import {
  faFacebook,
  faXTwitter,
  faInstagram,
  faYoutube,
  faTiktok,
} from "@fortawesome/free-brands-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

const Contact = () => {
  return (
    <>
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-lg-8">
            <h2 className="mb-4">Skontaktuj się z nami</h2>
            <address>
              <p>
                <strong>Barbershop</strong>
                <br />
                ul. Testowa 123
                <br />
                85-796 Bydgoszcz
              </p>
              <p>
                <strong>Telefon:</strong> +48 123-123-123
              </p>
              <p>
                <strong>Email:</strong>{" "}
                <a href="mailto:kontakt@barbershop.com">
                  kontakt@barbershop.com
                </a>
              </p>
            </address>

            <div className="mb-4">
              <h4>Godziny otwarcia</h4>
              <p>
                Poniedziałek - Piątek: 8:00 - 19:30
                <br />
                Sobota: 10:00 - 14:00
                <br />
                Niedziela: Zamknięte
              </p>
            </div>

            <div className="mb-4">
              <h4>Znajdź nas na mapie</h4>
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

            <p className="mb-0">
              Jesteśmy dostępni również w mediach społecznościowych:
            </p>
            <div className="social-icons">
              <a
                href="https://www.facebook.com"
                target="_blank"
                rel="noopener noreferrer"
              >
                <FontAwesomeIcon icon={faFacebook} />
              </a>
              <a
                href="https://twitter.com"
                target="_blank"
                rel="noopener noreferrer"
              >
                <FontAwesomeIcon
                  icon={faXTwitter}
                  style={{ color: "#000000" }}
                />
              </a>
              <a
                href="https://www.instagram.com"
                target="_blank"
                rel="noopener noreferrer"
              >
                <FontAwesomeIcon
                  icon={faInstagram}
                  style={{ color: "#e4405f" }}
                />
              </a>
              <a
                href="https://www.youtube.com"
                target="_blank"
                rel="noopener noreferrer"
              >
                <FontAwesomeIcon
                  icon={faYoutube}
                  style={{ color: "#ff1900" }}
                />
              </a>
              <a
                href="https://www.tiktok.com"
                target="_blank"
                rel="noopener noreferrer"
              >
                <FontAwesomeIcon icon={faTiktok} style={{ color: "#000000" }} />
              </a>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Contact;
