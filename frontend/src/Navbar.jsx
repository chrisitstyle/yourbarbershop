import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "./auth/AuthContextValue";
import { Sun, Moon } from "lucide-react";
import { useTranslation } from "react-i18next";
import LanguageSwitcher from "./components/common/LanguageSwitcher";
import "./css/Navbar.css";

const Navbar = ({ theme, onToggleTheme }) => {
  const { isLoggedIn, logout, user } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  const isAdmin = user?.role === "ADMIN";
  const isUser = user?.role === "USER";

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark app-navbar">
      <div className="container-fluid">
        <Link className="navbar-brand fw-semibold" to="/">
          YourBarbershop
        </Link>

        <button
          className="navbar-toggler app-navbar-toggler"
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
          {/* Main navigation links left */}
          <ul className="navbar-nav app-navbar-main">
            <li className="nav-item">
              <Link className="nav-link active custom-nav-link" to="/gallery">
                {t("nav.gallery")}
              </Link>
            </li>

            <li className="nav-item">
              <Link className="nav-link active custom-nav-link" to="/offers">
                {t("nav.offers")}
              </Link>
            </li>

            <li className="nav-item">
              <Link className="nav-link active custom-nav-link" to="/contact">
                {t("nav.contact")}
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
                  {t("nav.bookVisit")}
                </Link>
              </li>
            )}
          </ul>

          {/* Account navigation right */}
          <ul className="navbar-nav ms-lg-auto app-navbar-actions">
            <li className="nav-item dropdown app-navbar-account">
              <button
                className="nav-link dropdown-toggle custom-nav-link app-account-button"
                type="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                {t("nav.account")}
              </button>

              <ul className="dropdown-menu dropdown-menu-lg-end app-account-menu">
                {isLoggedIn ? (
                  <>
                    {isAdmin && (
                      <li>
                        <Link
                          className="dropdown-item custom-nav-link"
                          to="/adminpanel"
                        >
                          {t("nav.adminPanel")}
                        </Link>
                      </li>
                    )}

                    {isUser && (
                      <li>
                        <Link
                          className="dropdown-item custom-nav-link"
                          to={`/profile/${user?.id}`}
                        >
                          {t("nav.profile")}
                        </Link>
                      </li>
                    )}

                    <li>
                      <hr className="dropdown-divider" />
                    </li>

                    <li>
                      <button
                        className="dropdown-item custom-nav-link"
                        type="button"
                        onClick={handleLogout}
                      >
                        {t("nav.logout")}
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
                        {t("nav.login")}
                      </Link>
                    </li>

                    <li>
                      <Link
                        className="dropdown-item custom-nav-link"
                        to="/register"
                      >
                        {t("nav.register")}
                      </Link>
                    </li>
                  </>
                )}
              </ul>
            </li>

            <li className="nav-item app-navbar-controls">
              <div className="app-language-switcher">
                <LanguageSwitcher />
              </div>

              <button
                type="button"
                className="app-theme-toggle"
                onClick={onToggleTheme}
                title={
                  theme === "dark" ? t("nav.switchLight") : t("nav.switchDark")
                }
              >
                {theme === "dark" ? <Sun size={22} /> : <Moon size={22} />}
              </button>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
