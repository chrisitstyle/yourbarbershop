import { useState } from "react";

import { Alert, Button, Spinner } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";

import { resolvePaymentCheckout } from "../api/paymentService";

const PaymentLink = () => {
  const { t } = useTranslation();
  const { token } = useParams();

  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handlePayment = async () => {
    if (!token || isLoading) {
      return;
    }

    setIsLoading(true);
    setErrorMessage("");

    try {
      const response = await resolvePaymentCheckout(token);
      const checkoutUrl = response?.checkoutUrl;

      if (!checkoutUrl) {
        throw new Error(t("payment.checkoutUrlError"));
      }

      window.location.assign(checkoutUrl);
    } catch (error) {
      setErrorMessage(
        error?.response?.data?.message ||
          error?.message ||
          t("payment.checkoutRedirectError"),
      );

      setIsLoading(false);
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-lg-7 col-xl-6">
          <div className="card shadow-sm border-0">
            <div className="card-body p-4 p-md-5 text-center">
              <h1 className="h3 mb-3">{t("payment.retryTitle")}</h1>

              <p className="text-body-secondary mb-4">
                {t("payment.retryDescription")}
              </p>

              {errorMessage && <Alert variant="danger">{errorMessage}</Alert>}

              <div className="d-flex justify-content-center gap-2">
                <Button
                  variant="dark"
                  onClick={handlePayment}
                  disabled={isLoading || !token}
                >
                  {isLoading ? (
                    <>
                      <Spinner
                        animation="border"
                        size="sm"
                        className="me-2"
                        aria-hidden="true"
                      />
                      {t("payment.redirecting")}
                    </>
                  ) : (
                    t("payment.continuePayment")
                  )}
                </Button>

                <Button
                  as={Link}
                  to="/"
                  variant="outline-secondary"
                  disabled={isLoading}
                >
                  {t("payment.goHome")}
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentLink;
