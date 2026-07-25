import { useState } from "react";
import { useAuth } from "../auth/AuthContextValue";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Alert } from "react-bootstrap";
import useOffers from "../hooks/useOffers";
import { formatSelectedDateTime } from "../api/dataParser";
import { createOrder } from "../api/orderService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";
import { getPaymentMethods } from "../utils/paymentMethods";

const RegisterOrderLoggedForm = ({ onSuccess }) => {
  const { user } = useAuth();
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

  const createOrderMutation = useMutation({
    mutationFn: (orderCreationData) =>
      createOrder(orderCreationData, user?.token),
    onSuccess: (response) => {
      // invalidate order queries so profile and admin tables refresh
      queryClient.invalidateQueries({ queryKey: ["orders"] });
      if (user?.id) {
        queryClient.invalidateQueries({ queryKey: ["userDetails", user.id] });
      }

      // redirect to stripe checkout if online payment was chosen
      if (response?.checkoutUrl) {
        window.location.href = response.checkoutUrl;
        return;
      }

      // trigger success alert in parent wrapper for on-site payments
      onSuccess();
    },
    onError: (error) => {
      console.error("error registering order:", error);
      setShowErrorAlert(true);
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    setShowErrorAlert(false);

    const orderCreationData = {
      idOffer: Number(selectedOffer),
      visitDate: formatSelectedDateTime(
        selectedDate,
        selectedHour,
        selectedMinute,
      ),
      paymentMethod,
    };

    createOrderMutation.mutate(orderCreationData);
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
        <div className="mb-3">
          <label htmlFor="selectoffer" className="form-label">
            {t("orders.selectService")}
          </label>
          <select
            className="form-select"
            id="selectoffer"
            value={selectedOffer}
            onChange={(e) => setSelectedOffer(e.target.value)}
            required
            disabled={createOrderMutation.isPending}
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
            disabled={createOrderMutation.isPending}
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
              onChange={(e) => setSelectedHour(parseInt(e.target.value, 10))}
              required
              disabled={createOrderMutation.isPending}
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
              onChange={(e) => setSelectedMinute(parseInt(e.target.value, 10))}
              required
              disabled={createOrderMutation.isPending}
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
          <label htmlFor="paymentMethod" className="form-label">
            {t("orders.paymentMethod", "Metoda płatności")}
          </label>
          <select
            className="form-select"
            id="paymentMethod"
            value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value)}
            required
            disabled={createOrderMutation.isPending}
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
          loading={createOrderMutation.isPending}
          loadingText={t("orders.registeringOrder")}
        >
          {t("orders.registerBtn")}
        </ButtonSpinner>
      </form>
    </>
  );
};

export default RegisterOrderLoggedForm;
