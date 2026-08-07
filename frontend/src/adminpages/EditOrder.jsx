import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { updateOrder } from "../api/orderService";
import useOffers from "../hooks/useOffers";
import { format } from "date-fns-tz";
import { formatSelectedDateTime } from "../api/dataParser";
import { Alert } from "react-bootstrap";
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

const EditOrder = () => {
  const location = useLocation();
  const orderData = location.state?.orderData;
  const navigate = useNavigate();
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const { offers = [], isLoading: isLoadingOffers } = useOffers();

  const [selectedOffer, setSelectedOffer] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedHour, setSelectedHour] = useState(8);
  const [selectedMinute, setSelectedMinute] = useState(0);
  const [selectedStatus, setSelectedStatus] = useState("");

  const { isTerminalOrder, isOfferChangeBlocked, canComplete, canCancel } =
    getOrderModificationRules(orderData);

  const currentOfferId = orderData?.offer?.idOffer ?? null;

  const currentOfferLabel = orderData?.offer
    ? `${orderData.offer.kind} - ${orderData.offer.cost} ${t(
        "common.currency",
      )}`
    : t("admin.common.none");

  useEffect(() => {
    if (!orderData) {
      return;
    }

    setSelectedOffer(orderData.offer?.idOffer || "");

    if (orderData.visitDate) {
      const visitDate = new Date(orderData.visitDate);

      setSelectedDate(format(visitDate, "yyyy-MM-dd"));

      // set time from orderdata
      setSelectedHour(visitDate.getHours());
      setSelectedMinute(visitDate.getMinutes());
    }

    setSelectedStatus(orderData.orderStatus || "");
  }, [orderData]);

  const handleHourChange = (event) => {
    setSelectedHour(Number.parseInt(event.target.value, 10));
  };

  const handleMinuteChange = (event) => {
    setSelectedMinute(Number.parseInt(event.target.value, 10));
  };

  const updateOrderMutation = useMutation({
    mutationFn: (updatedOrder) => updateOrder(orderData.idOrder, updatedOrder),
    onSuccess: () => {
      // invalidate orders query cache so tables automatically update
      queryClient.invalidateQueries({
        queryKey: ["orders"],
      });

      toast.success(
        t("admin.messages.editSuccess", "Pomyślnie zapisano zmiany."),
      );

      navigate("/adminpanel");
    },
    onError: (error) => {
      console.error("error updating order:", error);

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

    updateOrderMutation.mutate({
      idOffer: offerIdToSend,
      visitDate: formatSelectedDateTime(
        selectedDate,
        selectedHour,
        selectedMinute,
      ),
      orderStatus: selectedStatus,
    });
  };

  if (!orderData) {
    return (
      <div className="container mt-5 text-center">
        <Alert variant="warning">{t("admin.common.noData")}</Alert>
      </div>
    );
  }

  if (isLoadingOffers && !isOfferChangeBlocked) {
    return <LoadingSpinner text={t("admin.common.loadingData")} />;
  }

  const isFormDisabled = isTerminalOrder || updateOrderMutation.isPending;

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-md-5">
          <div className="card card-accent-top">
            <div className="card-header py-3">
              <h5 className="card-title text-center mb-0 fw-semibold">
                {t("admin.orders.editTitle", {
                  id: orderData.idOrder,
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
                <div className="mb-3">
                  <label htmlFor="selectOffer" className="form-label">
                    {t("orders.selectService")}
                  </label>

                  {isOfferChangeBlocked ? (
                    <input
                      type="text"
                      className="form-control"
                      id="selectOffer"
                      value={currentOfferLabel}
                      readOnly
                      aria-describedby="selectOfferHelp"
                    />
                  ) : (
                    <select
                      className="form-select"
                      id="selectOffer"
                      value={selectedOffer}
                      onChange={(event) => setSelectedOffer(event.target.value)}
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
                    <div id="selectOfferHelp" className="form-text">
                      {t("admin.orderRules.offerLocked")}
                    </div>
                  )}
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
                      className="form-select"
                      id="selecthour"
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
                      className="form-select"
                      id="selectminute"
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
                  <label htmlFor="selectstatus" className="form-label">
                    {t("admin.common.status")}
                  </label>

                  <select
                    className="form-select"
                    id="selectstatus"
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
                  loading={updateOrderMutation.isPending}
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

export default EditOrder;
