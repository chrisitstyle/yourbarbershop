import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../auth/AuthContextValue.js";
import { Alert } from "react-bootstrap";
import { updateUser } from "../api/userService";
import { useTranslation } from "react-i18next";

const ROLES = ["USER", "ADMIN"];

const EditUser = () => {
  const { user } = useAuth();
  const location = useLocation();
  const userData = location.state?.userData;
  const navigate = useNavigate();
  const [editUserError, setEditUserError] = useState(false);
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const [firstname, setFirstName] = useState("");
  const [lastname, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [selectedRole, setSelectedRole] = useState("");

  useEffect(() => {
    if (userData) {
      setFirstName(userData.firstname);
      setLastName(userData.lastname);
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
      navigate("/adminpanel");
    },
    onError: () => {
      setEditUserError(true);
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
    <>
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-4 border p-3 ">
            <h4 className="text-center">{t("admin.users.editTitle")}</h4>
            <Alert
              variant="danger"
              show={editUserError}
              onClose={() => setEditUserError(false)}
              dismissible
            >
              {t("admin.messages.editError")}
            </Alert>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="firstname" className="form-label">
                  {t("auth.firstname")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="firstname"
                  value={firstname}
                  onChange={(e) => setFirstName(e.target.value)}
                  required
                  disabled={updateUserMutation.isPending}
                />
              </div>
              <div className="mb-3">
                <label htmlFor="lastname" className="form-label">
                  {t("auth.lastname")}
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="lastname"
                  value={lastname}
                  onChange={(e) => setLastName(e.target.value)}
                  required
                  disabled={updateUserMutation.isPending}
                />
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

              <div className="mb-3">
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

              <button
                type="submit"
                className="btn btn-dark mx-auto d-block"
                disabled={updateUserMutation.isPending}
              >
                {t("admin.common.save")}
              </button>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default EditUser;
