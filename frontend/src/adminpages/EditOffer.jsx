import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { updateOffer } from "../api/offerService";
import { Alert } from "react-bootstrap";
import ButtonSpinner from "../components/common/ButtonSpinner";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";

const EditOffer = () => {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const offerData = location.state?.offerData;
  const { t } = useTranslation();

  const [kind, setKind] = useState("");
  const [cost, setCost] = useState("");
  const [editOfferError, setEditOfferError] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isInitialLoading, setIsInitialLoading] = useState(true);

  useEffect(() => {
    if (offerData) {
      setKind(offerData.kind);
      setCost(offerData.cost);
    }
    setIsInitialLoading(false);
  }, [offerData]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await updateOffer(offerData.idOffer, { kind, cost }, user.token);
      navigate("/adminpanel");
    } catch (error) {
      setEditOfferError(true);
    } finally {
      setIsLoading(false);
    }
  };

  if (isInitialLoading) {
    return <LoadingSpinner text={t("admin.common.loadingData")} />;
  }

  if (!offerData) {
    return (
      <div className="container mt-5 text-center">
        <Alert variant="warning">{t("admin.common.noData")} </Alert>
      </div>
    );
  }

  return (
    <>
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3">
            <h2 className="text-center">{t("admin.offers.editTitle")}</h2>
            <Alert
              variant="danger"
              show={editOfferError}
              onClose={() => setEditOfferError(false)}
              dismissible
            >
              {t("admin.messages.editError")}
            </Alert>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="inputkind" className="form-label">
                  {t("admin.offers.kind")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="kind"
                  name="kind"
                  value={kind}
                  onChange={(e) => setKind(e.target.value)}
                  required
                  disabled={isLoading}
                />
              </div>
              <div className="mb-3">
                <label htmlFor="inputcost" className="form-label">
                  {t("admin.offers.cost")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="cost"
                  name="cost"
                  value={cost}
                  onChange={(e) => setCost(e.target.value)}
                  required
                  disabled={isLoading}
                />
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

export default EditOffer;
