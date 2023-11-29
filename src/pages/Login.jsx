import React, { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { Alert } from "react-bootstrap";
import { loginUser } from "../api/api";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const registrationSuccess = searchParams.get("registrationSuccess");
  const [loginError, setLoginError] = useState(null);

  const navigate = useNavigate();
  const { login } = useAuth();

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      const userData = await loginUser(email, password);

      localStorage.setItem("token", userData.token);

      login(userData);

      navigate("/");
    } catch (error) {
      setLoginError("Nie udało się zalogować");
    }
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-4 border p-3 ">
          <h4 className="text-center">Logowanie</h4>
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
              <div id="emailHelp" className="form-text">
                Dane są zabezpieczane przed nieautoryzowanym dostępem
              </div>
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
            <div className="mb-3 form-check">
              <input
                type="checkbox"
                className="form-check-input"
                id="checkbox"
              />
              <label className="form-check-label" htmlFor="inputCheckBox">
                Checkbox
              </label>
            </div>
            <button type="submit" className="btn btn-primary mx-auto d-block">
              Zaloguj
            </button>
            <p className="mt-3 text-center">
              Nie masz konta? <Link to="/register">Zarejestruj się</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;
