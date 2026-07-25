import { Link, NavLink, useNavigate } from "react-router-dom";
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

  const getMainNavLinkClass = ({ isActive }) =>
    `nav-link custom-nav-link${isActive ? " active" : ""}`;

  const getDropdownLinkClass = ({ isActive }) =>
    `dropdown-item app-dropdown-link${isActive ? " active" : ""}`;

  const isAdmin = user?.role === "ADMIN";
  const isUser = user?.role === "USER";

  const bookingPath = isUser ? "/registerorder" : "/registerorder";

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
          aria-label={t("nav.toggleNavigation")}
        >
          <span className="navbar-toggler-icon" />
        </button>

        <div className="collapse navbar-collapse" id="navbarNavDropdown">
          {/* Main navigation links */}
          <ul className="navbar-nav app-navbar-main">
            <li className="nav-item">
              <NavLink className={getMainNavLinkClass} to="/gallery">
                {t("nav.gallery")}
              </NavLink>
            </li>

            <li className="nav-item">
              <NavLink className={getMainNavLinkClass} to="/offers">
                {t("nav.offers")}
              </NavLink>
            </li>

            <li className="nav-item">
              <NavLink className={getMainNavLinkClass} to="/contact">
                {t("nav.contact")}
              </NavLink>
            </li>

            {/* Visible only for users and unauthenticated visitors */}
            {!isAdmin && (
              <li className="nav-item">
                <NavLink className={getMainNavLinkClass} to={bookingPath}>
                  {t("nav.bookVisit")}
                </NavLink>
              </li>
            )}
          </ul>

          {/* Account navigation */}
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
                        <NavLink
                          className={getDropdownLinkClass}
                          to="/adminpanel"
                        >
                          {t("nav.adminPanel")}
                        </NavLink>
                      </li>
                    )}

                    {isUser && user?.id && (
                      <li>
                        <NavLink
                          className={getDropdownLinkClass}
                          to={`/profile/${user.id}`}
                        >
                          {t("nav.profile")}
                        </NavLink>
                      </li>
                    )}

                    <li>
                      <hr className="dropdown-divider" />
                    </li>

                    <li>
                      <button
                        className="dropdown-item app-dropdown-link"
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
                      <NavLink className={getDropdownLinkClass} to="/login">
                        {t("nav.login")}
                      </NavLink>
                    </li>

                    <li>
                      <NavLink className={getDropdownLinkClass} to="/register">
                        {t("nav.register")}
                      </NavLink>
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
                aria-label={
                  theme === "dark" ? t("nav.switchLight") : t("nav.switchDark")
                }
                title={
                  theme === "dark" ? t("nav.switchLight") : t("nav.switchDark")
                }
              >
                {theme === "dark" ? (
                  <Sun size={22} aria-hidden="true" />
                ) : (
                  <Moon size={22} aria-hidden="true" />
                )}
              </button>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
