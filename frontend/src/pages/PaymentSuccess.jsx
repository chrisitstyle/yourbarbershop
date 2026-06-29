import { Alert, Button } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContextValue";
import { useTranslation } from "react-i18next";

const PaymentSuccess = () => {
  const { user } = useAuth();
  const { t } = useTranslation();

  return (
    <div className="container my-5 py-5">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <Alert variant="success" className="text-center shadow-sm">
            <h4 className="mb-3">
              {t("payment.successTitle", "Płatność zakończona sukcesem")}
            </h4>

            <p className="mb-0">
              {t(
                "payment.successMessage",
                "Dziękujemy! Twoja płatność została przyjęta. Szczegóły rezerwacji zostały wysłane na adres e-mail.",
              )}
            </p>
          </Alert>

          <div className="d-flex justify-content-center gap-2 mt-4">
            {user ? (
              <Button
                as={Link}
                to={`/profile/${user.id}?registrationOrderSuccess=true`}
                variant="dark"
              >
                {t("payment.goToProfile", "Przejdź do profilu")}
              </Button>
            ) : (
              <Button as={Link} to="/" variant="dark">
                {t("payment.goHome", "Wróć na stronę główną")}
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentSuccess;
