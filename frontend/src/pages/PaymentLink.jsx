import { useState } from "react";

import { Button, Spinner } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";
import { toast } from "sonner";

import { resolvePaymentCheckout } from "../api/paymentService";

import "./styles/payment-link.css";

const PaymentLink = () => {
  const { t } = useTranslation();
  const { token } = useParams();

  const [isLoading, setIsLoading] = useState(false);

  const handlePayment = async () => {
    if (!token || isLoading) {
      return;
    }

    setIsLoading(true);

    try {
      const response = await resolvePaymentCheckout(token);
      const checkoutUrl = response?.checkoutUrl;

      if (!checkoutUrl) {
        throw new Error(t("payment.checkoutUrlError"));
      }

      window.location.assign(checkoutUrl);
    } catch (error) {
      toast.error(t("payment.errorTitle"), {
        description:
          error?.response?.data?.message ||
          error?.message ||
          t("payment.checkoutRedirectError"),
        duration: 5000,
      });

      setIsLoading(false);
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-9 col-lg-7 col-xl-6">
          <div className="card border-0 shadow-lg overflow-hidden">
            <div className="card-body p-4 p-md-5 text-center">
              <div className="mb-4">
                <div
                  className="
                    d-inline-flex
                    align-items-center
                    justify-content-center
                    rounded-circle
                    bg-body-secondary
                    mb-3
                    payment-link-icon
                  "
                >
                  💳
                </div>

                <div className="mb-2">
                  <span className="badge rounded-pill text-bg-secondary">
                    {t("payment.secureCheckout")}
                  </span>
                </div>

                <h1 className="h3 fw-bold mb-3">{t("payment.retryTitle")}</h1>

                <p className="text-body-secondary mb-0">
                  {t("payment.retryDescription")}
                </p>
              </div>

              <div className="border rounded-3 bg-body-tertiary p-3 mb-4">
                <p className="small text-body-secondary mb-0">
                  {t("payment.retrySecurityInfo")}
                </p>
              </div>

              <div className="d-grid gap-2">
                <Button
                  variant="dark"
                  size="lg"
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

              <p className="small text-body-secondary mt-4 mb-0">
                {t("payment.stripeNotice")}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentLink;
