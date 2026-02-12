import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Alert from "react-bootstrap/Alert";
import axios from "axios";
import { getOffers } from "../api/offerService";
import { sendConfirmationEmail } from "../api/emailService";
import { getCurrentDateTime, formatSelectedDateTime } from "../api/dataParser";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { API_BASE_URL } from "../api/config";
import { useTranslation } from "react-i18next";

const RegisterOrderWithoutAcc = () => {
  const [firstname, setFirstName] = useState("");
  const [lastname, setLastName] = useState("");
  const [phonenumber, setPhoneNumber] = useState("");
  const [email, setEmail] = useState("");
  const [offers, setOffers] = useState([]);
  const [selectedOffer, setSelectedOffer] = useState("");

  const [selectedOfferName, setSelectedOfferName] = useState("");
  const [selectedOfferCost, setSelectedOfferCost] = useState(0);

  const [selectedDate, setSelectedDate] = useState("");
  const [selectedHour, setSelectedHour] = useState(8);
  const [selectedMinute, setSelectedMinute] = useState(0);
  const [showAlert, setShowAlert] = useState(false);
  const [showErrorAlert, setShowErrorAlert] = useState(false);
  const [isLoadingOffers, setIsLoadingOffers] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  const { t } = useTranslation();

  useEffect(() => {
    const fetchOffers = async () => {
      try {
        const offersData = await getOffers();
        setOffers(offersData);
      } catch (error) {
        console.error("Błąd ładowania ofert:", error);
      } finally {
        setIsLoadingOffers(false);
      }
    };

    fetchOffers();
  }, []);

  // function to reset form fields
  const setInitialState = () => {
    setFirstName("");
    setLastName("");
    setPhoneNumber("");
    setEmail("");
    setSelectedOffer("");
    setSelectedDate("");
    setSelectedHour(8);
    setSelectedMinute(0);
  };

  const handleOfferChange = (e) => {
    const selectedOfferId = e.target.value;

    const selectedOfferData = offers.find(
      (offer) => offer.idOffer === parseInt(selectedOfferId),
    );
    if (selectedOfferData) {
      setSelectedOffer(selectedOfferId);
      setSelectedOfferName(selectedOfferData.kind);
      setSelectedOfferCost(selectedOfferData.cost);
    }
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
      const response = await axios.post(`${API_BASE_URL}/guestorders`, {
        firstname,
        lastname,
        phonenumber,
        email,

        offer: {
          idOffer: selectedOffer,
        },

        orderDate: getCurrentDateTime(),
        visitDate: formatSelectedDateTime(
          selectedDate,
          selectedHour,
          selectedMinute,
        ),
        status: "NOWE",
      });
      setInitialState();
      await sendConfirmationEmail(
        email,
        firstname,
        lastname,
        selectedDate,
        selectedHour,
        selectedMinute,
        selectedOfferName,
        selectedOfferCost,
      );
      setShowAlert(true);
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
              variant="success"
              show={showAlert}
              onHide={() => setShowAlert(false)}
              dismissible
            >
              {t("orders.successMessage")}
            </Alert>
            <Alert
              variant="danger"
              show={showErrorAlert}
              onHide={() => setShowErrorAlert(false)}
              dismissible
            >
              {t("orders.errorMessage")}
            </Alert>
            <form onSubmit={handleSubmit}>
              <div className="mb-2">
                <label htmlFor="inputfirstname" className="form-label">
                  {t("auth.firstname")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="firstname"
                  value={firstname}
                  onChange={(e) => setFirstName(e.target.value)}
                  required
                  disabled={isLoading}
                />
              </div>
              <div className="mb-2">
                <label htmlFor="inputlastname" className="form-label">
                  {t("auth.lastname")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="lastname"
                  value={lastname}
                  onChange={(e) => setLastName(e.target.value)}
                  required
                  disabled={isLoading}
                />
              </div>
              <div className="mb-2">
                <label htmlFor="inputphonenumber" className="form-label">
                  {t("orders.phoneNumber")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="phonenumber"
                  value={phonenumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  required
                  disabled={isLoading}
                />
              </div>
              <div className="mb-2">
                <label htmlFor="inputemail" className="form-label">
                  {t("auth.email")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  disabled={isLoading}
                />
              </div>
              <div className="mb-2">
                <label htmlFor="selectoffer" className="form-label">
                  {t("orders.selectService")}
                </label>
                <select
                  className="form-select"
                  id="selectoffer"
                  value={selectedOffer}
                  onChange={handleOfferChange}
                  required
                  disabled={isLoading}
                >
                  <option value="" disabled></option>
                  {offers.map((offer) => (
                    <option key={offer.idOffer} value={offer.idOffer}>
                      {offer.kind} - {offer.cost} {t("common.currency")}
                    </option>
                  ))}
                </select>
              </div>
              <div className="mb-2">
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
                  disabled={isLoading}
                />
              </div>
              <div className="mb-2">
                <label htmlFor="selecttime" className="form-label">
                  {t("orders.selectTime")}
                </label>
                <div className="d-flex">
                  <select
                    className="form-select me-1"
                    id="selecthour"
                    value={selectedHour}
                    onChange={handleHourChange}
                    required
                    disabled={isLoading}
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
                    disabled={isLoading}
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
              <p className="mt-2 text-center">
                {t("auth.noAccount")}{" "}
                <Link to="/register">{t("auth.registerLink")}</Link>
              </p>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default RegisterOrderWithoutAcc;
