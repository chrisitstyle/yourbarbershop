import { useState, useEffect } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { userResetPasswordRequest } from "../api/userService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";

const ResetPasswordForm = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [token, setToken] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isResetLinkInvalid, setIsResetLinkInvalid] = useState(false);

  const { t } = useTranslation();

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const tokenFromUrl = searchParams.get("token") || "";

    setToken(tokenFromUrl);

    if (!tokenFromUrl) {
      setIsResetLinkInvalid(true);
      toast.error(t("auth.missingToken"));
    }
  }, [location.search, t]);

  const isBadRequestError = (error) => {
    return error?.status === 400 || error?.response?.status === 400;
  };

  const handleResetPasswordForm = async (e) => {
    e.preventDefault();

    if (!token) {
      setIsResetLinkInvalid(true);
      toast.error(t("auth.missingToken"));
      return;
    }

    if (newPassword !== confirmPassword) {
      toast.error(t("auth.passwordsDoNotMatch"));
      return;
    }

    setIsLoading(true);

    try {
      await userResetPasswordRequest(token, newPassword, confirmPassword);

      toast.success(t("auth.passwordChangedSuccess"));
      navigate("/login", { replace: true });
    } catch (err) {
      if (isBadRequestError(err)) {
        setIsResetLinkInvalid(true);
        toast.error(t("auth.invalidOrExpiredResetLink"));
        return;
      }

      toast.error(t("auth.passwordChangedError"));
    } finally {
      setIsLoading(false);
    }
  };

  const isSubmitDisabled =
    !token || !newPassword || !confirmPassword || isLoading;

  if (isResetLinkInvalid) {
    return (
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3 text-center">
            <h4 className="display-6 text-center">
              {t("auth.changePasswordHeader")}
            </h4>

            <p className="mb-3">{t("auth.requestNewResetLinkInfo")}</p>

            <Link to="/forgotpassword" className="btn btn-dark">
              {t("auth.requestNewResetLink")}
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-4 border p-3">
          <h4 className="display-6 text-center">
            {t("auth.changePasswordHeader")}
          </h4>

          <form onSubmit={handleResetPasswordForm}>
            <div className="mb-3">
              <label htmlFor="inputPassword" className="form-label">
                {t("auth.newPassword")}
              </label>

              <input
                type="password"
                className="form-control"
                id="inputPassword"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                autoComplete="new-password"
                required
                disabled={isLoading}
              />
            </div>

            <div className="mb-3">
              <label htmlFor="inputConfirmPassword" className="form-label">
                {t("auth.confirmPassword")}
              </label>

              <input
                type="password"
                className="form-control"
                id="inputConfirmPassword"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                autoComplete="new-password"
                required
                disabled={isLoading}
              />
            </div>

            <ButtonSpinner
              type="submit"
              variant="dark"
              className="mx-auto d-block"
              loading={isLoading}
              loadingText={t("auth.saving")}
              disabled={isSubmitDisabled}
            >
              {t("auth.changePasswordBtn")}
            </ButtonSpinner>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ResetPasswordForm;
