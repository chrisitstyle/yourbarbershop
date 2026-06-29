import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContextValue";
import { updateOrder } from "../api/orderService";
import { getOffers } from "../api/offerService";
import { format } from "date-fns-tz";
import { formatSelectedDateTime } from "../api/dataParser";
import { Alert } from "react-bootstrap";
import ButtonSpinner from "../components/common/ButtonSpinner";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";

const EditOrder = () => {
  const { user } = useAuth();
  const location = useLocation();
  const orderData = location.state?.orderData;
  const navigate = useNavigate();
  const { t } = useTranslation();

  const [offers, setOffers] = useState([]);
  const [selectedOffer, setSelectedOffer] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedHour, setSelectedHour] = useState(8);
  const [selectedMinute, setSelectedMinute] = useState(0);
  const [editOrderError, setEditOrderError] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingOffers, setIsLoadingOffers] = useState(true);

  useEffect(() => {
    const fetchOffers = async () => {
      if (!orderData) {
        setIsLoadingOffers(false);
        return;
      }

      try {
        const offersData = await getOffers();
        setOffers(offersData);
        setSelectedOffer(orderData.offer?.idOffer || "");
        setSelectedDate(format(new Date(orderData.visitDate), "yyyy-MM-dd"));

        // Set time from orderData
        const hours = new Date(orderData.visitDate).getHours();
        const minutes = new Date(orderData.visitDate).getMinutes();
        setSelectedHour(hours);
        setSelectedMinute(minutes);
        setSelectedStatus(orderData.status);
      } catch (error) {
        console.error("Błąd ładowania ofert:", error);
      } finally {
        setIsLoadingOffers(false);
      }
    };

    fetchOffers();
  }, [orderData]);

  const handleHourChange = (e) => {
    setSelectedHour(parseInt(e.target.value, 10));
  };

  const handleMinuteChange = (e) => {
    setSelectedMinute(parseInt(e.target.value, 10));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await updateOrder(
        orderData.idOrder,
        {
          user: { idUser: orderData.user.idUser },
          offer: { idOffer: selectedOffer },
          orderDate: orderData.orderDate,
          visitDate: formatSelectedDateTime(
            selectedDate,
            selectedHour,
            selectedMinute,
          ),
          status: selectedStatus,
        },
        user.token,
      );

      navigate("/adminpanel");
    } catch {
      setEditOrderError(true);
    } finally {
      setIsLoading(false);
    }
  };

  if (!orderData) {
    return (
      <div className="container mt-5 text-center">
        <Alert variant="warning">{t("admin.common.noData")} </Alert>
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
          <div className="col-md-4 border p-3">
            <h4 className="text-center">
              {t("admin.orders.editTitle", { id: orderData.idOrder })}
            </h4>
            <Alert
              variant="danger"
              show={editOrderError}
              onClose={() => setEditOrderError(false)}
              dismissible
            >
              {t("admin.messages.editError")}
            </Alert>
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
                  disabled={isLoading}
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
                  disabled={isLoading}
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
                    disabled={isLoading}
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
                    disabled={isLoading}
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
                  disabled={isLoading}
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
                loading={isLoading}
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

export default EditOrder;
