import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";

import { updateOrder } from "../api/orderService";
import { getOffers } from "../api/offerService";
import { format, zonedTimeToUtc } from "date-fns-tz";
import { Alert } from "react-bootstrap";

const EditOrder = () => {
  const { user } = useAuth();
  const location = useLocation();
  const orderData = location.state?.orderData;
  const navigate = useNavigate();

  const [offers, setOffers] = useState([]);
  const [selectedOffer, setSelectedOffer] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedHour, setSelectedHour] = useState(8);
  const [selectedMinute, setSelectedMinute] = useState(0);
  const [editOrderError, setEditOrderError] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState("");

  useEffect(() => {
    const fetchOffers = async () => {
      try {
        const offersData = await getOffers();
        setOffers(offersData);
        setSelectedOffer(orderData?.offer?.idOffer || "");
        setSelectedDate(format(new Date(orderData?.visitDate), "yyyy-MM-dd"));

        // Set time from orderData
        const hours = new Date(orderData?.visitDate).getHours();
        const minutes = new Date(orderData?.visitDate).getMinutes();
        setSelectedHour(hours);
        setSelectedMinute(minutes);
      } catch (error) {
        console.error("Błąd ładowania ofert:", error);
      }
    };

    fetchOffers();
  }, [orderData]);

  const formatSelectedDateTime = (date, hour, minute) => {
    // new object from date, hours and minutes
    const selectedDateTime = new Date(date);
    selectedDateTime.setHours(hour);
    selectedDateTime.setMinutes(minute);

    // convert to UTC
    const selectedDateTimeUTC = zonedTimeToUtc(
      selectedDateTime,
      "Europe/Warsaw"
    );

    // Format date and time for server
    const formattedDateTime = format(
      selectedDateTimeUTC,
      "yyyy-MM-dd'T'HH:mm:ss",
      { timeZone: "UTC" }
    );

    return formattedDateTime;
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
      const statusToSend = selectedStatus ? selectedStatus : orderData.status;
      await updateOrder(
        orderData.idOrder,
        {
          user: { idUser: orderData.user.idUser },
          offer: { idOffer: selectedOffer },
          orderDate: orderData.orderDate,
          visitDate: formatSelectedDateTime(
            selectedDate,
            selectedHour,
            selectedMinute
          ),
          status: statusToSend,
        },
        user.token
      );

      navigate("/adminpanel");
    } catch (error) {
      setEditOrderError(true);
    }
  };

  return (
    <>
      <div className="container mt-2">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3">
            <h4 className="text-center">
              Edycja wizyty o id {orderData.idOrder}
            </h4>
            <Alert
              variant="danger"
              show={editOrderError}
              onClose={() => setEditOrderError(false)}
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
                  <option value={orderData.offer?.idOffer || "brak"}>
                    {orderData.offer?.kind || "brak"} -{" "}
                    {orderData.offer?.cost || "brak"} zł
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
                  //  min={new Date().toISOString().split("T")[0]}
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
              <div className="mb-3">
                <label htmlFor="selectstatus" className="form-label">
                  Wybierz status
                </label>
                <select
                  className="form-select"
                  id="selectstatus"
                  value={selectedStatus}
                  onChange={(e) => setSelectedStatus(e.target.value)}
                  required
                >
                  {/* actual status as first option */}
                  <option value={orderData.status}>{orderData.status}</option>
                  {/* Map other status */}
                  {["NOWE", "ZREALIZOWANE", "ANULOWANE"].map(
                    (status) =>
                      // skip actual status
                      status !== orderData.status && (
                        <option key={status} value={status}>
                          {status}
                        </option>
                      )
                  )}
                </select>
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

export default EditOrder;
