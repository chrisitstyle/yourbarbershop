import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../auth/AuthContextValue";
import { updateGuestOrder } from "../api/guestOrderService";
import { format } from "date-fns-tz";
import { formatSelectedDateTime } from "../api/dataParser";
import { Alert } from "react-bootstrap";
import useOffers from "../hooks/useOffers";
import ButtonSpinner from "../components/common/ButtonSpinner";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";

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
  const [editGuestOrderError, setEditGuestOrderError] = useState(false);
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
    setSelectedHour(parseInt(e.target.value, 10));
  };

  const handleMinuteChange = (e) => {
    setSelectedMinute(parseInt(e.target.value, 10));
  };

  const editGuestOrderMutation = useMutation({
    mutationFn: (updatedData) =>
      updateGuestOrder(guestOrderData.idGuestOrder, updatedData, user?.token),
    onSuccess: () => {
      // invalidate guest orders query cache so tables automatically update
      queryClient.invalidateQueries({ queryKey: ["guestOrders"] });
      navigate("/adminpanel");
    },
    onError: () => {
      setEditGuestOrderError(true);
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
    <>
      <div className="container mt-2">
        <div className="row justify-content-center">
          <div className="col-md-5 border p-3">
            <h4 className="text-center">
              {t("admin.guestOrders.editTitle", {
                id: guestOrderData?.idGuestOrder,
              })}
            </h4>
            <Alert
              variant="danger"
              show={editGuestOrderError}
              onClose={() => setEditGuestOrderError(false)}
              dismissible
            >
              {t("admin.messages.editError")}
            </Alert>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="inputfirstname" className="form-label">
                  {t("auth.firstname")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="inputfirstname"
                  value={firstname}
                  onChange={(e) => setFirstname(e.target.value)}
                  required
                  disabled={editGuestOrderMutation.isPending}
                />
              </div>

              <div className="mb-3">
                <label htmlFor="inputlastname" className="form-label">
                  {t("auth.lastname")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="inputlastname"
                  value={lastname}
                  onChange={(e) => setLastname(e.target.value)}
                  required
                  disabled={editGuestOrderMutation.isPending}
                />
              </div>

              <div className="mb-3">
                <label htmlFor="inputphonenumber" className="form-label">
                  {t("orders.phonenumber")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="inputphonenumber"
                  value={phonenumber}
                  onChange={(e) => setPhonenumber(e.target.value)}
                  required
                  disabled={editGuestOrderMutation.isPending}
                />
              </div>

              <div className="mb-3">
                <label htmlFor="inputemail" className="form-label">
                  {t("auth.email")}
                </label>
                <input
                  type="email"
                  className="form-control"
                  id="inputemail"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  disabled={editGuestOrderMutation.isPending}
                />
              </div>

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
                  disabled={editGuestOrderMutation.isPending}
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
                    onChange={handleHourChange}
                    required
                    disabled={editGuestOrderMutation.isPending}
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
                    onChange={handleMinuteChange}
                    required
                    disabled={editGuestOrderMutation.isPending}
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
                <label htmlFor="selectstatus" className="form-label">
                  {t("admin.common.status")}
                </label>
                <select
                  className="form-select"
                  id="selectstatus"
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
                variant="dark"
                className="mx-auto d-block"
                loading={editGuestOrderMutation.isPending}
                loadingText={t("admin.common.saving")}
              >
                {t("admin.common.save")}
              </ButtonSpinner>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default EditGuestOrder;
