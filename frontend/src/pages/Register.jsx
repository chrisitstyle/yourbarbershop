import { useState, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";

import { registerUser } from "../api/authService";

import ButtonSpinner from "../components/common/ButtonSpinner";
import useAutoDismiss from "../hooks/useAutoDismiss";

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

  const [registerErrors, setRegisterErrors] = useAutoDismiss([], 6000);
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

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
    const data = error.response?.data;

    if (isValidationErrorResponse(data)) {
      return Object.values(data).flat().filter(Boolean).map(String);
    }

    return [t("auth.registerError")];
  };

  const resetCaptcha = () => {
    if (recaptchaRef.current) {
      recaptchaRef.current.reset();
    }

    setCaptchaToken(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setIsLoading(true);
    setRegisterErrors([]);

    if (!captchaToken) {
      setRegisterErrors([t("auth.captchaRequired")]);
      setIsLoading(false);

      return;
    }

    try {
      const response = await registerUser({
        firstname,
        lastname,
        email,
        password,
        captchaToken,
      });

      if (response.status === 200) {
        navigate("/login?registrationSuccess=true");
      }
    } catch (error) {
      resetCaptcha();

      setRegisterErrors(getRegisterErrorMessages(error));
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
              {registerErrors.length > 0 && (
                <div className="alert alert-danger text-center" role="alert">
                  <ul className="mb-0 list-unstyled">
                    {registerErrors.map((err, index) => (
                      <li key={index}>{err}</li>
                    ))}
                  </ul>
                </div>
              )}

              <label htmlFor="firstname" className="form-label">
                {t("auth.firstname")}
              </label>

              <input
                type="text"
                className="form-control"
                id="firstname"
                value={firstname}
                onChange={(e) => setFirstName(e.target.value)}
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
                onChange={(e) => setLastName(e.target.value)}
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
                onChange={(e) => setEmail(e.target.value)}
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
                onChange={(e) => setPassword(e.target.value)}
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
