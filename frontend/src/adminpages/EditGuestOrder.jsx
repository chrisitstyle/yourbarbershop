import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { useAuth } from "../auth/AuthContextValue";
import { updateGuestOrder } from "../api/guestOrderService";
import { format } from "date-fns-tz";
import { formatSelectedDateTime } from "../api/dataParser";
import { Alert } from "react-bootstrap";
import useOffers from "../hooks/useOffers";
import ButtonSpinner from "../components/common/ButtonSpinner";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";
import "./styles/AdminForms.css";

const EditGuestOrder = () => {
  const { user } = useAuth();
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

  useEffect(() => {
    if (guestOrderData) {
      setFirstname(guestOrderData.firstname || "");
      setLastname(guestOrderData.lastname || "");
      setPhonenumber(guestOrderData.phonenumber || "");
      setEmail(guestOrderData.email || "");
      setSelectedOffer(guestOrderData?.offer?.idOffer || "");

      if (guestOrderData?.visitDate) {
        setSelectedDate(
          format(new Date(guestOrderData.visitDate), "yyyy-MM-dd"),
        );
        const hours = new Date(guestOrderData.visitDate).getHours();
        const minutes = new Date(guestOrderData.visitDate).getMinutes();
        setSelectedHour(hours);
        setSelectedMinute(minutes);
      }

      setSelectedStatus(guestOrderData?.status || "");
    }
  }, [guestOrderData]);

  const handleHourChange = (e) => {
    setSelectedHour(Number.parseInt(e.target.value, 10));
  };

  const handleMinuteChange = (e) => {
    setSelectedMinute(Number.parseInt(e.target.value, 10));
  };

  const editGuestOrderMutation = useMutation({
    mutationFn: (updatedData) =>
      updateGuestOrder(guestOrderData.idGuestOrder, updatedData, user?.token),
    onSuccess: () => {
      // invalidate guest orders query cache so tables automatically update
      queryClient.invalidateQueries({ queryKey: ["guestOrders"] });
      toast.success(
        t("admin.messages.editSuccess", "Pomyślnie zapisano zmiany."),
      );
      navigate("/adminpanel");
    },
    onError: (error) => {
      console.error("error updating guest order:", error);
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

    const statusToSend = selectedStatus
      ? selectedStatus
      : guestOrderData.status;

    // payload
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
      status: statusToSend,
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

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card card-accent-top">
            <div className="card-header py-3">
              <h5 className="card-title text-center mb-0 fw-semibold">
                {t("admin.guestOrders.editTitle", {
                  id: guestOrderData?.idGuestOrder,
                })}
              </h5>
            </div>

            <div className="card-body p-4">
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
                      onChange={(e) => setFirstname(e.target.value)}
                      required
                      disabled={editGuestOrderMutation.isPending}
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
                      onChange={(e) => setLastname(e.target.value)}
                      required
                      disabled={editGuestOrderMutation.isPending}
                    />
                  </div>
                </div>

                <div className="row g-3 mb-4">
                  <div className="col">
                    <label htmlFor="ego-phone" className="form-label">
                      {t("orders.phonenumber")}
                    </label>
                    <input
                      type="tel"
                      id="ego-phone"
                      className="form-control"
                      value={phonenumber}
                      onChange={(e) => setPhonenumber(e.target.value)}
                      required
                      disabled={editGuestOrderMutation.isPending}
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
                      onChange={(e) => setEmail(e.target.value)}
                      required
                      disabled={editGuestOrderMutation.isPending}
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
                    onChange={(e) => setSelectedOffer(e.target.value)}
                    required
                    disabled={editGuestOrderMutation.isPending}
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
                    onChange={(e) => setSelectedDate(e.target.value)}
                    required
                    disabled={editGuestOrderMutation.isPending}
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
                      disabled={editGuestOrderMutation.isPending}
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
                      disabled={editGuestOrderMutation.isPending}
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
                    onChange={(e) => setSelectedStatus(e.target.value)}
                    required
                    disabled={editGuestOrderMutation.isPending}
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
                  loading={editGuestOrderMutation.isPending}
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

export default EditGuestOrder;
