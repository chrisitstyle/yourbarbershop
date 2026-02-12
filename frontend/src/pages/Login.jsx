import { useEffect, useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { Alert } from "react-bootstrap";
import { loginUser } from "../api/authService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGithub } from "@fortawesome/free-brands-svg-icons";
import { API_BASE_URL } from "../api/config";
import GoogleIcon from "../components/common/GoogleIcon";
import useAutoDismiss from "../hooks/useAutoDismiss";
import { useTranslation } from "react-i18next";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const registrationSuccess = searchParams.get("registrationSuccess");

  const [successMessage, setSuccessMessage] = useAutoDismiss(null, 5000);
  const [loginErrors, setLoginErrors] = useAutoDismiss(null, 6000);
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();
  const { login } = useAuth();
  const { t } = useTranslation();

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);

    // if ?registrationSuccess=true is in the URL
    if (searchParams.get("registrationSuccess")) {
      // set the message in state (this triggers the timer from useAutoDismiss)
      setSuccessMessage(t("auth.successRegister"));

      // clean up the URL so the parameter doesn't linger in the address bar
      // replace: true ensures the user cannot navigate back to the URL with the parameter
      navigate(location.pathname, { replace: true });
    }
  }, [location, setSuccessMessage, navigate, t]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setLoginErrors(null);

    try {
      const userData = await loginUser(email, password);
      localStorage.setItem("token", userData.token);
      login(userData);
      navigate("/");
    } catch (error) {
      if (error.response && error.response.data) {
        const data = error.response.data;
        if (typeof data === "object") {
          setLoginErrors(Object.values(data));
        } else {
          setLoginErrors([data]);
        }
      } else {
        setLoginErrors([t("validation.genericError")]);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-4 border p-3 ">
          <h4 className="display-6 text-center">{t("auth.loginHeader")}</h4>
          <form onSubmit={handleLogin}>
            <div className="mb-3">
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
              {loginErrors && loginErrors.length > 0 && (
                <Alert
                  variant="danger"
                  onClose={() => setLoginErrors(null)}
                  dismissible
                >
                  <ul className="mb-0 ps-3 list-unstyled centered text-center">
                    {loginErrors.map((err, index) => (
                      <li key={index}>{err}</li>
                    ))}
                  </ul>
                </Alert>
              )}

              <label htmlFor="inputEmail" className="form-label">
                {t("auth.email")}
              </label>
              <input
                type="email"
                className="form-control"
                id="login"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                aria-describedby="emailHelp"
                required
              />
            </div>
            <div className="mb-3">
              <label htmlFor="inputPassword" className="form-label">
                {t("auth.password")}
              </label>
              <input
                type="password"
                className="form-control"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <ButtonSpinner
              type="submit"
              variant="dark"
              className="mx-auto d-block"
              loading={isLoading}
              loadingText={t("auth.loggingIn")}
            >
              {t("auth.loginBtn")}
            </ButtonSpinner>

            {/* OAUTH2 */}
            <div className="text-center mt-3">
              {/* GITHUB BUTTON */}
              <a
                href={`${API_BASE_URL}/oauth2/authorization/github`}
                className="btn btn-dark w-100 py-2 fw-bold position-relative d-flex align-items-center"
                style={{ borderRadius: "6px" }}
              >
                <FontAwesomeIcon
                  icon={faGithub}
                  className="position-absolute start-0 ms-3"
                  size="xl"
                  style={{ color: "#ffffff" }}
                />

                <span className="w-100 text-center">
                  {t("auth.githubSign")}
                </span>
              </a>
              {/* GOOGLE BUTTON */}
              <a
                href={`${API_BASE_URL}/oauth2/authorization/google`}
                className="btn btn-light w-100 py-2 fw-bold position-relative d-flex align-items-center mt-2"
                style={{
                  borderRadius: "6px",
                  border: "1px solid #dadce0",
                  color: "#3c4043",
                  backgroundColor: "#ffffff",
                }}
              >
                <GoogleIcon className="position-absolute start-0 ms-3" />

                <span className="w-100 text-center">
                  {t("auth.googleSign")}
                </span>
              </a>
            </div>
            {/* --------------------------- */}

            <p className="mt-3 text-center">
              <Link to="/forgotpassword">{t("auth.forgotPassword")}</Link>
            </p>
            <p className=" mt-3 text-center">
              {t("auth.noAccount")}{" "}
              <Link to="/register">{t("auth.registerLink")}</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;
