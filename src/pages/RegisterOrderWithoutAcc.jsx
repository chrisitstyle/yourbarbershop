import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import Alert from "react-bootstrap/Alert";
import axios from "axios";
import { getOffers } from "../api/offerService";
import { getCurrentDateTime, formatSelectedDateTime } from "../api/dataParser";
const RegisterOrderWithoutAcc = () => {
  const navigate = useNavigate();
  const [firstname, setFirstName] = useState("");
  const [lastname, setLastName] = useState("");
  const [phonenumber, setPhoneNumber] = useState("");
  const [offers, setOffers] = useState([]);
  const [selectedOffer, setSelectedOffer] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedHour, setSelectedHour] = useState(8);
  const [selectedMinute, setSelectedMinute] = useState(0);
  const [showAlert, setShowAlert] = useState(false);
  const [showErrorAlert, setShowErrorAlert] = useState(false);

  // function to reset form fields
  const setInitialState = () => {
    setFirstName("");
    setLastName("");
    setPhoneNumber("");
    setSelectedOffer("");
    setSelectedDate("");
    setSelectedHour(8);
    setSelectedMinute(0);
  };

  useEffect(() => {
    const fetchOffers = async () => {
      try {
        const offersData = await getOffers();
        setOffers(offersData);
      } catch (error) {
        console.error("Błąd ładowania ofert:", error);
      }
    };

    fetchOffers();
  }, []);

  const handleOfferChange = (e) => {
    const selectedOfferId = e.target.value;
    setSelectedOffer(selectedOfferId);
  };

  const handleHourChange = (e) => {
    setSelectedHour(parseInt(e.target.value, 10));
  };

  const handleMinuteChange = (e) => {
    setSelectedMinute(parseInt(e.target.value, 10));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await axios.post(
        "http://localhost:8080/guestorders/add",
        {
          firstname,
          lastname,
          phonenumber,

          offer: {
            idOffer: selectedOffer,
          },

          orderDate: getCurrentDateTime(),
          visitDate: formatSelectedDateTime(
            selectedDate,
            selectedHour,
            selectedMinute
          ),
          status: "NOWE",
        }
      );

      setInitialState();
      setShowAlert(true);
    } catch (error) {
      setShowErrorAlert(true);
    }
  };

  return (
    <>
      <div className="container mt-2">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3">
            <h4 className="display-6 text-center">Umów wizytę</h4>
            <Alert
              variant="success"
              show={showAlert}
              onClose={() => setShowAlert(false)}
              dismissible
            >
              Udało się umówić wizytę!
            </Alert>
            <Alert
              variant="danger"
              show={showErrorAlert}
              onClose={() => setShowErrorAlert(false)}
              dismissible
            >
              Błąd podczas umawiania wizyty. Spróbuj ponownie.
            </Alert>
            <form onSubmit={handleSubmit}>
              <div className="mb-2">
                <label htmlFor="inputfirstname" className="form-label">
                  Imię
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="firstname"
                  value={firstname}
                  onChange={(e) => setFirstName(e.target.value)}
                  required
                />
              </div>
              <div className="mb-2">
                <label htmlFor="inputlastname" className="form-label">
                  Nazwisko
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="lastname"
                  value={lastname}
                  onChange={(e) => setLastName(e.target.value)}
                  required
                />
              </div>
              <div className="mb-2">
                <label htmlFor="inputphonenumber" className="form-label">
                  Numer telefonu
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="phonenumber"
                  value={phonenumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  required
                />
              </div>
              <div className="mb-2">
                <label htmlFor="selectoffer" className="form-label">
                  Wybierz usługę
                </label>
                <select
                  className="form-select"
                  id="selectoffer"
                  value={selectedOffer}
                  onChange={handleOfferChange}
                  required
                >
                  <option value="" disabled></option>
                  {offers.map((offer) => (
                    <option key={offer.idOffer} value={offer.idOffer}>
                      {offer.kind} - {offer.cost} zł
                    </option>
                  ))}
                </select>
              </div>
              <div className="mb-2">
                <label htmlFor="selectdate" className="form-label">
                  Wybierz datę
                </label>
                <input
                  type="date"
                  className="form-control"
                  id="selectdate"
                  value={selectedDate}
                  onChange={(e) => setSelectedDate(e.target.value)}
                  min={new Date().toISOString().split("T")[0]}
                  required
                />
              </div>
              <div className="mb-2">
                <label htmlFor="selecttime" className="form-label">
                  Wybierz godzinę i minutę
                </label>
                <div className="d-flex">
                  <select
                    className="form-select me-1"
                    id="selecthour"
                    value={selectedHour}
                    onChange={handleHourChange}
                    required
                  >
                    {[...Array(12).keys()].map((hour) => (
                      <option key={hour} value={hour + 8}>
                        {String(hour + 8).padStart(2, "0")}
                      </option>
                    ))}
                  </select>
                  <select
                    className="form-select me-1"
                    id="selectminute"
                    value={selectedMinute}
                    onChange={handleMinuteChange}
                    required
                  >
                    {[...Array(2).keys()].map((half) => (
                      <option key={half * 30} value={half * 30}>
                        {String(half * 30).padStart(2, "0")}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <button type="submit" className="btn btn-dark mx-auto d-block">
                Umów wizytę!
              </button>
              <p className="mt-2 text-center">
                Nie masz konta? <Link to="/register">Zarejestruj się</Link>
              </p>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default RegisterOrderWithoutAcc;
