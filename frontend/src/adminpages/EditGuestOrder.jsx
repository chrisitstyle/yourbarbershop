import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { updateGuestOrder } from "../api/guestOrderService";
import { format } from "date-fns-tz";
import { formatSelectedDateTime } from "../api/dataParser";
import { Alert } from "react-bootstrap";
import useOffers from "../hooks/useOffers";
import ButtonSpinner from "../components/common/ButtonSpinner";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";
import { getOrderModificationRules } from "./utils/orderModificationRules";
import "./styles/AdminForms.css";

const ORDER_STATUSES = ["NOWE", "ZREALIZOWANE", "ANULOWANE"];

const getErrorMessage = (error) => {
  if (typeof error?.data === "string") {
    return error.data;
  }

  return error?.data?.message ?? error?.data?.error ?? error?.message ?? null;
};

const EditGuestOrder = () => {
  const location = useLocation();
  const guestOrderData = location.state?.guestOrderData;

  const navigate = useNavigate();
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const { offers = [], isLoading: isLoadingOffers } = useOffers();

  const [firstname, setFirstname] = useState("");
  const [lastname, setLastname] = useState("");
  const [phonenumber, setPhonenumber] = useState("");
  const [email, setEmail] = useState("");
  const [selectedOffer, setSelectedOffer] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedHour, setSelectedHour] = useState(8);
  const [selectedMinute, setSelectedMinute] = useState(0);
  const [selectedStatus, setSelectedStatus] = useState("");

  const { isTerminalOrder, isOfferChangeBlocked, canComplete, canCancel } =
    getOrderModificationRules(guestOrderData);

  useEffect(() => {
    if (!guestOrderData) {
      return;
    }

    setFirstname(guestOrderData.firstname || "");
    setLastname(guestOrderData.lastname || "");
    setPhonenumber(guestOrderData.phonenumber || "");
    setEmail(guestOrderData.email || "");
    setSelectedOffer(guestOrderData.offer?.idOffer || "");

    if (guestOrderData.visitDate) {
      const visitDate = new Date(guestOrderData.visitDate);

      setSelectedDate(format(visitDate, "yyyy-MM-dd"));
      setSelectedHour(visitDate.getHours());
      setSelectedMinute(visitDate.getMinutes());
    }

    setSelectedStatus(guestOrderData.orderStatus || "");
  }, [guestOrderData]);

  const handleHourChange = (event) => {
    setSelectedHour(Number.parseInt(event.target.value, 10));
  };

  const handleMinuteChange = (event) => {
    setSelectedMinute(Number.parseInt(event.target.value, 10));
  };

  const editGuestOrderMutation = useMutation({
    mutationFn: (updatedData) =>
      updateGuestOrder(guestOrderData.idGuestOrder, updatedData),
    onSuccess: () => {
      // invalidate guest orders query cache so tables automatically update
      queryClient.invalidateQueries({
        queryKey: ["guestOrders"],
      });

      toast.success(
        t("admin.messages.editSuccess", "Pomyślnie zapisano zmiany."),
      );

      navigate("/adminpanel");
    },
    onError: (error) => {
      console.error("error updating guest order:", error);

      const errorMessage = getErrorMessage(error);

      toast.error(errorMessage || t("admin.messages.editError"));
    },
  });

  const handleSubmit = (event) => {
    event.preventDefault();

    if (isTerminalOrder) {
      toast.error(t("admin.orderRules.terminal"));
      return;
    }

    const offerChanged =
      Number(selectedOffer) !== Number(guestOrderData.offer?.idOffer);

    if (offerChanged && isOfferChangeBlocked) {
      toast.error(t("admin.orderRules.offerLocked"));
      return;
    }

    if (selectedStatus === "ZREALIZOWANE" && !canComplete) {
      toast.error(t("admin.orderRules.cannotComplete"));
      return;
    }

    if (selectedStatus === "ANULOWANE" && !canCancel) {
      toast.error(t("admin.orderRules.cannotCancel"));
      return;
    }

    editGuestOrderMutation.mutate({
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
      orderStatus: selectedStatus,
    });
  };

  if (!guestOrderData) {
    return (
      <div className="container mt-5 text-center">
        <Alert variant="warning">{t("admin.common.noData")}</Alert>
      </div>
    );
  }

  if (isLoadingOffers) {
    return <LoadingSpinner text={t("admin.common.loadingData")} />;
  }

  const isFormDisabled = isTerminalOrder || editGuestOrderMutation.isPending;

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card card-accent-top">
            <div className="card-header py-3">
              <h5 className="card-title text-center mb-0 fw-semibold">
                {t("admin.guestOrders.editTitle", {
                  id: guestOrderData.idGuestOrder,
                })}
              </h5>
            </div>

            <div className="card-body p-4">
              {isTerminalOrder && (
                <Alert variant="warning">
                  {t("admin.orderRules.terminal")}
                </Alert>
              )}

              {!isTerminalOrder && isOfferChangeBlocked && (
                <Alert variant="info">
                  {t("admin.orderRules.offerLocked")}
                </Alert>
              )}

              <form onSubmit={handleSubmit}>
                {/* guest details */}
                <div className="row g-3 mb-3">
                  <div className="col">
                    <label htmlFor="ego-firstname" className="form-label">
                      {t("auth.firstname")}
                    </label>

                    <input
                      type="text"
                      id="ego-firstname"
                      className="form-control"
                      value={firstname}
                      onChange={(event) => setFirstname(event.target.value)}
                      required
                      disabled={isFormDisabled}
                    />
                  </div>

                  <div className="col">
                    <label htmlFor="ego-lastname" className="form-label">
                      {t("auth.lastname")}
                    </label>

                    <input
                      type="text"
                      id="ego-lastname"
                      className="form-control"
                      value={lastname}
                      onChange={(event) => setLastname(event.target.value)}
                      required
                      disabled={isFormDisabled}
                    />
                  </div>
                </div>

                <div className="row g-3 mb-4">
                  <div className="col">
                    <label htmlFor="ego-phone" className="form-label">
                      {t("orders.phoneNumber")}
                    </label>

                    <input
                      type="tel"
                      id="ego-phone"
                      className="form-control"
                      value={phonenumber}
                      onChange={(event) => setPhonenumber(event.target.value)}
                      required
                      disabled={isFormDisabled}
                    />
                  </div>

                  <div className="col">
                    <label htmlFor="ego-email" className="form-label">
                      {t("auth.email")}
                    </label>

                    <input
                      type="email"
                      id="ego-email"
                      className="form-control"
                      value={email}
                      onChange={(event) => setEmail(event.target.value)}
                      required
                      disabled={isFormDisabled}
                    />
                  </div>
                </div>

                <hr className="my-4" />

                {/* appointment details */}
                <div className="mb-3">
                  <label htmlFor="ego-offer" className="form-label">
                    {t("orders.selectService")}
                  </label>

                  <select
                    id="ego-offer"
                    className="form-select"
                    value={selectedOffer}
                    onChange={(event) => setSelectedOffer(event.target.value)}
                    required
                    disabled={isFormDisabled || isOfferChangeBlocked}
                  >
                    <option value="" disabled>
                      {t("orders.selectService")}
                    </option>

                    {offers.map((offer) => (
                      <option key={offer.idOffer} value={offer.idOffer}>
                        {offer.kind} - {offer.cost} {t("common.currency")}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="mb-3">
                  <label htmlFor="ego-date" className="form-label">
                    {t("orders.selectDate")}
                  </label>

                  <input
                    type="date"
                    id="ego-date"
                    className="form-control"
                    value={selectedDate}
                    onChange={(event) => setSelectedDate(event.target.value)}
                    required
                    disabled={isFormDisabled}
                  />
                </div>

                <div className="mb-3">
                  <label className="form-label">
                    {t("orders.selectTimeFull")}
                  </label>

                  <div className="input-group">
                    <select
                      id="ego-hour"
                      className="form-select"
                      value={selectedHour}
                      onChange={handleHourChange}
                      required
                      disabled={isFormDisabled}
                    >
                      {[...new Array(12).keys()].map((hour) => (
                        <option key={hour} value={hour + 8}>
                          {String(hour + 8).padStart(2, "0")}
                        </option>
                      ))}
                    </select>

                    <span className="input-group-text">:</span>

                    <select
                      id="ego-minute"
                      className="form-select"
                      value={selectedMinute}
                      onChange={handleMinuteChange}
                      required
                      disabled={isFormDisabled}
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
                  <label htmlFor="ego-status" className="form-label">
                    {t("admin.common.status")}
                  </label>

                  <select
                    id="ego-status"
                    className="form-select"
                    value={selectedStatus}
                    onChange={(event) => setSelectedStatus(event.target.value)}
                    required
                    disabled={isFormDisabled}
                  >
                    {ORDER_STATUSES.map((status) => {
                      const isCompletionBlocked =
                        status === "ZREALIZOWANE" && !canComplete;

                      const isCancellationBlocked =
                        status === "ANULOWANE" && !canCancel;

                      return (
                        <option
                          key={status}
                          value={status}
                          disabled={
                            isCompletionBlocked || isCancellationBlocked
                          }
                        >
                          {t(`enums.${status}`, {
                            defaultValue: status,
                          })}
                        </option>
                      );
                    })}
                  </select>

                  {!isTerminalOrder && !canComplete && (
                    <div className="form-text">
                      {t("admin.orderRules.cannotComplete")}
                    </div>
                  )}

                  {!isTerminalOrder && !canCancel && (
                    <div className="form-text">
                      {t("admin.orderRules.cannotCancel")}
                    </div>
                  )}
                </div>

                <ButtonSpinner
                  type="submit"
                  variant="primary"
                  className="d-block mx-auto px-4"
                  loading={editGuestOrderMutation.isPending}
                  loadingText={t("admin.common.saving")}
                  disabled={isFormDisabled}
                >
                  {t("admin.common.save")}
                </ButtonSpinner>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EditGuestOrder;
