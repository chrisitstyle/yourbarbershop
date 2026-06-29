import { Navigate, Outlet, useLocation } from "react-router-dom";

import { useAuth } from "../AuthContext";

import LoadingSpinner from "../components/common/LoadingSpinner";

const AdminRoute = () => {
  const { isLoggedIn, user, authLoading } = useAuth();

  const location = useLocation();

  const isAdmin = user?.role === "ADMIN";

  if (authLoading) {
    return <LoadingSpinner text="Ładowanie..." />;
  }

  if (!isLoggedIn || !user || !isAdmin) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return <Outlet />;
};

export default AdminRoute;
