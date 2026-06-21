import { useState, useRef } from "react";

import { Alert } from "react-bootstrap";
import { Link } from "react-router-dom";

import { userForgotPasswordRequest } from "../api/userService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";

import ReCAPTCHA from "react-google-recaptcha";

import { RECAPTCHA_SITE_KEY } from "../api/config";

const ForgotPassword = () => {
  const [email, setEmail] = useState("");
  const [alert, setAlert] = useState({ message: "", variant: "" });
  const [isLoading, setIsLoading] = useState(false);
  const [emailSent, setEmailSent] = useState(false);

  const { t } = useTranslation();

  const [captchaToken, setCaptchaToken] = useState(null);
  const recaptchaRef = useRef(null);

  const handleCaptchaChange = (token) => {
    setCaptchaToken(token);
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();

    if (!captchaToken) {
      setAlert({
        message: t("auth.captchaRequired") || "Proszę rozwiązać CAPTCHA!",
        variant: "danger",
      });

      return;
    }

    setIsLoading(true);

    try {
      await userForgotPasswordRequest(email, captchaToken);

      setEmail("");
      setEmailSent(true);
    } catch (err) {
      if (recaptchaRef.current) {
        recaptchaRef.current.reset();
        setCaptchaToken(null);
      }

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

  if (emailSent) {
    return (
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3 text-center">
            <Alert variant="success" className="text-center">
              {t("auth.resetLinkSent")}
            </Alert>

            <p className="mb-3">{t("auth.checkEmailForResetLink")}</p>

            <Link to="/login" className="btn btn-dark">
              {t("auth.loginBtn")}
            </Link>
          </div>
        </div>
      </div>
    );
  }

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

            <div className="mb-3 d-flex justify-content-center">
              <ReCAPTCHA
                ref={recaptchaRef}
                sitekey={RECAPTCHA_SITE_KEY}
                onChange={handleCaptchaChange}
              />
            </div>

            <ButtonSpinner
              type="submit"
              variant="dark"
              className="mx-auto d-block"
              loading={isLoading}
              loadingText={t("auth.sending")}
              disabled={!captchaToken || isLoading}
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
