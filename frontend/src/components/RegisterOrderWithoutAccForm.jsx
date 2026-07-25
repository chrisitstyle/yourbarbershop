import { useState } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import Alert from "react-bootstrap/Alert";
import useOffers from "../hooks/useOffers";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";
import { formatSelectedDateTime } from "../api/dataParser";
import { createGuestOrder } from "../api/guestOrderService";
import { getPaymentMethods } from "../utils/paymentMethods";

const RegisterOrderWithoutAccForm = ({ onSuccess }) => {
  const [firstname, setFirstName] = useState("");
  const [lastname, setLastName] = useState("");
  const [phonenumber, setPhoneNumber] = useState("");
  const [email, setEmail] = useState("");
  const [selectedOffer, setSelectedOffer] = useState("");

  const [selectedDate, setSelectedDate] = useState("");
  const [selectedHour, setSelectedHour] = useState(8);
  const [selectedMinute, setSelectedMinute] = useState(0);
  const [paymentMethod, setPaymentMethod] = useState("GOTOWKA");

  const [showErrorAlert, setShowErrorAlert] = useState(false);

  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const { offers = [], isLoading: isLoadingOffers } = useOffers();
  const paymentMethods = getPaymentMethods(t);

  const createGuestOrderMutation = useMutation({
    mutationFn: (guestOrderCreationData) =>
      createGuestOrder(guestOrderCreationData),
    onSuccess: (response) => {
      // invalidate guest orders query cache so admin tables update automatically
      queryClient.invalidateQueries({ queryKey: ["guestOrders"] });

      // redirect to stripe checkout if online payment was chosen
      if (response?.checkoutUrl) {
        window.location.href = response.checkoutUrl;
        return;
      }

      // trigger success state in parent wrapper
      onSuccess();
    },
    onError: (error) => {
      console.error("error registering guest order:", error);
      setShowErrorAlert(true);
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    setShowErrorAlert(false);

    const guestOrderCreationData = {
      firstname,
      lastname,
      phonenumber,
      email,
      idOffer: Number(selectedOffer),
      visitDate: formatSelectedDateTime(
        selectedDate,
        selectedHour,
        selectedMinute,
      ),
      paymentMethod,
    };

    createGuestOrderMutation.mutate(guestOrderCreationData);
  };

  if (isLoadingOffers) {
    return <LoadingSpinner text={t("orders.loadingServices")} />;
  }

  return (
    <>
      <Alert
        variant="danger"
        show={showErrorAlert}
        onClose={() => setShowErrorAlert(false)}
        dismissible
      >
        {t("orders.errorMessage")}
      </Alert>

      <form onSubmit={handleSubmit}>
        <div className="mb-2">
          <label htmlFor="firstname" className="form-label">
            {t("auth.firstname")}
          </label>
          <input
            type="text"
            className="form-control"
            id="firstname"
            value={firstname}
            onChange={(e) => setFirstName(e.target.value)}
            required
            disabled={createGuestOrderMutation.isPending}
          />
        </div>

        <div className="mb-2">
          <label htmlFor="lastname" className="form-label">
            {t("auth.lastname")}
          </label>
          <input
            type="text"
            className="form-control"
            id="lastname"
            value={lastname}
            onChange={(e) => setLastName(e.target.value)}
            required
            disabled={createGuestOrderMutation.isPending}
          />
        </div>

        <div className="mb-2">
          <label htmlFor="phonenumber" className="form-label">
            {t("orders.phoneNumber")}
          </label>
          <input
            type="text"
            className="form-control"
            id="phonenumber"
            value={phonenumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            required
            disabled={createGuestOrderMutation.isPending}
          />
        </div>

        <div className="mb-2">
          <label htmlFor="email" className="form-label">
            {t("auth.email")}
          </label>
          <input
            type="email"
            className="form-control"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={createGuestOrderMutation.isPending}
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
            onChange={(e) => setSelectedOffer(e.target.value)}
            required
            disabled={createGuestOrderMutation.isPending}
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
            disabled={createGuestOrderMutation.isPending}
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
              onChange={(e) => setSelectedHour(parseInt(e.target.value, 10))}
              required
              disabled={createGuestOrderMutation.isPending}
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
              onChange={(e) => setSelectedMinute(parseInt(e.target.value, 10))}
              required
              disabled={createGuestOrderMutation.isPending}
            >
              {[...Array(2).keys()].map((half) => (
                <option key={half * 30} value={half * 30}>
                  {String(half * 30).padStart(2, "0")}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="mb-2">
          <label htmlFor="paymentMethod" className="form-label">
            {t("orders.paymentMethod", "Metoda płatności")}
          </label>
          <select
            className="form-select"
            id="paymentMethod"
            value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value)}
            required
            disabled={createGuestOrderMutation.isPending}
          >
            {paymentMethods.map((method) => (
              <option key={method.value} value={method.value}>
                {method.label}
              </option>
            ))}
          </select>
        </div>

        <ButtonSpinner
          type="submit"
          variant="dark"
          className="mx-auto d-block"
          loading={createGuestOrderMutation.isPending}
          loadingText={t("orders.registeringOrder")}
        >
          {t("orders.registerBtn")}
        </ButtonSpinner>

        <p className="mt-2 text-center">
          {t("auth.noAccount")}{" "}
          <Link to="/register">{t("auth.registerLink")}</Link>
        </p>
      </form>
    </>
  );
};

export default RegisterOrderWithoutAccForm;
