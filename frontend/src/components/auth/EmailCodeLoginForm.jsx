import { useState } from "react";

import { Alert } from "react-bootstrap";

import { useNavigate } from "react-router-dom";

import { useTranslation } from "react-i18next";

import { useAuth } from "../../AuthContext";

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
    if (error.response && error.response.data) {
      const data = error.response.data;

      if (typeof data === "object") {
        return Object.values(data).flat().filter(Boolean).map(String);
      }

      return [data];
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
      setSuccessMessage(
        t("auth.emailCodeSent", {
          defaultValue:
            "If the account exists, a login code has been sent to the email address.",
        }),
      );
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
          className="text-center"
        >
          {successMessage}
        </Alert>
      )}

      {errors && errors.length > 0 && (
        <Alert variant="danger" onClose={() => setErrors(null)} dismissible>
          <ul className="mb-0 ps-3 list-unstyled centered text-center">
            {errors.map((err, index) => (
              <li key={index}>{err}</li>
            ))}
          </ul>
        </Alert>
      )}
    </>
  );

  if (step === EMAIL_CODE_STEP.CODE) {
    return (
      <form onSubmit={handleVerifyCode}>
        <div className="mb-3">
          {renderAlerts()}

          <p className="text-center">
            {t("auth.emailCodeInstruction", {
              defaultValue: "Enter the verification code sent to",
            })}{" "}
            <strong>{email}</strong>
          </p>

          <label htmlFor="emailLoginCode" className="form-label">
            {t("auth.emailCode", {
              defaultValue: "Code",
            })}
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
          />
        </div>

        <ButtonSpinner
          type="submit"
          variant="dark"
          className="mx-auto d-block"
          loading={isLoading}
          loadingText={t("auth.loggingIn")}
          disabled={code.length !== 6 || isLoading}
        >
          {t("auth.continueBtn", {
            defaultValue: "Continue",
          })}
        </ButtonSpinner>

        <button
          type="button"
          className="btn btn-link w-100 mt-3"
          disabled={isLoading}
          onClick={() => handleRequestCode()}
        >
          {t("auth.resendEmail", {
            defaultValue: "Resend email",
          })}
        </button>

        <button
          type="button"
          className="btn btn-link w-100"
          disabled={isLoading}
          onClick={handleUseAnotherEmail}
        >
          {t("auth.useAnotherEmail", {
            defaultValue: "Use another email",
          })}
        </button>

        <button
          type="button"
          className="btn btn-link w-100"
          disabled={isLoading}
          onClick={onBackToPassword}
        >
          {t("auth.loginWithPassword", {
            defaultValue: "Log in with password",
          })}
        </button>
      </form>
    );
  }

  return (
    <form onSubmit={handleRequestCode}>
      <div className="mb-3">
        {renderAlerts()}

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
        />
      </div>

      <ButtonSpinner
        type="submit"
        variant="dark"
        className="mx-auto d-block"
        loading={isLoading}
        loadingText={t("auth.sendingCode", {
          defaultValue: "Sending code...",
        })}
      >
        {t("auth.continueBtn", {
          defaultValue: "Continue",
        })}
      </ButtonSpinner>

      <button
        type="button"
        className="btn btn-link w-100 mt-3"
        disabled={isLoading}
        onClick={onBackToPassword}
      >
        {t("auth.loginWithPassword", {
          defaultValue: "Log in with password",
        })}
      </button>
    </form>
  );
};

export default EmailCodeLoginForm;
