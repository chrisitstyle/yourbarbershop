import { useRef, useState } from "react";
import { useAuth } from "../auth/AuthContextValue";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import useOffers from "../hooks/useOffers";
import { formatSelectedDateTime } from "../api/dataParser";
import { createOrder } from "../api/orderService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";
import { getPaymentMethods } from "../utils/paymentMethods";
import "./styles/OrderForm.css";

const RegisterOrderLoggedForm = ({ onSuccess }) => {
  const { user } = useAuth();
  const [selectedOffer, setSelectedOffer] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedHour, setSelectedHour] = useState(8);
  const [selectedMinute, setSelectedMinute] = useState(0);
  const [paymentMethod, setPaymentMethod] = useState("GOTOWKA");

  const idempotencyRequestRef = useRef(null);

  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const { offers = [], isLoading: isLoadingOffers } = useOffers();
  const paymentMethods = getPaymentMethods(t);

  const createOrderMutation = useMutation({
    mutationFn: ({ orderCreationData, idempotencyKey }) =>
      createOrder(orderCreationData, idempotencyKey),
    onSuccess: (response) => {
      idempotencyRequestRef.current = null;

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

      toast.success(t("orders.successMessage"));

      // trigger success callback in parent wrapper for on-site payments
      onSuccess?.(response);
    },
    onError: (error) => {
      // extract backend error message or fallback to generic i18n message
      const errorMsg = error?.response?.data || error?.message;

      if (typeof errorMsg === "string" && errorMsg) {
        toast.error(errorMsg);
      } else {
        toast.error(t("orders.errorMessage"));
      }
    },
  });

  const resolveIdempotencyKey = (orderCreationData) => {
    const requestSignature = JSON.stringify(orderCreationData);
    const previousRequest = idempotencyRequestRef.current;

    if (previousRequest?.requestSignature === requestSignature) {
      return previousRequest.idempotencyKey;
    }

    const idempotencyKey = globalThis.crypto.randomUUID();

    idempotencyRequestRef.current = {
      requestSignature,
      idempotencyKey,
    };

    return idempotencyKey;
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const orderCreationData = {
      idOffer: Number(selectedOffer),
      visitDate: formatSelectedDateTime(
        selectedDate,
        selectedHour,
        selectedMinute,
      ),
      paymentMethod,
    };

    const idempotencyKey = resolveIdempotencyKey(orderCreationData);

    createOrderMutation.mutate({
      orderCreationData,
      idempotencyKey,
    });
  };

  if (isLoadingOffers) {
    return <LoadingSpinner text={t("orders.loadingServices")} />;
  }

  return (
    <form
      className="order-form order-form--logged card card-accent-top"
      onSubmit={handleSubmit}
    >
      <div className="card-header order-form__header py-3">
        <h2 className="card-title order-form__title text-center mb-0 fw-semibold">
          {t("orders.bookAppointment")}
        </h2>
      </div>

      <div className="card-body order-form__body p-4">
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
            <option value="" disabled>
              {t("orders.selectServicePlaceholder")}
            </option>
            {offers.map((offer) => (
              <option key={offer.idOffer} value={offer.idOffer}>
                {offer.kind} — {offer.cost} {t("common.currency")}
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
          <label htmlFor="selecthour" className="form-label">
            {t("orders.selectTimeFull")}
          </label>
          <div className="input-group order-form__time-group">
            <select
              className="form-select"
              id="selecthour"
              value={selectedHour}
              onChange={(e) =>
                setSelectedHour(Number.parseInt(e.target.value, 10))
              }
              required
              disabled={createOrderMutation.isPending}
              aria-label={t("orders.selectHour")}
            >
              {[...new Array(12).keys()].map((hour) => (
                <option key={hour} value={hour + 8}>
                  {String(hour + 8).padStart(2, "0")}
                </option>
              ))}
            </select>

            <span className="input-group-text" aria-hidden="true">
              :
            </span>

            <select
              className="form-select"
              id="selectminute"
              value={selectedMinute}
              onChange={(e) =>
                setSelectedMinute(Number.parseInt(e.target.value, 10))
              }
              required
              disabled={createOrderMutation.isPending}
              aria-label={t("orders.selectMinute")}
            >
              {[...new Array(2).keys()].map((half) => (
                <option key={half * 30} value={half * 30}>
                  {String(half * 30).padStart(2, "0")}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="mb-4">
          <label htmlFor="paymentMethod" className="form-label">
            {t("orders.paymentMethod")}
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
          variant="primary"
          className="order-form__submit d-block mx-auto px-4"
          loading={createOrderMutation.isPending}
          loadingText={t("orders.registeringOrder")}
        >
          {t("orders.registerBtn")}
        </ButtonSpinner>
      </div>
    </form>
  );
};

export default RegisterOrderLoggedForm;
