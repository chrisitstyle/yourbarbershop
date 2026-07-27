import { useEffect, useState } from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import { Toaster } from "sonner";
import Navbar from "./Navbar.jsx";
import Home from "./pages/Home.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Gallery from "./pages/Gallery.jsx";
import Offer from "./pages/Offer.jsx";
import Contact from "./pages/Contact.jsx";
import RegisterOrder from "./pages/RegisterOrder.jsx";
import Footer from "./Footer.jsx";
import { AuthProvider } from "./auth/AuthContext.jsx";
import Profile from "./pages/Profile.jsx";
import AdminPanel from "./adminpages/AdminPanel.jsx";
import EditOffer from "./adminpages/EditOffer.jsx";
import EditUser from "./adminpages/EditUser.jsx";
import EditOrder from "./adminpages/EditOrder.jsx";
import EditGuestOrder from "./adminpages/EditGuestOrder.jsx";
import ForgotPassword from "./pages/ForgotPassword.jsx";
import ResetPasswordForm from "./pages/ResetPasswordForm.jsx";
import AdminRoute from "./routes/AdminRoute";
import UserRoute from "./routes/UserRoute";
import NotFound from "./pages/NotFound";
import OAuth2Redirect from "./OAuth2Redirect.jsx";
import PaymentSuccess from "./pages/PaymentSuccess.jsx";
import PaymentCancel from "./pages/PaymentCancel";

function App() {
  const [theme, setTheme] = useState(
    () => localStorage.getItem("theme") || "light",
  );

  useEffect(() => {
    document.documentElement.setAttribute("data-bs-theme", theme);
    localStorage.setItem("theme", theme);
  }, [theme]);

  const handleToggleTheme = () => {
    setTheme((prev) => (prev === "dark" ? "light" : "dark"));
  };

  return (
    <AuthProvider>
      <div className="App">
        {/* global sonner notification container */}
        <Toaster
          theme={theme}
          richColors
          position="bottom-right"
          closeButton
          toastOptions={{
            style: {
              borderRadius: "6px",
              fontSize: "0.9rem",
            },
          }}
        />

        <Router>
          <Navbar theme={theme} onToggleTheme={handleToggleTheme} />
          <main>
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

              {/* unified order registration route for both logged-in users and guests */}
              <Route path="/registerorder" element={<RegisterOrder />} />

              <Route element={<AdminRoute />}>
                <Route path="/adminpanel" element={<AdminPanel />} />
                <Route
                  path="/adminpanel/editoffer/:id"
                  element={<EditOffer />}
                />
                <Route path="/adminpanel/edituser/:id" element={<EditUser />} />
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

              {/* Stripe payment routes */}
              <Route path="/payment/success" element={<PaymentSuccess />} />
              <Route path="/payment/cancel" element={<PaymentCancel />} />
              <Route path="*" element={<NotFound />} />
            </Routes>
          </main>
          <Footer />
        </Router>
      </div>
    </AuthProvider>
  );
}

export default App;
