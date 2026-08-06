import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import ButtonSpinner from "../components/common/ButtonSpinner";
import { useTranslation } from "react-i18next";
import "./styles/AdminForms.css";

const AddUser = ({ onSubmit }) => {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("USER");
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const addUserMutation = useMutation({
    mutationFn: (newUser) => onSubmit(newUser),
    onSuccess: () => {
      // invalidate users query cache so tables automatically update
      queryClient.invalidateQueries({ queryKey: ["users"] });
      toast.success(t("admin.messages.addUserSuccess"));
      setFirstName("");
      setLastName("");
      setEmail("");
      setPassword("");
    },
    onError: (error) => {
      console.error("error adding user:", error);
      const errorMsg = error?.data || error?.message;

      if (typeof errorMsg === "string" && errorMsg) {
        toast.error(errorMsg);
      } else {
        toast.error(t("admin.messages.addUserError"));
      }
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();

    const newUser = {
      firstname: firstName,
      lastname: lastName,
      email,
      password,
      role,
    };

    addUserMutation.mutate(newUser);
  };

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-md-5">
          <div className="card card-accent-top">
            <div className="card-header py-3">
              <h5 className="card-title text-center mb-0 fw-semibold">
                {t("admin.users.addTitle")}
              </h5>
            </div>

            <div className="card-body p-4">
              <form onSubmit={handleSubmit}>
                <div className="row g-3 mb-3">
                  <div className="col">
                    <label htmlFor="add-firstname" className="form-label">
                      {t("auth.firstname")}
                    </label>
                    <input
                      type="text"
                      id="add-firstname"
                      className="form-control"
                      value={firstName}
                      onChange={(e) => setFirstName(e.target.value)}
                      required
                      disabled={addUserMutation.isPending}
                    />
                  </div>

                  <div className="col">
                    <label htmlFor="add-lastname" className="form-label">
                      {t("auth.lastname")}
                    </label>
                    <input
                      type="text"
                      id="add-lastname"
                      className="form-control"
                      value={lastName}
                      onChange={(e) => setLastName(e.target.value)}
                      required
                      disabled={addUserMutation.isPending}
                    />
                  </div>
                </div>

                <div className="mb-3">
                  <label htmlFor="add-email" className="form-label">
                    {t("auth.email")}
                  </label>
                  <input
                    type="email"
                    id="add-email"
                    className="form-control"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    disabled={addUserMutation.isPending}
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="add-password" className="form-label">
                    {t("auth.password")}
                  </label>
                  <input
                    type="password"
                    id="add-password"
                    className="form-control"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    disabled={addUserMutation.isPending}
                  />
                </div>

                <div className="mb-4">
                  <label htmlFor="add-role" className="form-label">
                    {t("admin.users.role")}
                  </label>
                  <select
                    id="add-role"
                    className="form-select"
                    value={role}
                    onChange={(e) => setRole(e.target.value)}
                    disabled={addUserMutation.isPending}
                  >
                    <option value="USER">{t("admin.users.roleUser")}</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                </div>

                <ButtonSpinner
                  type="submit"
                  variant="primary"
                  className="d-block mx-auto px-4"
                  loading={addUserMutation.isPending}
                  loadingText={t("admin.common.adding")}
                >
                  {t("admin.users.addBtn")}
                </ButtonSpinner>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AddUser;
