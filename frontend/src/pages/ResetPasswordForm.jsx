import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import { userResetPasswordRequest } from "../api/userService";
import { Alert } from "react-bootstrap";
import ButtonSpinner from "../components/common/ButtonSpinner";
const ChangePasswordForm = () => {
  const [newPassword, setNewPassword] = useState("");
  const [alert, setAlert] = useState({ message: "", variant: "" });
  const location = useLocation();
  const [token, setToken] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const token = searchParams.get("token");
    if (token) {
      setToken(token);
    } else {
      setAlert({
        message: "Brak tokena resetowania hasła w URL.",
        variant: "danger",
      });
    }
  }, [location]);

  const handleResetPasswordForm = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await userResetPasswordRequest(token, newPassword);
      setAlert({
        message: "Hasło zostało pomyślnie zmienione.",
        variant: "success",
      });
      setNewPassword("");
    } catch (err) {
      setAlert({
        message: "Wystąpił błąd podczas zmiany hasła.",
        variant: "danger",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCloseAlert = () => {
    setAlert({ message: "", variant: "" });
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-4 border p-3">
          {alert.message && (
            <Alert
              variant={alert.variant}
              onClose={handleCloseAlert}
              dismissible
              className="text-center"
            >
              {alert.message}
            </Alert>
          )}
          <h4 className="display-6 text-center">Zmiana hasła</h4>
          <form onSubmit={handleResetPasswordForm}>
            <div className="mb-3">
              <label htmlFor="inputPassword" className="form-label">
                Nowe hasło
              </label>
              <input
                type="password"
                className="form-control"
                id="inputPassword"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                aria-describedby="passwordHelp"
                required
                disabled={isLoading}
              />
            </div>
            <ButtonSpinner
              type="submit"
              variant="dark"
              className="mx-auto d-block"
              loading={isLoading}
              loadingText="Zapisywanie..."
            >
              Zmień hasło
            </ButtonSpinner>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ChangePasswordForm;
