import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { useAuth } from "../auth/AuthContextValue";
import { updateOffer } from "../api/offerService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import LoadingSpinner from "../components/common/LoadingSpinner";
import { useTranslation } from "react-i18next";

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

      toast.success(
        t("admin.messages.editSuccess", "Pomyślnie zapisano zmiany."),
      );
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
    updateOfferMutation.mutate({ kind, cost });
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
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-4 border p-3">
          <h4 className="text-center">{t("admin.offers.editTitle")}</h4>

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

            <div className="mb-3">
              <label htmlFor="cost" className="form-label">
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
                disabled={updateOfferMutation.isPending}
              />
            </div>

            <ButtonSpinner
              type="submit"
              variant="dark"
              className="mx-auto d-block"
              loading={updateOfferMutation.isPending}
              loadingText={t("admin.common.saving")}
            >
              {t("admin.common.save")}
            </ButtonSpinner>
          </form>
        </div>
      </div>
    </div>
  );
};

export default EditOffer;
