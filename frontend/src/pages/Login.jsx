import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { Alert } from "react-bootstrap";
import { loginUser } from "../api/authService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGithub } from "@fortawesome/free-brands-svg-icons";
import { API_BASE_URL } from "../api/config";
import GoogleIcon from "../components/common/GoogleIcon";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const registrationSuccess = searchParams.get("registrationSuccess");
  const [loginError, setLoginError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();
  const { login } = useAuth();

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      const userData = await loginUser(email, password);

      localStorage.setItem("token", userData.token);

      login(userData);

      navigate("/");
    } catch (error) {
      setLoginError("Błąd logowania");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-4 border p-3 ">
          <h4 className="display-6 text-center">Logowanie</h4>
          <form onSubmit={handleLogin}>
            <div className="mb-3">
              {registrationSuccess && (
                <Alert
                  variant="success"
                  onClose={() => {}}
                  dismissible
                  className="text-center"
                >
                  Konto zostało pomyślnie zarejestrowane. Zaloguj się.
                </Alert>
              )}
              {loginError && (
                <Alert
                  variant="danger"
                  onClose={() => setLoginError(null)}
                  dismissible
                  className="text-center"
                >
                  {loginError}
                </Alert>
              )}

              <label htmlFor="inputEmail" className="form-label">
                Adres e-mail
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
                Hasło
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
              loadingText="Logowanie..."
            >
              Zaloguj
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

                <span className="w-100 text-center">Sign in with Github</span>
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

                <span className="w-100 text-center">Sign in with Google</span>
              </a>
            </div>
            {/* --------------------------- */}

            <p className="mt-3 text-center">
              <Link to="/forgotpassword">Zapomniałem hasła</Link>
            </p>
            <p className=" mt-3 text-center">
              Nie masz konta? <Link to="/register">Zarejestruj się</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;
