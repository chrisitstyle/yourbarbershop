import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "./AuthContext";
import { Sun, Moon } from "lucide-react";
import { useTranslation } from "react-i18next";
import LanguageSwitcher from "./components/common/LanguageSwitcher";

const Navbar = ({ theme, onToggleTheme }) => {
  const { isLoggedIn, logout, user } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();

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
          YourBarbershop
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
          {/* Main navigation links left */}
          <ul className="navbar-nav">
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
          <ul className="navbar-nav ms-auto align-items-center">
            <li className="nav-item dropdown">
              <a
                className="nav-link dropdown-toggle custom-nav-link"
                role="button"
                data-bs-toggle="dropdown"
                tabIndex={0}
              >
                {t("nav.account")}
              </a>
              <ul className="dropdown-menu dropdown-menu-end">
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

            <li className="nav-item ms-3 me-2">
              <LanguageSwitcher />
            </li>

            <li className="nav-item d-flex align-items-center">
              <button
                className="bg-transparent border-0 ms-2 p-0"
                onClick={onToggleTheme}
                title={
                  theme === "dark" ? t("nav.switchLight") : t("nav.switchDark")
                }
              >
                {theme === "dark" ? <Sun /> : <Moon color="#fff" />}
              </button>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
