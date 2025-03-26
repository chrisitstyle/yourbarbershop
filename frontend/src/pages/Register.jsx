import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../api/authService";

const Register = () => {
  const [firstname, setFirstName] = useState("");
  const [lastname, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [registerError, setRegisterError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await registerUser({
        firstname,
        lastname,
        email,
        password,
      });

      if (response.status === 200) {
        navigate("/login?registrationSuccess=true");
      } else {
        setRegisterError("Błąd przy zakładaniu konta");
      }
    } catch (error) {
      setRegisterError("Błąd przy zakładaniu konta");
    }
  };

  return (
    <>
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3 ">
            <h4 className=" display-6 text-center">Rejestracja konta</h4>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                {registerError && (
                  <div className="alert alert-danger text-center" role="alert">
                    {registerError}
                  </div>
                )}
                <label htmlFor="inputfirstname" className="form-label">
                  Imie
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="firstname"
                  value={firstname}
                  onChange={(e) => setFirstName(e.target.value)}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="inputlastname" className="form-label">
                  Nazwisko
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="lastname"
                  value={lastname}
                  onChange={(e) => setLastName(e.target.value)}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="inputemail" className="form-label">
                  Email
                </label>
                <input
                  type="email"
                  className="form-control"
                  id="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="inputpassword" className="form-label">
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

              <button type="submit" className="btn btn-dark mx-auto d-block">
                Załóż konto
              </button>
              <p className="mt-3 text-center">
                Masz konto? <Link to="/login">Zaloguj się</Link>
              </p>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default Register;
