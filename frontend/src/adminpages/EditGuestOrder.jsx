import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { updateGuestOrder } from "../api/guestOrderService";
import { format } from "date-fns-tz";
import { formatSelectedDateTime } from "../api/dataParser";
import { Alert } from "react-bootstrap";
import useOffers from "../hooks/useOffers";
import useGuestOrder from "../hooks/useGuestOrder";
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
  const navigate = useNavigate();
  const { id } = useParams();

  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const guestOrderId = Number(id);
  const initialGuestOrderData = location.state?.guestOrderData;

  const {
    data: guestOrderData,
    isLoading: isLoadingGuestOrder,
    error: guestOrderError,
  } = useGuestOrder(guestOrderId, initialGuestOrderData);

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

  const isFormDirtyRef = useRef(false);

  const { isTerminalOrder, isOfferChangeBlocked, canComplete, canCancel } =
    getOrderModificationRules(guestOrderData);

  const currentOfferId = guestOrderData?.offer?.idOffer ?? null;

  const currentOfferLabel = guestOrderData?.offer
    ? `${guestOrderData.offer.kind} - ${guestOrderData.offer.cost} ${t(
        "common.currency",
      )}`
    : t("admin.common.none");

  useEffect(() => {
    if (!guestOrderData || isFormDirtyRef.current) {
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

  const handleFirstnameChange = (event) => {
    isFormDirtyRef.current = true;
    setFirstname(event.target.value);
  };

  const handleLastnameChange = (event) => {
    isFormDirtyRef.current = true;
    setLastname(event.target.value);
  };

  const handlePhonenumberChange = (event) => {
    isFormDirtyRef.current = true;
    setPhonenumber(event.target.value);
  };

  const handleEmailChange = (event) => {
    isFormDirtyRef.current = true;
    setEmail(event.target.value);
  };

  const handleOfferChange = (event) => {
    isFormDirtyRef.current = true;
    setSelectedOffer(event.target.value);
  };

  const handleDateChange = (event) => {
    isFormDirtyRef.current = true;
    setSelectedDate(event.target.value);
  };

  const handleHourChange = (event) => {
    isFormDirtyRef.current = true;
    setSelectedHour(Number.parseInt(event.target.value, 10));
  };

  const handleMinuteChange = (event) => {
    isFormDirtyRef.current = true;
    setSelectedMinute(Number.parseInt(event.target.value, 10));
  };

  const handleStatusChange = (event) => {
    isFormDirtyRef.current = true;
    setSelectedStatus(event.target.value);
  };

  const editGuestOrderMutation = useMutation({
    mutationFn: (updatedData) => updateGuestOrder(guestOrderId, updatedData),
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

    if (selectedStatus === "ZREALIZOWANE" && !canComplete) {
      toast.error(t("admin.orderRules.cannotComplete"));
      return;
    }

    if (selectedStatus === "ANULOWANE" && !canCancel) {
      toast.error(t("admin.orderRules.cannotCancel"));
      return;
    }

    const offerIdToSend = Number(
      isOfferChangeBlocked ? currentOfferId : selectedOffer,
    );

    if (!Number.isInteger(offerIdToSend) || offerIdToSend <= 0) {
      toast.error(t("admin.orderRules.missingOffer"));
      return;
    }

    editGuestOrderMutation.mutate({
      firstname,
      lastname,
      phonenumber,
      email,
      idOffer: offerIdToSend,
      visitDate: formatSelectedDateTime(
        selectedDate,
        selectedHour,
        selectedMinute,
      ),
      orderStatus: selectedStatus,
    });
  };

  if (!Number.isInteger(guestOrderId) || guestOrderId <= 0) {
    return (
      <div className="container mt-5 text-center">
        <Alert variant="warning">{t("admin.common.noData")}</Alert>
      </div>
    );
  }

  if (isLoadingGuestOrder) {
    return <LoadingSpinner text={t("admin.common.loadingData")} />;
  }

  if (guestOrderError && !guestOrderData) {
    return (
      <div className="container mt-5 text-center">
        <Alert variant="danger">
          {getErrorMessage(guestOrderError) || t("admin.common.noData")}
        </Alert>
      </div>
    );
  }

  if (!guestOrderData) {
    return (
      <div className="container mt-5 text-center">
        <Alert variant="warning">{t("admin.common.noData")}</Alert>
      </div>
    );
  }

  if (isLoadingOffers && !isOfferChangeBlocked) {
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
                      onChange={handleFirstnameChange}
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
                      onChange={handleLastnameChange}
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
                      onChange={handlePhonenumberChange}
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
                      onChange={handleEmailChange}
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

                  {isOfferChangeBlocked ? (
                    <input
                      type="text"
                      id="ego-offer"
                      className="form-control"
                      value={currentOfferLabel}
                      readOnly
                      aria-describedby="ego-offer-help"
                    />
                  ) : (
                    <select
                      id="ego-offer"
                      className="form-select"
                      value={selectedOffer}
                      onChange={handleOfferChange}
                      required
                      disabled={isFormDisabled}
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
                  )}

                  {isOfferChangeBlocked && (
                    <div id="ego-offer-help" className="form-text">
                      {t("admin.orderRules.offerLocked")}
                    </div>
                  )}
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
                    onChange={handleDateChange}
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
                    onChange={handleStatusChange}
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
