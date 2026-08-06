import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { useAuth } from "../auth/AuthContextValue";
import { updateOrder } from "../api/orderService";
import useOffers from "../hooks/useOffers";
import { format } from "date-fns-tz";
import { formatSelectedDateTime } from "../api/dataParser";
import { Alert } from "react-bootstrap";
import ButtonSpinner from "../components/common/ButtonSpinner";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";
import "./styles/AdminForms.css";

const EditOrder = () => {
  const { user } = useAuth();
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

  useEffect(() => {
    if (orderData) {
      setSelectedOffer(orderData.offer?.idOffer || "");

      if (orderData.visitDate) {
        setSelectedDate(format(new Date(orderData.visitDate), "yyyy-MM-dd"));

        // set time from orderdata
        const hours = new Date(orderData.visitDate).getHours();
        const minutes = new Date(orderData.visitDate).getMinutes();
        setSelectedHour(hours);
        setSelectedMinute(minutes);
      }

      setSelectedStatus(orderData.status || "");
    }
  }, [orderData]);

  const handleHourChange = (e) => {
    setSelectedHour(Number.parseInt(e.target.value, 10));
  };

  const handleMinuteChange = (e) => {
    setSelectedMinute(Number.parseInt(e.target.value, 10));
  };

  const updateOrderMutation = useMutation({
    mutationFn: (updatedOrder) =>
      updateOrder(orderData.idOrder, updatedOrder, user?.token),
    onSuccess: () => {
      // invalidate orders query cache so tables automatically update
      queryClient.invalidateQueries({ queryKey: ["orders"] });

      toast.success(
        t("admin.messages.editSuccess", "Pomyślnie zapisano zmiany."),
      );

      navigate("/adminpanel");
    },
    onError: (error) => {
      console.error("error updating order:", error);
      const errorMsg = error?.data || error?.message;

      if (typeof errorMsg === "string" && errorMsg) {
        toast.error(errorMsg);
      } else {
        toast.error(t("admin.messages.editError"));
      }
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();

    updateOrderMutation.mutate({
      idOffer: Number(selectedOffer),
      visitDate: formatSelectedDateTime(
        selectedDate,
        selectedHour,
        selectedMinute,
      ),
      status: selectedStatus,
    });
  };

  if (!orderData) {
    return (
      <div className="container mt-5 text-center">
        <Alert variant="warning">{t("admin.common.noData")}</Alert>
      </div>
    );
  }

  if (isLoadingOffers) {
    return <LoadingSpinner text={t("admin.common.loadingData")} />;
  }

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-md-5">
          <div className="card card-accent-top">
            <div className="card-header py-3">
              <h5 className="card-title text-center mb-0 fw-semibold">
                {t("admin.orders.editTitle", { id: orderData.idOrder })}
              </h5>
            </div>

            <div className="card-body p-4">
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="selectOffer" className="form-label">
                    {t("orders.selectService")}
                  </label>

                  <select
                    className="form-select"
                    id="selectOffer"
                    value={selectedOffer}
                    onChange={(e) => setSelectedOffer(e.target.value)}
                    required
                    disabled={updateOrderMutation.isPending}
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
                  <label htmlFor="selectdate" className="form-label">
                    {t("orders.selectDate")}
                  </label>

                  <input
                    type="date"
                    className="form-control"
                    id="selectdate"
                    value={selectedDate}
                    onChange={(e) => setSelectedDate(e.target.value)}
                    required
                    disabled={updateOrderMutation.isPending}
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
                      disabled={updateOrderMutation.isPending}
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
                      disabled={updateOrderMutation.isPending}
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
                    onChange={(e) => setSelectedStatus(e.target.value)}
                    required
                    disabled={updateOrderMutation.isPending}
                  >
                    {["NOWE", "ZREALIZOWANE", "ANULOWANE"].map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                </div>

                <ButtonSpinner
                  type="submit"
                  variant="primary"
                  className="d-block mx-auto px-4"
                  loading={updateOrderMutation.isPending}
                  loadingText={t("admin.common.saving")}
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
