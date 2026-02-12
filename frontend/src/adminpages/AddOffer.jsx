import { useState } from "react";
import { useNavigate } from "react-router-dom";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";

const AddOffer = ({ onAddOffer }) => {
  const [kind, setKind] = useState("");
  const [cost, setCost] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { t } = useTranslation();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await onAddOffer({ kind, cost });
      setKind("");
      setCost("");
      navigate("/adminpanel");
    } catch (error) {
      console.error("Błąd dodawania oferty:", error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3 ">
            <h4 className="text-center">{t("admin.offers.addTitle")}</h4>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="inputkind" className="form-label">
                  {t("admin.offers.kind")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="kind"
                  value={kind}
                  onChange={(e) => setKind(e.target.value)}
                  required
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
                  value={cost}
                  onChange={(e) => setCost(e.target.value)}
                  required
                />
              </div>
              <ButtonSpinner
                type="submit"
                variant="dark"
                className="mx-auto d-block"
                loading={isLoading}
                loadingText={t("admin.common.adding")}
              >
                {t("admin.common.add")}
              </ButtonSpinner>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default AddOffer;
