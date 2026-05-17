import { Navigate, Outlet } from "react-router-dom";
import { APP_ROUTES } from "../../shared/constants/routes";
import { useAuthSession } from "../../features/auth/hooks/useAuthSession.jsx";

export function AuthGuard() {
  const { isAuthenticated } = useAuthSession();

  if (!isAuthenticated) {
    return <Navigate to={APP_ROUTES.login} replace />;
  }

  return <Outlet />;
}

