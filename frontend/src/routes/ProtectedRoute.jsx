import { Navigate, Outlet, useLocation } from "react-router-dom";
import useAuth from "@/context/useAuth";

// <ProtectedRoute />                          any logged-in user
// <ProtectedRoute allowedRoles={["ADMIN"]} /> only these roles
export default function ProtectedRoute({ allowedRoles }) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}