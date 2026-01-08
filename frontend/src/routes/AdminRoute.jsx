import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { isTokenValid } from "../utils/jwt";

const AdminRoute = () => {
  const { user } = useAuth();
  const isAdmin = user && user.role === "ADMIN";
  const hasValidToken = isTokenValid(user?.token);

  if (!user || !user.token || !isAdmin || !hasValidToken) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

export default AdminRoute;
