import { useState, useRef } from "react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { userForgotPasswordRequest } from "../api/userService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";
import ReCAPTCHA from "react-google-recaptcha";
import { RECAPTCHA_SITE_KEY } from "../api/config";

const ForgotPassword = () => {
  const [email, setEmail] = useState("");
  const [submittedEmail, setSubmittedEmail] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [emailSent, setEmailSent] = useState(false);

  const { t } = useTranslation();

  const [captchaToken, setCaptchaToken] = useState(null);
  const recaptchaRef = useRef(null);

  const handleCaptchaChange = (token) => {
    setCaptchaToken(token);
  };

  const resetCaptcha = () => {
    if (recaptchaRef.current) {
      recaptchaRef.current.reset();
    }

    setCaptchaToken(null);
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();

    if (!captchaToken) {
      toast.error(t("auth.captchaRequired"));
      return;
    }

    setIsLoading(true);

    try {
      await userForgotPasswordRequest(email, captchaToken);

      setSubmittedEmail(email);
      setEmail("");
      setEmailSent(true);
      toast.success(t("auth.resetLinkSent"));
      resetCaptcha();
    } catch {
      resetCaptcha();
      toast.error(t("auth.requestError"));
    } finally {
      setIsLoading(false);
    }
  };

  const handleResendResetLink = async () => {
    if (!captchaToken) {
      toast.error(t("auth.captchaRequired"));
      return;
    }

    setIsLoading(true);

    try {
      await userForgotPasswordRequest(submittedEmail, captchaToken);
      toast.success(t("auth.resetLinkResent"));
      resetCaptcha();
    } catch {
      resetCaptcha();
      toast.error(t("auth.requestError"));
    } finally {
      setIsLoading(false);
    }
  };

  if (emailSent) {
    return (
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3 text-center">
            <p className="mb-3">{t("auth.checkEmailForResetLink")}</p>

            <div className="border-top pt-3 mt-3">
              <p className="mb-3 fw-semibold">
                {t("auth.resetLinkNotArrived")}
              </p>

              <div className="mb-3 d-flex justify-content-center">
                <ReCAPTCHA
                  ref={recaptchaRef}
                  sitekey={RECAPTCHA_SITE_KEY}
                  onChange={handleCaptchaChange}
                />
              </div>

              <ButtonSpinner
                type="button"
                variant="dark"
                className="mx-auto d-block"
                loading={isLoading}
                loadingText={t("auth.resendingResetLink")}
                disabled={!captchaToken || isLoading}
                onClick={handleResendResetLink}
              >
                {t("auth.resendResetLink")}
              </ButtonSpinner>
            </div>

            <Link to="/login" className="btn btn-dark mt-3">
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
                autoComplete="email"
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
