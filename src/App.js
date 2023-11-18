import "../node_modules/bootstrap/dist/css/bootstrap.min.css";
import "../node_modules/bootstrap/dist/js/bootstrap.bundle.min.js";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Navbar from "./Navbar";
import Home from "./Home.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Gallery from "./pages/Gallery.jsx";
import Offer from "./pages/Offer.jsx";
import Contact from "./pages/Contact.jsx";
import RegisterOrderWithoutAcc from "./pages/RegisterOrderWithoutAcc.jsx";
import Footer from "./Footer.jsx";
import { AuthProvider } from "./AuthContext";
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
              path="/registerorderwithoutaccount"
              element={<RegisterOrderWithoutAcc />}
            ></Route>
          </Routes>
          <Footer />
        </Router>
      </div>
    </AuthProvider>
  );
}

export default App;
