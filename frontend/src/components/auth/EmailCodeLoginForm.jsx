import { useState } from "react";
import { Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../auth/AuthContextValue";
import { requestEmailLoginCode } from "../../api/authService";
import ButtonSpinner from "../common/ButtonSpinner";
import useAutoDismiss from "../../hooks/useAutoDismiss";

const EMAIL_CODE_STEP = {
  EMAIL: "email",
  CODE: "code",
};

const EmailCodeLoginForm = ({ email, setEmail, onBackToPassword }) => {
  const [step, setStep] = useState(EMAIL_CODE_STEP.EMAIL);
  const [code, setCode] = useState("");
  const [successMessage, setSuccessMessage] = useAutoDismiss(null, 5000);
  const [errors, setErrors] = useAutoDismiss(null, 6000);
  const [isLoading, setIsLoading] = useState(false);

  const { loginWithEmailCode } = useAuth();

  const navigate = useNavigate();

  const { t } = useTranslation();

  const getErrorMessages = (error) => {
    const data = error?.data || error?.response?.data;

    if (data) {
      if (typeof data === "object") {
        const messages = Object.values(data).flat().filter(Boolean).map(String);

        return [...new Set(messages)];
      }

      return [String(data)];
    }

    if (error?.message) {
      return [error.message];
    }

    return [t("validation.genericError")];
  };

  const handleRequestCode = async (event) => {
    event?.preventDefault();

    setIsLoading(true);
    setErrors(null);
    setSuccessMessage(null);

    try {
      await requestEmailLoginCode(email);

      setStep(EMAIL_CODE_STEP.CODE);
      setSuccessMessage(t("auth.emailCodeSent"));
    } catch (error) {
      setErrors(getErrorMessages(error));
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyCode = async (event) => {
    event.preventDefault();

    setIsLoading(true);
    setErrors(null);

    try {
      await loginWithEmailCode(email, code);

      navigate("/");
    } catch (error) {
      setErrors(getErrorMessages(error));
    } finally {
      setIsLoading(false);
    }
  };

  const handleCodeChange = (event) => {
    setCode(event.target.value.replace(/\D/g, ""));
  };

  const handleUseAnotherEmail = () => {
    setErrors(null);
    setSuccessMessage(null);
    setStep(EMAIL_CODE_STEP.EMAIL);
    setCode("");
  };

  const renderAlerts = () => (
    <>
      {successMessage && (
        <Alert
          variant="success"
          onClose={() => setSuccessMessage(null)}
          dismissible
          className="text-center mb-3"
        >
          {successMessage}
        </Alert>
      )}

      {errors?.length > 0 && (
        <Alert
          variant="danger"
          onClose={() => setErrors(null)}
          dismissible
          className="mb-3"
        >
          <ul className="mb-0 ps-0 list-unstyled text-center">
            {errors.map((error) => (
              <li key={error}>{error}</li>
            ))}
          </ul>
        </Alert>
      )}
    </>
  );

  if (step === EMAIL_CODE_STEP.CODE) {
    return (
      <form onSubmit={handleVerifyCode}>
        {renderAlerts()}

        <p className="text-muted small mb-3">
          {t("auth.emailCodeInstruction")}{" "}
          <strong className="text-body">{email}</strong>
        </p>

        <div className="mb-3">
          <label htmlFor="emailLoginCode" className="form-label">
            {t("auth.emailCode")}
          </label>

          <input
            type="text"
            className="form-control text-center"
            id="emailLoginCode"
            inputMode="numeric"
            autoComplete="one-time-code"
            maxLength={6}
            value={code}
            onChange={handleCodeChange}
            required
            disabled={isLoading}
          />
        </div>

        <ButtonSpinner
          type="submit"
          variant="primary"
          className="w-100 py-2 fw-semibold"
          loading={isLoading}
          loadingText={t("auth.loggingIn")}
          disabled={code.length !== 6 || isLoading}
        >
          {t("auth.continueBtn")}
        </ButtonSpinner>

        <div className="d-flex flex-column align-items-center gap-2 mt-3">
          <button
            type="button"
            className="btn btn-link p-0 text-muted small"
            disabled={isLoading}
            onClick={() => handleRequestCode()}
          >
            {t("auth.resendEmail")}
          </button>

          <button
            type="button"
            className="btn btn-link p-0 text-muted small"
            disabled={isLoading}
            onClick={handleUseAnotherEmail}
          >
            {t("auth.useAnotherEmail")}
          </button>

          <button
            type="button"
            className="btn btn-link p-0 text-muted small"
            disabled={isLoading}
            onClick={onBackToPassword}
          >
            {t("auth.loginWithPassword")}
          </button>
        </div>
      </form>
    );
  }

  return (
    <form onSubmit={handleRequestCode}>
      {renderAlerts()}

      <p className="text-muted small mb-3">{t("auth.emailCodeDescription")}</p>

      <div className="mb-3">
        <label htmlFor="emailCodeLoginEmail" className="form-label">
          {t("auth.email")}
        </label>

        <input
          type="email"
          className="form-control"
          id="emailCodeLoginEmail"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          autoComplete="email"
          required
          disabled={isLoading}
        />
      </div>

      <ButtonSpinner
        type="submit"
        variant="primary"
        className="w-100 py-2 fw-semibold mb-3"
        loading={isLoading}
        loadingText={t("auth.sendingCode")}
        disabled={isLoading}
      >
        {t("auth.continueBtn")}
      </ButtonSpinner>

      <button
        type="button"
        className="btn btn-link p-0 text-muted small"
        disabled={isLoading}
        onClick={onBackToPassword}
      >
        {t("auth.loginWithPassword")}
      </button>
    </form>
  );
};

export default EmailCodeLoginForm;
