import { Button, Card } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

const GuestOrderSuccessAlert = () => {
  const { t } = useTranslation();

  return (
    <div className="d-flex justify-content-center px-3 py-4">
      <Card
        className="border-0 shadow-sm text-center w-100"
        style={{ maxWidth: "480px", borderRadius: "1rem" }}
      >
        <Card.Body className="p-4 p-md-5">
          {/* Success icon */}
          <div
            className="d-inline-flex align-items-center justify-content-center mb-4"
            style={{
              width: "72px",
              height: "72px",
              borderRadius: "50%",
              backgroundColor: "rgba(25, 135, 84, 0.12)",
            }}
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="36"
              height="36"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#198754"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <path d="M20 6 9 17l-5-5" />
            </svg>
          </div>

          <h5 className="fw-bold mb-2">
            {t("orders.guestSuccessTitle", "Rezerwacja potwierdzona")}
          </h5>

          <p
            className="text-secondary mb-4 px-md-2"
            style={{ lineHeight: 1.6 }}
          >
            {t(
              "orders.guestSuccessMessage",
              "Dziękujemy! Twoja wizyta została pomyślnie zarejestrowana. Szczegóły rezerwacji znajdziesz w wiadomości wysłanej na Twój adres e-mail.",
            )}
          </p>

          <div className="d-flex flex-column flex-sm-row justify-content-center gap-2">
            <Button as={Link} to="/login" variant="primary">
              {t("auth.loginTitle", "Zaloguj się")}
            </Button>
            <Button as={Link} to="/" variant="outline-secondary">
              {t("payment.goHome", "Wróć na stronę główną")}
            </Button>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
};

export default GuestOrderSuccessAlert;
