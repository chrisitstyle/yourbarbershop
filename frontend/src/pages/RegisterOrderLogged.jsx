import { useState } from "react";
import { useAuth } from "../AuthContext";
import { useNavigate } from "react-router-dom";
import { Alert } from "react-bootstrap";
import useOffers from "../hooks/useOffers";
import { formatSelectedDateTime } from "../api/dataParser";
import { createOrder } from "../api/orderService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";

const RegisterOrderLogged = () => {
  const { user } = useAuth();
  const [selectedOffer, setSelectedOffer] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedHour, setSelectedHour] = useState(8);
  const [selectedMinute, setSelectedMinute] = useState(0);
  const [showErrorAlert, setShowErrorAlert] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { t } = useTranslation();

  const { offers, isLoading: isLoadingOffers, error } = useOffers();

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
    setIsLoading(true);

    try {
      const orderCreationData = {
        idOffer: Number(selectedOffer),
        visitDate: formatSelectedDateTime(
          selectedDate,
          selectedHour,
          selectedMinute,
        ),
      };

      await createOrder(orderCreationData, user.token);

      navigate(`/profile/${user.id}?registrationOrderSuccess=true`);
    } catch (error) {
      setShowErrorAlert(true);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoadingOffers) {
    return <LoadingSpinner text={t("orders.loadingServices")} />;
  }

  return (
    <>
      <div className="container mt-2">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3">
            <h4 className="display-6 text-center">
              {t("orders.registerTitle")}
            </h4>
            <Alert
              variant="danger"
              show={showErrorAlert}
              onClose={() => setShowErrorAlert(false)}
              dismissible
            >
              {t("orders.errorMessage")}
            </Alert>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="selectoffer" className="form-label">
                  {t("orders.selectService")}
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
                      {offer.kind} - {offer.cost} {t("common.currency")}
                    </option>
                  ))}
                </select>
              </div>
              <div className="mb-3">
                <label htmlFor="selectdate" className="form-label">
                  {t("orders.selectDate")}
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
                  {t("orders.selectTimeFull")}
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
              <ButtonSpinner
                type="submit"
                variant="dark"
                className="mx-auto d-block"
                loading={isLoading}
                loadingText={t("orders.registeringOrder")}
              >
                {t("orders.registerBtn")}
              </ButtonSpinner>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default RegisterOrderLogged;
