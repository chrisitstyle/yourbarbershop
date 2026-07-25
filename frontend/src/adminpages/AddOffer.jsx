import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";

const AddOffer = ({ onAddOffer }) => {
  const [kind, setKind] = useState("");
  const [cost, setCost] = useState("");
  const navigate = useNavigate();
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const addOfferMutation = useMutation({
    mutationFn: (newOffer) => onAddOffer(newOffer),
    onSuccess: () => {
      // invalidate offers query cache so tables automatically update
      queryClient.invalidateQueries({ queryKey: ["offers"] });
      setKind("");
      setCost("");
      navigate("/adminpanel");
    },
    onError: (error) => {
      console.error("error adding offer:", error);
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    addOfferMutation.mutate({ kind, cost });
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
                loading={addOfferMutation.isPending}
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
