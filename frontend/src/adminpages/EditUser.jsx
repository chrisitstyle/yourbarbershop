import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { useAuth } from "../auth/AuthContextValue.js";
import { updateUser } from "../api/userService";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";
import "./styles/AdminForms.css";

const ROLES = ["USER", "ADMIN"];

const EditUser = () => {
  const { user } = useAuth();
  const location = useLocation();
  const userData = location.state?.userData;
  const navigate = useNavigate();
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const [firstname, setFirstname] = useState("");
  const [lastname, setLastname] = useState("");
  const [email, setEmail] = useState("");
  const [selectedRole, setSelectedRole] = useState("");

  useEffect(() => {
    if (userData) {
      setFirstname(userData.firstname);
      setLastname(userData.lastname);
      setEmail(userData.email);

      if (ROLES.includes(userData.role)) {
        setSelectedRole(userData.role);
      }
    } else if (user?.role && ROLES.includes(user.role)) {
      // setting logged-in user role
      setSelectedRole(user.role);
    }
  }, [user?.role, userData]);

  const updateUserMutation = useMutation({
    mutationFn: (updatedUser) => updateUser(userData.idUser, updatedUser),
    onSuccess: () => {
      // invalidate users query cache so tables automatically update
      queryClient.invalidateQueries({ queryKey: ["users"] });
      toast.success(t("admin.messages.editSuccess"));
      navigate("/adminpanel");
    },
    onError: (error) => {
      console.error("error updating user:", error);
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

    updateUserMutation.mutate({
      firstname,
      lastname,
      email,
      role: selectedRole,
    });
  };

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-md-5">
          <div className="card card-accent-top">
            <div className="card-header py-3">
              <h5 className="card-title text-center mb-0 fw-semibold">
                {t("admin.users.editTitle")}
              </h5>
            </div>

            <div className="card-body p-4">
              <form onSubmit={handleSubmit}>
                <div className="row g-3 mb-3">
                  <div className="col">
                    <label htmlFor="firstname" className="form-label">
                      {t("auth.firstname")}
                    </label>

                    <input
                      type="text"
                      className="form-control"
                      id="firstname"
                      value={firstname}
                      onChange={(e) => setFirstname(e.target.value)}
                      required
                      disabled={updateUserMutation.isPending}
                    />
                  </div>

                  <div className="col">
                    <label htmlFor="lastname" className="form-label">
                      {t("auth.lastname")}
                    </label>

                    <input
                      type="text"
                      className="form-control"
                      id="lastname"
                      value={lastname}
                      onChange={(e) => setLastname(e.target.value)}
                      required
                      disabled={updateUserMutation.isPending}
                    />
                  </div>
                </div>

                <div className="mb-3">
                  <label htmlFor="email" className="form-label">
                    {t("auth.email")}
                  </label>

                  <input
                    type="email"
                    className="form-control"
                    id="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    disabled={updateUserMutation.isPending}
                  />
                </div>

                <div className="mb-4">
                  <label htmlFor="role" className="form-label">
                    {t("admin.users.role")}
                  </label>

                  <select
                    className="form-select"
                    id="role"
                    value={selectedRole}
                    onChange={(e) => setSelectedRole(e.target.value)}
                    required
                    disabled={updateUserMutation.isPending}
                  >
                    {ROLES.map((role) => (
                      <option key={role} value={role}>
                        {role === "USER" ? t("admin.users.roleUser") : role}
                      </option>
                    ))}
                  </select>
                </div>

                <ButtonSpinner
                  type="submit"
                  variant="primary"
                  className="d-block mx-auto px-4"
                  loading={updateUserMutation.isPending}
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

export default EditUser;
