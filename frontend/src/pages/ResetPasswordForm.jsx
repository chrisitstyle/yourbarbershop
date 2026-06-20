import { useState, useEffect } from "react";

import { useLocation, useNavigate } from "react-router-dom";

import { userResetPasswordRequest } from "../api/userService";

import { Alert } from "react-bootstrap";

import ButtonSpinner from "../components/common/ButtonSpinner";

import { useTranslation } from "react-i18next";

const ChangePasswordForm = () => {
  const [newPassword, setNewPassword] = useState("");
  const [alert, setAlert] = useState({ message: "", variant: "" });
  const [token, setToken] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const location = useLocation();
  const navigate = useNavigate();

  const { t } = useTranslation();

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const token = searchParams.get("token");

    if (token) {
      setToken(token);
    } else {
      setAlert({
        message: t("auth.missingToken"),
        variant: "danger",
      });
    }
  }, [location, t]);

  const handleResetPasswordForm = async (e) => {
    e.preventDefault();

    if (!token) {
      setAlert({
        message: t("auth.missingToken"),
        variant: "danger",
      });

      return;
    }

    setIsLoading(true);

    try {
      await userResetPasswordRequest(token, newPassword);

      navigate("/login", {
        replace: true,
        state: {
          message: t("auth.passwordChangedSuccess"),
        },
      });
    } catch (err) {
      setAlert({
        message: t("auth.passwordChangedError"),
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
            {t("auth.changePasswordHeader")}
          </h4>

          <form onSubmit={handleResetPasswordForm}>
            <div className="mb-3">
              <label htmlFor="inputPassword" className="form-label">
                {t("auth.newPassword")}
              </label>

              <input
                type="password"
                className="form-control"
                id="inputPassword"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                aria-describedby="passwordHelp"
                required
                disabled={isLoading}
              />
            </div>

            <ButtonSpinner
              type="submit"
              variant="dark"
              className="mx-auto d-block"
              loading={isLoading}
              loadingText={t("auth.saving")}
              disabled={!token || isLoading}
            >
              {t("auth.changePasswordBtn")}
            </ButtonSpinner>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ChangePasswordForm;
