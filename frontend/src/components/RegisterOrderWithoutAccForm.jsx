import { useRef, useState } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import useOffers from "../hooks/useOffers";
import LoadingSpinner from "../components/common/LoadingSpinner";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";
import { formatSelectedDateTime } from "../api/dataParser";
import { createGuestOrder } from "../api/guestOrderService";
import { getPaymentMethods } from "../utils/paymentMethods";
import "./styles/OrderForm.css";

const RegisterOrderWithoutAccForm = ({ onSuccess }) => {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [email, setEmail] = useState("");
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

  const createGuestOrderMutation = useMutation({
    mutationFn: ({ guestOrderCreationData, idempotencyKey }) =>
      createGuestOrder(guestOrderCreationData, idempotencyKey),
    onSuccess: (response) => {
      idempotencyRequestRef.current = null;

      // invalidate guest orders query cache so admin tables update automatically
      queryClient.invalidateQueries({ queryKey: ["guestOrders"] });

      // redirect to stripe checkout if online payment was chosen
      if (response?.checkoutUrl) {
        window.location.href = response.checkoutUrl;
        return;
      }

      toast.success(t("orders.successMessage"));

      // trigger success state in parent wrapper
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

  const resolveIdempotencyKey = (guestOrderCreationData) => {
    const requestSignature = JSON.stringify(guestOrderCreationData);
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

    const guestOrderCreationData = {
      firstname: firstName,
      lastname: lastName,
      phonenumber: phoneNumber,
      email: email,
      idOffer: Number(selectedOffer),
      visitDate: formatSelectedDateTime(
        selectedDate,
        selectedHour,
        selectedMinute,
      ),
      paymentMethod,
    };

    const idempotencyKey = resolveIdempotencyKey(guestOrderCreationData);

    createGuestOrderMutation.mutate({
      guestOrderCreationData,
      idempotencyKey,
    });
  };

  if (isLoadingOffers) {
    return <LoadingSpinner text={t("orders.loadingServices")} />;
  }

  return (
    <form
      className="order-form order-form--guest card card-accent-top"
      onSubmit={handleSubmit}
    >
      <div className="card-header order-form__header py-3">
        <h2 className="card-title order-form__title text-center mb-0 fw-semibold">
          {t("orders.bookAppointment")}
        </h2>
        <p className="order-form__subtitle text-center text-muted small mb-0 mt-1">
          {t("orders.guestReservation")}
        </p>
      </div>

      <div className="card-body order-form__body p-4">
        <div className="row g-3 mb-3">
          <div className="col-12 col-sm-6">
            <label htmlFor="firstname" className="form-label">
              {t("auth.firstname")}
            </label>
            <input
              type="text"
              className="form-control"
              id="firstname"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              autoComplete="given-name"
              required
              disabled={createGuestOrderMutation.isPending}
            />
          </div>

          <div className="col-12 col-sm-6">
            <label htmlFor="lastname" className="form-label">
              {t("auth.lastname")}
            </label>
            <input
              type="text"
              className="form-control"
              id="lastname"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              autoComplete="family-name"
              required
              disabled={createGuestOrderMutation.isPending}
            />
          </div>
        </div>

        <div className="row g-3 mb-4">
          <div className="col-12 col-sm-6">
            <label htmlFor="phonenumber" className="form-label">
              {t("orders.phoneNumber")}
            </label>
            <input
              type="tel"
              className="form-control"
              id="phonenumber"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              autoComplete="tel"
              inputMode="tel"
              required
              disabled={createGuestOrderMutation.isPending}
            />
          </div>

          <div className="col-12 col-sm-6">
            <label htmlFor="email" className="form-label">
              {t("auth.email")}
            </label>
            <input
              type="email"
              className="form-control"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
              required
              disabled={createGuestOrderMutation.isPending}
            />
          </div>
        </div>

        <hr className="order-form__divider my-4" />

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
            disabled={createGuestOrderMutation.isPending}
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
            disabled={createGuestOrderMutation.isPending}
          />
        </div>

        <div className="mb-3">
          <label htmlFor="selecthour" className="form-label">
            {t("orders.selectTime")}
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
              disabled={createGuestOrderMutation.isPending}
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
              disabled={createGuestOrderMutation.isPending}
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
          variant="primary"
          className="order-form__submit d-block mx-auto px-4"
          loading={createGuestOrderMutation.isPending}
          loadingText={t("orders.registeringOrder")}
        >
          {t("orders.registerBtn")}
        </ButtonSpinner>

        <p className="order-form__account-link text-center text-muted small mt-3 mb-0">
          {t("auth.noAccount")}{" "}
          <Link to="/register" className="text-decoration-none">
            {t("auth.registerLink")}
          </Link>
        </p>
      </div>
    </form>
  );
};

export default RegisterOrderWithoutAccForm;
