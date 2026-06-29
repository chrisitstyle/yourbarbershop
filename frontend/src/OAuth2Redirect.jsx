import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

const OAuth2Redirect = () => {
  const navigate = useNavigate();
  const { refreshAuth } = useAuth();
  const processed = useRef(false);

  useEffect(() => {
    const finishOAuthLogin = async () => {
      if (processed.current) return;

      processed.current = true;

      try {
        await refreshAuth();
        navigate("/");
      } catch (error) {
        console.error("Błąd logowania OAuth2:", error);
        navigate("/login?error=social_login_failed");
      }
    };

    finishOAuthLogin();
  }, [navigate, refreshAuth]);

  return (
    <div className="d-flex justify-content-center align-items-center vh-100 flex-column">
      <div className="spinner-border text-dark" role="status">
        <span className="visually-hidden">Logowanie...</span>
      </div>

      <h5 className="mt-3">Logowanie...</h5>
    </div>
  );
};

export default OAuth2Redirect;
