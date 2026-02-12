import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../api/authService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import useAutoDismiss from "../hooks/useAutoDismiss";
import { useTranslation } from "react-i18next";

const Register = () => {
  const [firstname, setFirstName] = useState("");
  const [lastname, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [registerErrors, setRegisterErrors] = useAutoDismiss([], 6000);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { t } = useTranslation();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setRegisterErrors([]);

    try {
      const response = await registerUser({
        firstname,
        lastname,
        email,
        password,
      });

      if (response.status === 200) {
        navigate("/login?registrationSuccess=true");
      }
    } catch (error) {
      if (error.response && error.response.data) {
        const data = error.response.data;

        if (typeof data === "object") {
          const messages = Object.values(data);
          setRegisterErrors(messages);
        } else {
          setRegisterErrors([data]);
        }
      } else {
        setRegisterErrors([t("validation.genericError")]);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3 ">
            <h4 className=" display-6 text-center">
              {t("auth.registerHeader")}
            </h4>
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
                <label htmlFor="inputfirstname" className="form-label">
                  {t("auth.firstname")}
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
                  {t("auth.lastname")}
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
                  {t("auth.email")}
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
                loadingText={t("auth.registering")}
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
    </>
  );
};

export default Register;
