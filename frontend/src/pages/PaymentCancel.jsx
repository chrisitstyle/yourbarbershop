import { Alert, Button } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

const PaymentCancel = () => {
  const { t } = useTranslation();

  return (
    <div className="container my-5 py-5">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <Alert variant="warning" className="text-center shadow-sm">
            <h4 className="mb-3">
              {t("payment.cancelTitle", "Płatność nie została ukończona")}
            </h4>

            <p className="mb-0">
              {t(
                "payment.cancelMessage",
                "Proces płatności został przerwany. Możesz wrócić i spróbować ponownie.",
              )}
            </p>
          </Alert>

          <div className="d-flex justify-content-center gap-2 mt-4">
            <Button as={Link} to="/register-order" variant="dark">
              {t("payment.tryAgain", "Spróbuj ponownie")}
            </Button>

            <Button as={Link} to="/" variant="outline-dark">
              {t("payment.goHome", "Wróć na stronę główną")}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentCancel;
