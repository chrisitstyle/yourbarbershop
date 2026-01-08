import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

const Navbar = () => {
  const { isLoggedIn, logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const isAdmin = user?.role === "ADMIN";
  const isUser = user?.role === "USER";

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container-fluid">
        <Link className="navbar-brand" to="/">
          Barbershop
        </Link>
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNavDropdown"
          aria-controls="navbarNavDropdown"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarNavDropdown">
          <ul className="navbar-nav custom-navbar-ul">
            <li className="nav-item">
              <Link className="nav-link active custom-nav-link" to="/gallery">
                Galeria
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link active custom-nav-link" to="/offers">
                Nasza Oferta
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link active custom-nav-link" to="/contact">
                Kontakt
              </Link>
            </li>

            {/* Umów wizytę – visible only for USER and unauthenticated users */}
            {!isAdmin && (
              <li className="nav-item">
                <Link
                  className="nav-link active custom-nav-link"
                  to={
                    isUser
                      ? "/registerorderlogged"
                      : "/registerorderwithoutaccount"
                  }
                >
                  Umów wizytę
                </Link>
              </li>
            )}

            <li className="nav-item dropdown">
              <a
                className="nav-link dropdown-toggle custom-nav-link"
                role="button"
                data-bs-toggle="dropdown"
                tabIndex={0}
              >
                Konto
              </a>
              <ul className="dropdown-menu">
                {isLoggedIn ? (
                  <>
                    {isAdmin && (
                      <li>
                        <Link
                          className="dropdown-item custom-nav-link"
                          to="/adminpanel"
                        >
                          Panel administratora
                        </Link>
                      </li>
                    )}
                    {isUser && (
                      <li>
                        <Link
                          className="dropdown-item custom-nav-link"
                          to={`/profile/${user?.id}`}
                        >
                          Twój profil
                        </Link>
                      </li>
                    )}
                    <li>
                      <button
                        className="dropdown-item custom-nav-link"
                        onClick={handleLogout}
                      >
                        Wyloguj się
                      </button>
                    </li>
                  </>
                ) : (
                  <>
                    <li>
                      <Link
                        className="dropdown-item custom-nav-link"
                        to="/login"
                      >
                        Zaloguj się
                      </Link>
                    </li>
                    <li>
                      <Link
                        className="dropdown-item custom-nav-link"
                        to="/register"
                      >
                        Utwórz konto
                      </Link>
                    </li>
                  </>
                )}
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
