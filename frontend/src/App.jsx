import { Suspense, lazy, useCallback, useEffect, useState } from "react";
import {
  BrowserRouter as Router,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";
import { Toaster } from "sonner";
import { AuthProvider } from "./auth/AuthContext.jsx";

// eager: shell + landing (needed immediately)
import Navbar from "./Navbar.jsx";
import Footer from "./Footer.jsx";
import Home from "./pages/Home.jsx";

// lazy: other pages split into separate chunks -> faster initial render
const Login = lazy(() => import("./pages/Login.jsx"));
const Register = lazy(() => import("./pages/Register.jsx"));
const Gallery = lazy(() => import("./pages/Gallery.jsx"));
const Offer = lazy(() => import("./pages/Offer.jsx"));
const Contact = lazy(() => import("./pages/Contact.jsx"));
const RegisterOrder = lazy(() => import("./pages/RegisterOrder.jsx"));
const Profile = lazy(() => import("./pages/Profile.jsx"));
const ForgotPassword = lazy(() => import("./pages/ForgotPassword.jsx"));
const ResetPasswordForm = lazy(() => import("./pages/ResetPasswordForm.jsx"));
const OAuth2Redirect = lazy(() => import("./OAuth2Redirect.jsx"));
const PaymentSuccess = lazy(() => import("./pages/PaymentSuccess.jsx"));
const PaymentCancel = lazy(() => import("./pages/PaymentCancel.jsx"));
const NotFound = lazy(() => import("./pages/NotFound.jsx"));

// admin panel loaded only for admins
const AdminPanel = lazy(() => import("./adminpages/AdminPanel.jsx"));
const EditOffer = lazy(() => import("./adminpages/EditOffer.jsx"));
const EditUser = lazy(() => import("./adminpages/EditUser.jsx"));
const EditOrder = lazy(() => import("./adminpages/EditOrder.jsx"));
const EditGuestOrder = lazy(() => import("./adminpages/EditGuestOrder.jsx"));

// route guards
import AdminRoute from "./routes/AdminRoute";
import UserRoute from "./routes/UserRoute";

const THEME_KEY = "theme";

function getInitialTheme() {
  const stored = localStorage.getItem(THEME_KEY);
  if (stored === "light" || stored === "dark") return stored;
  // on first visit use system preference
  return window.matchMedia?.("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light";
}

// scroll to top on every path change
function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: "instant" });
  }, [pathname]);
  return null;
}

// fallback displayed while loading a lazy chunk
function RouteFallback() {
  return (
    <div
      className="d-flex justify-content-center align-items-center py-5"
      style={{ minHeight: "50vh" }}
      role="status"
      aria-live="polite"
    >
      <div className="spinner-border" aria-hidden="true" />
      <span className="visually-hidden">Loading…</span>
    </div>
  );
}

function App() {
  const [theme, setTheme] = useState(getInitialTheme);

  useEffect(() => {
    document.documentElement.dataset.bsTheme = theme;
    localStorage.setItem(THEME_KEY, theme);
  }, [theme]);

  const handleToggleTheme = useCallback(() => {
    setTheme((prev) => (prev === "dark" ? "light" : "dark"));
  }, []);

  return (
    <Router>
      <AuthProvider>
        <div className="App d-flex flex-column min-vh-100">
          {/* sonner toast notification container */}
          <Toaster
            theme={theme}
            richColors
            position="bottom-right"
            closeButton
            toastOptions={{
              style: { borderRadius: "6px", fontSize: "0.9rem" },
            }}
          />

          <ScrollToTop />
          <Navbar theme={theme} onToggleTheme={handleToggleTheme} />

          <main className="flex-grow-1" id="main-content">
            <Suspense fallback={<RouteFallback />}>
              <Routes>
                <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="/forgotpassword" element={<ForgotPassword />} />
                <Route path="/resetpassword" element={<ResetPasswordForm />} />
                <Route path="/register" element={<Register />} />
                <Route path="/gallery" element={<Gallery />} />
                <Route path="/offers" element={<Offer />} />
                <Route path="/contact" element={<Contact />} />

                {/* unified order registration route for users and guests */}
                <Route path="/registerorder" element={<RegisterOrder />} />

                <Route element={<AdminRoute />}>
                  <Route path="/adminpanel" element={<AdminPanel />} />
                  <Route
                    path="/adminpanel/editoffer/:id"
                    element={<EditOffer />}
                  />
                  <Route
                    path="/adminpanel/edituser/:id"
                    element={<EditUser />}
                  />
                  <Route
                    path="/adminpanel/editorder/:id"
                    element={<EditOrder />}
                  />
                  <Route
                    path="/adminpanel/editguestorder/:id"
                    element={<EditGuestOrder />}
                  />
                </Route>

                <Route element={<UserRoute />}>
                  <Route path="/profile/:id" element={<Profile />} />
                </Route>

                {/* stripe payment routes */}
                <Route path="/payment/success" element={<PaymentSuccess />} />
                <Route path="/payment/cancel" element={<PaymentCancel />} />
                <Route path="*" element={<NotFound />} />
              </Routes>
            </Suspense>
          </main>

          <Footer />
        </div>
      </AuthProvider>
    </Router>
  );
}

export default App;
