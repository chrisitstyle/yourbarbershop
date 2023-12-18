import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { getOffers, updateGuestOrder } from "../api/api";
import { format } from "date-fns-tz";
import { Alert } from "react-bootstrap";
const EditGuestOrder = () => {
  const { user } = useAuth();
  const location = useLocation();
  const guestOrderData = location.state?.guestOrderData;
  const navigate = useNavigate();

  const [offers, setOffers] = useState([]);
  const [selectedOffer, setSelectedOffer] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedHour, setSelectedHour] = useState(8);
  const [selectedMinute, setSelectedMinute] = useState(0);
  const [editGuestOrderError, setEditGuestOrderError] = useState(false);

  useEffect(() => {
    const fetchOffers = async () => {
      try {
        const offersData = await getOffers();
        setOffers(offersData);
        setSelectedOffer(guestOrderData?.offer?.idOffer || "");
        setSelectedDate(
          format(new Date(guestOrderData?.visitDate), "yyyy-MM-dd")
        );
        const hours = new Date(guestOrderData?.visitDate).getHours();
        const minutes = new Date(guestOrderData?.visitDate).getMinutes();
        setSelectedHour(hours - 1);
        setSelectedMinute(minutes);
      } catch (error) {
        console.error("Błąd ładowania ofert:", error);
      }
    };

    fetchOffers();
  }, [guestOrderData]);

  const formatSelectedDateTime = (date, hour, minute) => {
    const selectedDateTimeUTC = new Date(
      `${date}T${String(hour).padStart(2, "0")}:${String(minute).padStart(
        2,
        "0"
      )}:00.000Z`
    );

    const offset = new Date().getTimezoneOffset();
    const selectedDateTimeLocal = new Date(
      selectedDateTimeUTC.getTime() - offset * 60 * 1000
    );

    return selectedDateTimeLocal.toISOString();
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
      await updateGuestOrder(
        guestOrderData.idGuestOrder,
        {
          firstname: guestOrderData.firstname,
          lastname: guestOrderData.lastname,
          phonenumber: guestOrderData.phonenumber,
          offer: {
            idOffer: selectedOffer,
          },
          orderDate: guestOrderData.orderDate,
          visitDate: formatSelectedDateTime(
            selectedDate,
            selectedHour,
            selectedMinute
          ),
        },
        user.token
      );

      navigate("/adminpanel");
    } catch (error) {
      setEditGuestOrderError(true);
    }
  };

  return (
    <>
      <div className="container mt-2">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3">
            <h4 className="text-center">
              Edycja wizyty o id {guestOrderData.idGuestOrder}
            </h4>
            <Alert
              variant="danger"
              show={editGuestOrderError}
              onClose={() => setEditGuestOrderError(false)}
              dismissible
            >
              Błąd podczas edytowania wizyty. Spróbuj ponownie.
            </Alert>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="selectOffer" className="form-label">
                  Wybierz usługę
                </label>
                <select
                  className="form-select"
                  id="selectOffer"
                  value={selectedOffer}
                  onChange={(e) => setSelectedOffer(e.target.value)}
                  required
                >
                  <option value={guestOrderData.offer.idOffer}>
                    {guestOrderData.offer.kind} - {guestOrderData.offer.cost} zł
                  </option>
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
                  // min={new Date().toISOString().split("T")[0]}
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
                Zapisz zmiany
              </button>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default EditGuestOrder;
