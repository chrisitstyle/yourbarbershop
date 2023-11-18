import React from "react";
import { useState, useEffect } from "react";
import axios from "axios";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../AuthContext";
const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loggedIn, setLoggedIn] = useState(false);
  const [loginError, setLoginError] = useState(null);
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const registrationSuccess = searchParams.get("registrationSuccess");

  const navigate = useNavigate();
  const { login } = useAuth(); // Użyj hooka do uzyskania dostępu do funkcji login z kontekstu

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      // Wysyłanie zapytania POST do backendu
      const response = await axios.post("http://localhost:8080/login", {
        email,
        password,
      });

      // Obsługa odpowiedzi od backendu (np. zapisanie tokena, przekierowanie itp.)
      const user = response.data;
      const token = response.data.token;

      // Zapisanie tokenu w localStorage
      localStorage.setItem("token", token);

      console.log("Udane logowanie", user);

      // Ustawienie stanu zalogowania w kontekście na true
      login();

      // Przekierowanie na stronę główną po zalogowaniu
      navigate("/");
    } catch (error) {
      // Obsługa błędów logowania
      console.error("Błąd logowania", error.response.data);
      setLoginError("Nieprawidłowy adres e-mail lub hasło");
    }
  };
  return (
    <>
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3 ">
            <form onSubmit={handleLogin}>
              <div className="mb-3">
                {loginError && (
                  <div className="alert alert-danger" role="alert">
                    {loginError}
                  </div>
                )}
                {registrationSuccess && (
                  <div className="alert alert-success" role="alert">
                    Konto zostało pomyślnie zarejestrowane. Zaloguj się.
                  </div>
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
                <label htmlFor="exampleInputPassword1" className="form-label">
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
                <label className="form-check-label" htmlFor="exampleCheck1">
                  Checkbox
                </label>
              </div>
              <button type="submit" className="btn btn-primary">
                Zaloguj
              </button>
              <p className="mt-3">
                Nie masz konta? <Link to="/register">Zarejestruj się</Link>
              </p>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default Login;
