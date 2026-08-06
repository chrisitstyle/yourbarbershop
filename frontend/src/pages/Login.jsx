import { useEffect, useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContextValue";
import { toast } from "sonner";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGithub } from "@fortawesome/free-brands-svg-icons";
import { faEnvelope } from "@fortawesome/free-solid-svg-icons";
import { API_BASE_URL } from "../api/config";
import GoogleIcon from "../components/common/GoogleIcon";
import { useTranslation } from "react-i18next";
import EmailCodeLoginForm from "../components/auth/EmailCodeLoginForm";
import "../adminpages/styles/AdminForms.css";

const LOGIN_MODE = {
  PASSWORD: "password",
  EMAIL_CODE: "emailCode",
};

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [loginMode, setLoginMode] = useState(LOGIN_MODE.PASSWORD);
  const [isLoading, setIsLoading] = useState(false);

  const location = useLocation();
  const navigate = useNavigate();

  const { login } = useAuth();
  const { t } = useTranslation();

  useEffect(() => {
    // show success toast from location state if passed from navigation
    if (location.state?.message) {
      toast.success(location.state.message);

      navigate(location.pathname, {
        replace: true,
        state: null,
      });

      return;
    }

    // handle query param after registration
    const searchParams = new URLSearchParams(location.search);

    if (searchParams.get("registrationSuccess")) {
      toast.success(t("auth.successRegister"));

      navigate(location.pathname, {
        replace: true,
      });
    }
  }, [location.pathname, location.search, location.state, navigate, t]);

  const getErrorMessages = (error) => {
    // handle fetch/httpclient error format (error.data) as well as axios legacy (error.response?.data)
    const data = error?.data || error?.response?.data;

    if (data) {
      if (typeof data === "object") {
        return Object.values(data).flat().filter(Boolean).map(String);
      }

      return [String(data)];
    }

    if (error?.message) {
      return [error.message];
    }

    return [t("validation.genericError")];
  };

  const handleLogin = async (event) => {
    event.preventDefault();

    setIsLoading(true);

    try {
      await login(email, password);
      toast.success(t("auth.loginSuccess") || t("auth.loginHeader"));
      navigate("/");
    } catch (error) {
      const errorMsgs = getErrorMessages(error);
      errorMsgs.forEach((msg) => toast.error(msg));
    } finally {
      setIsLoading(false);
    }
  };

  const switchToEmailCodeLogin = () => {
    setPassword("");
    setLoginMode(LOGIN_MODE.EMAIL_CODE);
  };

  const switchToPasswordLogin = () => {
    setLoginMode(LOGIN_MODE.PASSWORD);
  };

  const renderPasswordLogin = () => (
    <form onSubmit={handleLogin}>
      <div className="mb-3">
        <label htmlFor="login" className="form-label">
          {t("auth.email")}
        </label>

        <input
          type="email"
          className="form-control"
          id="login"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          aria-describedby="emailHelp"
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
          required
          disabled={isLoading}
        />
      </div>

      {/* primary submit - matches btn-primary gold accent in dark mode */}
      <ButtonSpinner
        type="submit"
        variant="primary"
        className="w-100 py-2 fw-semibold"
        loading={isLoading}
        loadingText={t("auth.loggingIn")}
      >
        {t("auth.loginBtn")}
      </ButtonSpinner>

      {/* divider before alternative login methods */}
      <div className="d-flex align-items-center gap-2 my-3">
        <hr className="flex-grow-1 m-0" />
        <span className="text-muted small">{t("common.or")}</span>
        <hr className="flex-grow-1 m-0" />
      </div>

      {/* email code login button styled identically to github button */}
      <button
        type="button"
        className="btn btn-outline-secondary w-100 py-2 fw-semibold d-flex align-items-center justify-content-center gap-2 mb-2"
        onClick={switchToEmailCodeLogin}
        disabled={isLoading}
      >
        <FontAwesomeIcon icon={faEnvelope} size="lg" aria-hidden="true" />
        <span>{t("auth.loginWithEmailCode")}</span>
      </button>

      {/* github oauth button */}
      <a
        href={`${API_BASE_URL}/oauth2/authorization/github`}
        className="btn btn-outline-secondary w-100 py-2 fw-semibold d-flex align-items-center justify-content-center gap-2 mb-2"
      >
        <FontAwesomeIcon icon={faGithub} size="lg" aria-hidden="true" />
        <span>{t("auth.githubSign")}</span>
      </a>

      {/* google oauth button adjusted for theme compatibility */}
      <a
        href={`${API_BASE_URL}/oauth2/authorization/google`}
        className="btn btn-outline-secondary w-100 py-2 fw-semibold d-flex align-items-center justify-content-center gap-2 mb-2"
      >
        <GoogleIcon />
        <span>{t("auth.googleSign")}</span>
      </a>

      <div className="text-center mt-3">
        <Link to="/forgotpassword" className="small text-muted">
          {t("auth.forgotPassword")}
        </Link>
      </div>

      <p className="mt-2 text-center small text-muted">
        {t("auth.noAccount")}{" "}
        <Link to="/register">{t("auth.registerLink")}</Link>
      </p>
    </form>
  );

  const getHeader = () => {
    if (loginMode === LOGIN_MODE.EMAIL_CODE) {
      return t("auth.loginWithEmailCodeHeader");
    }

    return t("auth.loginHeader");
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        {/*picks up dark-theme gradient and shadow from AdminForms.css */}
        <div className="col-md-4">
          <div className="card card-accent-top">
            <div className="card-body p-4">
              <h4 className="card-title fw-bold text-center mb-4">
                {getHeader()}
              </h4>

              {loginMode === LOGIN_MODE.PASSWORD && renderPasswordLogin()}

              {loginMode === LOGIN_MODE.EMAIL_CODE && (
                <EmailCodeLoginForm
                  email={email}
                  setEmail={setEmail}
                  onBackToPassword={switchToPasswordLogin}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
