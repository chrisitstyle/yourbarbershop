import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { updateGuestOrder } from "../api/guestOrderService";
import { format } from "date-fns-tz";
import { formatSelectedDateTime } from "../api/dataParser";
import { Alert } from "react-bootstrap";
import { getOffers } from "../api/offerService";
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
  const [selectedStatus, setSelectedStatus] = useState("");

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
        setSelectedHour(hours);
        setSelectedMinute(minutes);
      } catch (error) {
        console.error("Błąd ładowania ofert:", error);
      }
    };

    fetchOffers();
  }, [guestOrderData]);

  const handleHourChange = (e) => {
    setSelectedHour(parseInt(e.target.value, 10));
  };

  const handleMinuteChange = (e) => {
    setSelectedMinute(parseInt(e.target.value, 10));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const statusToSend = selectedStatus
        ? selectedStatus
        : guestOrderData.status;
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
          status: statusToSend,
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
                  <option value={guestOrderData.offer?.idOffer || "brak"}>
                    {guestOrderData.offer?.kind || "brak"} -{" "}
                    {guestOrderData.offer?.cost || "brak"} zł
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
                  <option value={guestOrderData.status}>
                    {guestOrderData.status}
                  </option>
                  {/* Map other status */}
                  {["NOWE", "ZREALIZOWANE", "ANULOWANE"].map(
                    (status) =>
                      // skip actual status
                      status !== guestOrderData.status && (
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

export default EditGuestOrder;
