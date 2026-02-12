import { useState } from "react";
import { Alert } from "react-bootstrap";
import { userForgotPasswordRequest } from "../api/userService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";

const ForgotPassword = () => {
  const [email, setEmail] = useState("");
  const [alert, setAlert] = useState({ message: "", variant: "" });
  const [isLoading, setIsLoading] = useState(false);
  const { t } = useTranslation();

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await userForgotPasswordRequest(email);
      setAlert({
        message: t("auth.resetLinkSent"),
        variant: "success",
      });
    } catch (err) {
      setAlert({
        message: t("auth.requestError"),
        variant: "danger",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCloseAlert = () => {
    setAlert({ message: "", variant: "" });
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-4 border p-3">
          {alert.message && (
            <Alert
              variant={alert.variant}
              onClose={handleCloseAlert}
              dismissible
              className="text-center"
            >
              {alert.message}
            </Alert>
          )}
          <h4 className="display-6 text-center">
            {t("auth.forgotPasswordHeader")}
          </h4>
          <form onSubmit={handleForgotPassword}>
            <div className="mb-3">
              <label htmlFor="inputEmail" className="form-label">
                {t("auth.email")}
              </label>
              <input
                type="email"
                className="form-control"
                id="inputEmail"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                aria-describedby="emailHelp"
                required
                disabled={isLoading}
              />
            </div>
            <ButtonSpinner
              type="submit"
              variant="dark"
              className="mx-auto d-block"
              loading={isLoading}
              loadingText={t("auth.sending")}
            >
              {t("auth.send")}
            </ButtonSpinner>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
