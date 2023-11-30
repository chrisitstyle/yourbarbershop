import React, { useState, useEffect } from "react";
import axios from "axios";
import { useAuth } from "../AuthContext";
import { useNavigate } from "react-router-dom";
import { utcToZonedTime, zonedTimeToUtc, format } from "date-fns-tz";
import { Alert } from "react-bootstrap";
import * as api from "../api/api.js";

const RegisterOrderLogged = () => {
  const { user } = useAuth();
  const [offers, setOffers] = useState([]);
  const [selectedOffer, setSelectedOffer] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedHour, setSelectedHour] = useState(8);
  const [selectedMinute, setSelectedMinute] = useState(0);
  const [showErrorAlert, setShowErrorAlert] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchOffers = async () => {
      try {
        const offersData = await api.getOffers();
        setOffers(offersData);
      } catch (error) {
        console.error("Błąd ładowania ofert:", error);
      }
    };

    fetchOffers();
  }, []);

  const formatSelectedDateTime = (date, hour, minute) => {
    // obiekt dla UTC
    const selectedDateTimeUTC = new Date(
      `${date}T${String(hour).padStart(2, "0")}:${String(minute).padStart(
        2,
        "0"
      )}:00.000Z`
    );

    // przesunięcie czasu dla wybranej daty w czasie lokalnym (Europe/Warsaw)
    const offset = new Date().getTimezoneOffset();

    // przesunięcie do daty UTC
    const selectedDateTimeLocal = new Date(
      selectedDateTimeUTC.getTime() - offset * 60 * 1000
    );

    return selectedDateTimeLocal.toISOString();
  };

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
        "http://localhost:8080/orders/add",
        {
          user: {
            idUser: user.id,
          },
          offer: {
            idOffer: selectedOffer,
          },
          orderDate: getCurrentDateTime(),
          visitDate: formatSelectedDateTime(
            selectedDate,
            selectedHour,
            selectedMinute
          ),
        },
        {
          withCredentials: true,
          headers: {
            Authorization: `Bearer ${user.token}`,
          },
        }
      );
      navigate(`/profile/${user.id}`);
    } catch (error) {
      setShowErrorAlert(true);
    }
  };

  const getCurrentDateTime = () => {
    const currentDateTimeUTC = utcToZonedTime(new Date(), "Europe/Warsaw");
    const adjustedDateTimeUTC = new Date(
      currentDateTimeUTC.getTime() + 60 * 60 * 1000 // + 1 godzina
    );

    return format(adjustedDateTimeUTC, "yyyy-MM-dd'T'HH:mm:ss");
  };

  return (
    <>
      <div className="container mt-2">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3">
            <h4 className="text-center">Umów wizytę</h4>
            <Alert
              variant="danger"
              show={showErrorAlert}
              onClose={() => setShowErrorAlert(false)}
              dismissible
            >
              Błąd podczas umawiania wizyty. Spróbuj ponownie.
            </Alert>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
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
              <div className="mb-3">
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
              <div className="mb-3">
                <label htmlFor="selecttime" className="form-label">
                  Wybierz godzinę i minutę
                </label>
                <div className="d-flex">
                  <select
                    className="form-select me-2"
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
                    className="form-select"
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
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default RegisterOrderLogged;
