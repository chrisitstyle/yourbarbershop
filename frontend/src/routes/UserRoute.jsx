import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { isTokenValid } from "../utils/jwt";

const UserRoute = () => {
  const { user } = useAuth();
  const hasValidToken = isTokenValid(user?.token);

  if (!user || !user.token || !hasValidToken) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

export default UserRoute;
