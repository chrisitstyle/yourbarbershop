import "../node_modules/bootstrap/dist/css/bootstrap.min.css";
import "../node_modules/bootstrap/dist/js/bootstrap.bundle.min.js";
import {
  BrowserRouter as Router,
  Route,
  Routes,
  Navigate,
} from "react-router-dom";
import Navbar from "./Navbar";
import Home from "./pages/Home.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Gallery from "./pages/Gallery.jsx";
import Offer from "./pages/Offer.jsx";
import Contact from "./pages/Contact.jsx";
import RegisterOrderWithoutAcc from "./pages/RegisterOrderWithoutAcc.jsx";
import Footer from "./Footer.jsx";
import { AuthProvider } from "./AuthContext";
import { useAuth } from "./AuthContext";
import Profile from "./pages/Profile.jsx";
import AdminPanel from "./adminpages/AdminPanel.jsx";
import EditOffer from "./adminpages/EditOffer.jsx";
import EditUser from "./adminpages/EditUser.jsx";
import RegisterOrderLogged from "./pages/RegisterOrderLogged.jsx";
import EditOrder from "./adminpages/EditOrder.jsx";
import EditGuestOrder from "./adminpages/EditGuestOrder.jsx";

function AdminRoute({ element }) {
  const { user } = useAuth();

  if (user && user.role === "ADMIN") {
    return element;
  } else {
    return <Navigate to="/" replace />;
  }
}

function UserRoute({ element }) {
  const { user } = useAuth();

  if (user && user.role === "USER") {
    return element;
  } else {
    return <Navigate to="/" replace />;
  }
}

function App() {
  return (
    <AuthProvider>
      <div className="App">
        <Router>
          <Navbar />
          <Routes>
            <Route exact path="/" element={<Home />}></Route>
            <Route exact path="/login" element={<Login />}></Route>
            <Route exact path="/register" element={<Register />}></Route>
            <Route exact path="/gallery" element={<Gallery />}></Route>
            <Route exact path="/offer" element={<Offer />}></Route>
            <Route exact path="/contact" element={<Contact />}></Route>
            <Route
              exact
              path="/profile/:id"
              element={<UserRoute element={<Profile />} />}
            />
            <Route
              exact
              path="/adminpanel"
              element={<AdminRoute element={<AdminPanel />} />}
            />
            <Route
              path="/adminpanel/editoffer/:id"
              element={<AdminRoute element={<EditOffer />} />}
            />
            <Route
              path="/adminpanel/edituser/:id"
              element={<AdminRoute element={<EditUser />} />}
            />
            <Route
              path="/adminpanel/editorder/:id"
              element={<AdminRoute element={<EditOrder />} />}
            />
            <Route
              path="/adminpanel/editguestorder/:id"
              element={<AdminRoute element={<EditGuestOrder />} />}
            />

            <Route
              exact
              path="/registerorderwithoutaccount"
              element={<RegisterOrderWithoutAcc />}
            ></Route>
            <Route
              exact
              path="/registerorderlogged"
              element={<RegisterOrderLogged />}
            />
          </Routes>
          <Footer />
        </Router>
      </div>
    </AuthProvider>
  );
}

export default App;
