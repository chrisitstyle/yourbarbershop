import { useState, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContextValue";
import { toast } from "sonner";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";
import ReCAPTCHA from "react-google-recaptcha";
import { RECAPTCHA_SITE_KEY } from "../api/config";

const Register = () => {
  const [firstname, setFirstName] = useState("");
  const [lastname, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [captchaToken, setCaptchaToken] = useState(null);
  const recaptchaRef = useRef(null);

  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();
  const { register } = useAuth();
  const { t } = useTranslation();

  const handleCaptchaChange = (token) => {
    setCaptchaToken(token);
  };

  const isValidationErrorResponse = (data) => {
    if (!data || typeof data !== "object" || Array.isArray(data)) {
      return false;
    }

    const backendErrorKeys = [
      "message",
      "error",
      "status",
      "timestamp",
      "path",
    ];

    return !backendErrorKeys.some((key) => key in data);
  };

  const getRegisterErrorMessages = (error) => {
    const data = error?.data || error?.response?.data;

    if (isValidationErrorResponse(data)) {
      return Object.values(data).flat().filter(Boolean).map(String);
    }

    if (typeof data === "string") {
      return [data];
    }

    if (error?.message) {
      return [error.message];
    }

    return [t("auth.registerError")];
  };

  const resetCaptcha = () => {
    if (recaptchaRef.current) {
      recaptchaRef.current.reset();
    }

    setCaptchaToken(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setIsLoading(true);

    if (!captchaToken) {
      toast.error(t("auth.captchaRequired"));
      setIsLoading(false);

      return;
    }

    try {
      await register({
        firstname,
        lastname,
        email,
        password,
        captchaToken,
      });

      // trigger success toast and redirect to login page
      toast.success(t("auth.successRegister"));
      navigate("/login");
    } catch (error) {
      resetCaptcha();
      const errors = getRegisterErrorMessages(error);
      errors.forEach((msg) => toast.error(msg));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-4 border p-3">
          <h4 className="display-6 text-center">{t("auth.registerHeader")}</h4>

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label htmlFor="firstname" className="form-label">
                {t("auth.firstname")}
              </label>

              <input
                type="text"
                className="form-control"
                id="firstname"
                value={firstname}
                onChange={(event) => setFirstName(event.target.value)}
                autoComplete="given-name"
                required
                disabled={isLoading}
              />
            </div>

            <div className="mb-3">
              <label htmlFor="lastname" className="form-label">
                {t("auth.lastname")}
              </label>

              <input
                type="text"
                className="form-control"
                id="lastname"
                value={lastname}
                onChange={(event) => setLastName(event.target.value)}
                autoComplete="family-name"
                required
                disabled={isLoading}
              />
            </div>

            <div className="mb-3">
              <label htmlFor="email" className="form-label">
                {t("auth.email")}
              </label>

              <input
                type="email"
                className="form-control"
                id="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                autoComplete="email"
                required
                disabled={isLoading}
              />
            </div>

            <div className="mb-3">
              <label htmlFor="password" className="form-label">
                {t("auth.password")}
              </label>

              <input
                type="password"
                className="form-control"
                id="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="new-password"
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
              loadingText={t("auth.registering")}
              disabled={!captchaToken || isLoading}
            >
              {t("auth.registerBtn")}
            </ButtonSpinner>

            <p className="mt-3 text-center">
              {t("auth.hasAccount")}{" "}
              <Link to="/login">{t("auth.loginLink")}</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Register;
