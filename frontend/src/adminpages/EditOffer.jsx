import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { useAuth } from "../auth/AuthContextValue";
import { updateOffer } from "../api/offerService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";
import "./styles/AdminForms.css";

const EditOffer = () => {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const offerData = location.state?.offerData;
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const [kind, setKind] = useState("");
  const [cost, setCost] = useState("");
  const [isInitialLoading, setIsInitialLoading] = useState(true);

  useEffect(() => {
    if (offerData) {
      setKind(offerData.kind);
      setCost(offerData.cost);
    }

    setIsInitialLoading(false);
  }, [offerData]);

  const updateOfferMutation = useMutation({
    mutationFn: (updatedOffer) =>
      updateOffer(offerData.idOffer, updatedOffer, user?.token),
    onSuccess: () => {
      // invalidate offers query cache so tables update automatically
      queryClient.invalidateQueries({ queryKey: ["offers"] });

      toast.success(t("admin.messages.editSuccess"));

      navigate("/adminpanel");
    },
    onError: (error) => {
      console.error("error updating offer:", error);

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

    updateOfferMutation.mutate({
      kind,
      cost,
    });
  };

  if (isInitialLoading) {
    return <LoadingSpinner text={t("admin.common.loadingData")} />;
  }

  if (!offerData) {
    return (
      <div className="container mt-5">
        <div className="alert alert-danger text-center">
          {t("admin.common.noData")}
        </div>
      </div>
    );
  }

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-md-5">
          <div className="card card-accent-top">
            <div className="card-header py-3">
              <h5 className="card-title text-center mb-0 fw-semibold">
                {t("admin.offers.editTitle")}
              </h5>
            </div>

            <div className="card-body p-4">
              <div className="alert alert-warning small mb-4" role="alert">
                <strong>
                  {t(
                    "admin.offers.editNoticeTitle",
                    "Zmiana dotyczy tylko nowych zamówień.",
                  )}
                </strong>

                <div className="mt-1">
                  {t("admin.offers.editNoticeDescription")}
                </div>
              </div>

              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="kind" className="form-label">
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
                    disabled={updateOfferMutation.isPending}
                  />
                </div>

                <div className="mb-4">
                  <label htmlFor="cost" className="form-label">
                    {t("admin.offers.cost")}
                  </label>

                  <div className="input-group">
                    <input
                      type="number"
                      className="form-control"
                      id="cost"
                      name="cost"
                      value={cost}
                      onChange={(e) => setCost(e.target.value)}
                      min="0"
                      step="0.01"
                      required
                      disabled={updateOfferMutation.isPending}
                    />

                    <span className="input-group-text">
                      {t("common.currency")}
                    </span>
                  </div>

                  <div className="form-text">{t("admin.offers.costHint")}</div>
                </div>

                <ButtonSpinner
                  type="submit"
                  variant="primary"
                  className="d-block mx-auto px-4"
                  loading={updateOfferMutation.isPending}
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

export default EditOffer;
