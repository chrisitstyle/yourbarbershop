import { useEffect } from "react";

import { Button } from "react-bootstrap";
import { CircleCheck } from "lucide-react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { toast } from "sonner";

import { useAuth } from "../auth/AuthContextValue";

import "./styles/payment-success.css";

const PaymentSuccess = () => {
  const { user } = useAuth();
  const { t } = useTranslation();

  useEffect(() => {
    toast.success(t("payment.successTitle", "Płatność zakończona sukcesem"), {
      description: t("payment.successToastMessage"),
      duration: 5000,
    });
  }, [t]);

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-9 col-lg-7 col-xl-6">
          <div className="card border-0 shadow-lg overflow-hidden">
            <div className="card-body p-4 p-md-5 text-center">
              <div className="payment-success-icon d-inline-flex align-items-center justify-content-center rounded-circle mb-4">
                <CircleCheck size={36} aria-hidden="true" />
              </div>

              <div className="mb-4">
                <span className="badge rounded-pill text-bg-success mb-3">
                  {t("payment.paymentConfirmed")}
                </span>

                <h1 className="h3 fw-bold mb-3">{t("payment.successTitle")}</h1>

                <p className="text-body-secondary mb-0">
                  {t("payment.successMessage")}
                </p>
              </div>

              <div className="border rounded-3 bg-body-tertiary p-3 mb-4">
                <p className="small text-body-secondary mb-0">
                  {t("payment.successInfo")}
                </p>
              </div>

              <div className="d-grid">
                {user ? (
                  <Button
                    as={Link}
                    to={`/profile/${user.id}?registrationOrderSuccess=true`}
                    variant="dark"
                    size="lg"
                  >
                    {t("payment.goToProfile", "Przejdź do profilu")}
                  </Button>
                ) : (
                  <Button as={Link} to="/" variant="dark" size="lg">
                    {t("payment.goHome")}
                  </Button>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentSuccess;
