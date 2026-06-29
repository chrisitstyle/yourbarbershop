import { Navigate, Outlet, useLocation } from "react-router-dom";

import { useAuth } from "../auth/AuthContextValue";

import LoadingSpinner from "../components/common/LoadingSpinner";

const UserRoute = () => {
  const { isLoggedIn, user, authLoading } = useAuth();

  const location = useLocation();

  if (authLoading) {
    return <LoadingSpinner text="Ładowanie..." />;
  }

  if (!isLoggedIn || !user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return <Outlet />;
};

export default UserRoute;
