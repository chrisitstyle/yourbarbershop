import { useEffect, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "./AuthContext";

const OAuth2Redirect = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();
  const processed = useRef(false);

  useEffect(() => {
    if (processed.current) return;
    processed.current = true;

    const token = searchParams.get("token");

    if (token) {
      try {
        // decoding JWT token to extract user info
        const base64Url = token.split(".")[1];
        const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
        const jsonPayload = decodeURIComponent(
          atob(base64)
            .split("")
            .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
            .join(""),
        );
        const decoded = JSON.parse(jsonPayload);

        const userDataForStorage = {
          id: decoded.id,
          role: decoded.role,
          token: token,
        };

        localStorage.setItem("token", token);
        localStorage.setItem("user", JSON.stringify(userDataForStorage));

        // send data to AuthContext
        login({
          ...userDataForStorage,
          email: decoded.sub,
        });

        navigate("/");
      } catch (error) {
        console.error("Błąd dekodowania tokena OAuth2:", error);
        navigate("/login?error=invalid_token");
      }
    } else {
      navigate("/login?error=no_token");
    }
  }, [searchParams, navigate, login]);

  return (
    <div className="d-flex justify-content-center align-items-center vh-100 flex-column">
      <div className="spinner-border text-dark" role="status">
        <span className="visually-hidden">Logowanie...</span>
      </div>
      <h5 className="mt-3">Logowanie przez GitHub...</h5>
    </div>
  );
};

export default OAuth2Redirect;
