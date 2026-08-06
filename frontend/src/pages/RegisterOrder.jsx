import { useState } from "react";
import { useAuth } from "../auth/AuthContextValue";
import RegisterOrderLoggedForm from "../components/RegisterOrderLoggedForm";
import RegisterOrderWithoutAccForm from "../components/RegisterOrderWithoutAccForm";
import OrderSuccessAlert from "../components/OrderSuccessAlert";
import { useTranslation } from "react-i18next";

const RegisterOrder = () => {
  const { user } = useAuth();
  const { t } = useTranslation();

  // state to manage order success alert visibility for all non-card-online payments
  const [showSuccessAlert, setShowSuccessAlert] = useState(false);

  return (
    <div className="container mt-2">
      <div className="row justify-content-center">
        {showSuccessAlert ? (
          /* render success card when order creation succeeds */
          <div className="col-12 col-md-8 col-lg-6">
            <OrderSuccessAlert />
          </div>
        ) : (
          /* render form box */
          <div className="col-md-4">
            <h4 className="display-6 text-center mb-4">
              {t("orders.registerTitle")}
            </h4>

            {user ? (
              <RegisterOrderLoggedForm
                onSuccess={() => setShowSuccessAlert(true)}
              />
            ) : (
              <RegisterOrderWithoutAccForm
                onSuccess={() => setShowSuccessAlert(true)}
              />
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default RegisterOrder;
