import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { isTokenValid } from "../utils/jwt";
import LoadingSpinner from "../components/common/LoadingSpinner";

const AdminRoute = () => {
  const { user, authLoading } = useAuth();
  const isAdmin = user && user.role === "ADMIN";
  const hasValidToken = isTokenValid(user?.token);

  if (authLoading) {
    return <LoadingSpinner text="Ładowanie..." />;
  }

  if (!user || !user.token || !isAdmin || !hasValidToken) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

export default AdminRoute;
