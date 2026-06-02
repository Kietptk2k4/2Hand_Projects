import { Navigate, Outlet, useLocation } from "react-router-dom";
import { APP_ROUTES } from "../../shared/constants/routes";
import { useAuthSession } from "../../features/auth/hooks/useAuthSession.jsx";

export function AuthGuard() {
  const { isAuthenticated } = useAuthSession();
  const location = useLocation();

  if (!isAuthenticated) {
    const redirectTarget = `${location.pathname}${location.search}`;
    const loginUrl = `${APP_ROUTES.login}?redirectUrl=${encodeURIComponent(redirectTarget)}`;
    return <Navigate to={loginUrl} replace />;
  }

  return <Outlet />;
}

