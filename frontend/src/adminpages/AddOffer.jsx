import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";
import "./styles/AdminForms.css";

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
      toast.success(t("admin.messages.addOfferSuccess"));
      setKind("");
      setCost("");
      navigate("/adminpanel");
    },
    onError: (error) => {
      console.error("error adding offer:", error);

      const errorMsg = error?.data || error?.message;

      if (typeof errorMsg === "string" && errorMsg) {
        toast.error(errorMsg);
      } else {
        toast.error(t("admin.messages.addOfferError"));
      }
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    addOfferMutation.mutate({ kind, cost });
  };

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-md-5">
          <div className="card card-accent-top">
            <div className="card-header py-3">
              <h5 className="card-title text-center mb-0 fw-semibold">
                {t("admin.offers.addTitle")}
              </h5>
            </div>

            <div className="card-body p-4">
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="ao-kind" className="form-label">
                    {t("admin.offers.kind")}
                  </label>
                  <input
                    type="text"
                    id="ao-kind"
                    className="form-control"
                    value={kind}
                    onChange={(e) => setKind(e.target.value)}
                    placeholder={t("admin.offers.kindPlaceholder", {
                      defaultValue: "np. Strzyżenie + broda",
                    })}
                    required
                    disabled={addOfferMutation.isPending}
                  />
                </div>

                <div className="mb-4">
                  <label htmlFor="ao-cost" className="form-label">
                    {t("admin.offers.cost")}
                  </label>
                  <div className="input-group">
                    <input
                      type="number"
                      id="ao-cost"
                      className="form-control"
                      value={cost}
                      onChange={(e) => setCost(e.target.value)}
                      placeholder="0.00"
                      min="0"
                      step="0.01"
                      required
                      disabled={addOfferMutation.isPending}
                    />
                    <span className="input-group-text">
                      {t("common.currency")}
                    </span>
                  </div>
                  <div className="form-text">
                    {t("admin.offers.costHelp", {
                      defaultValue: "Cena widoczna dla klientów w cenniku.",
                    })}
                  </div>
                </div>

                <ButtonSpinner
                  type="submit"
                  variant="primary"
                  loading={addOfferMutation.isPending}
                  loadingText={t("admin.common.adding")}
                  className="d-block mx-auto px-4"
                >
                  {t("admin.offers.addBtn", {
                    defaultValue: "Dodaj ofertę",
                  })}
                </ButtonSpinner>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AddOffer;
