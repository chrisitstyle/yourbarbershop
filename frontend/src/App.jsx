import "../node_modules/bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Navbar from "./Navbar.jsx";
import Home from "./pages/Home.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Gallery from "./pages/Gallery.jsx";
import Offer from "./pages/Offer.jsx";
import Contact from "./pages/Contact.jsx";
import RegisterOrderWithoutAcc from "./pages/RegisterOrderWithoutAcc.jsx";
import Footer from "./Footer.jsx";
import { AuthProvider } from "./AuthContext.jsx";
import Profile from "./pages/Profile.jsx";
import AdminPanel from "./adminpages/AdminPanel.jsx";
import EditOffer from "./adminpages/EditOffer.jsx";
import EditUser from "./adminpages/EditUser.jsx";
import RegisterOrderLogged from "./pages/RegisterOrderLogged.jsx";
import EditOrder from "./adminpages/EditOrder.jsx";
import EditGuestOrder from "./adminpages/EditGuestOrder.jsx";
import ForgotPassword from "./pages/ForgotPassword.jsx";
import ResetPasswordForm from "./pages/ResetPasswordForm.jsx";
import AdminRoute from "./routes/AdminRoute";
import UserRoute from "./routes/UserRoute";
import NotFound from "./pages/NotFound";
function App() {
  return (
    <AuthProvider>
      <div className="App">
        <Router>
          <Navbar />
          <main>
            <Routes>
              <Route path="/" element={<Home />}></Route>
              <Route path="/login" element={<Login />}></Route>
              <Route
                path="/forgotpassword"
                element={<ForgotPassword />}
              ></Route>
              <Route
                path="/resetpassword"
                element={<ResetPasswordForm />}
              ></Route>
              <Route path="/register" element={<Register />}></Route>
              <Route path="/gallery" element={<Gallery />}></Route>
              <Route path="/offers" element={<Offer />}></Route>
              <Route path="/contact" element={<Contact />}></Route>

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
                <Route
                  path="/registerorderlogged"
                  element={<RegisterOrderLogged />}
                />
              </Route>
              <Route
                path="/registerorderwithoutaccount"
                element={<RegisterOrderWithoutAcc />}
              ></Route>
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
