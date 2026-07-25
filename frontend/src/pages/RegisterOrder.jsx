import { useState } from "react";
import { useAuth } from "../auth/AuthContextValue";
import RegisterOrderLoggedForm from "../components/RegisterOrderLoggedForm";
import RegisterOrderWithoutAccForm from "../components/RegisterOrderWithoutAccForm";
import GuestOrderSuccessAlert from "../components/GuestOrderSuccessAlert";
import { useTranslation } from "react-i18next";

const RegisterOrder = () => {
  const { user } = useAuth();
  const { t } = useTranslation();

  // state to manage guest order success visibility
  const [showGuestSuccess, setShowGuestSuccess] = useState(false);

  return (
    <div className="container mt-2">
      <div className="row justify-content-center">
        {showGuestSuccess ? (
          /* render standalone success card when guest order succeeds */
          <div className="col-12 col-md-8 col-lg-6">
            <GuestOrderSuccessAlert />
          </div>
        ) : (
          /* render form box */
          <div className="col-md-4 border p-3">
            <h4 className="display-6 text-center mb-4">
              {t("orders.registerTitle")}
            </h4>

            {user ? (
              <RegisterOrderLoggedForm />
            ) : (
              <RegisterOrderWithoutAccForm
                onSuccess={() => setShowGuestSuccess(true)}
              />
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default RegisterOrder;
