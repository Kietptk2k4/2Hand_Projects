import { Link, Navigate, Outlet, useLocation } from "react-router-dom";
import { ADMIN_ACCESS_DENIED_MESSAGE } from "../../features/auth/constants/adminAuthUiStrings";
import { useAuthSession } from "../../features/auth/hooks/useAuthSession.jsx";
import { hasAdminPortalAccess } from "../../features/auth/utils/adminSession";
import { APP_ROUTES } from "../../shared/constants/routes";
import { AuthAlert } from "../../shared/ui/auth/authUi.jsx";

export function AdminAuthGuard() {
  const { isAuthenticated, user, isAdminSession } = useAuthSession();
  const location = useLocation();

  if (!isAuthenticated) {
    const redirectTarget = `${location.pathname}${location.search}`;
    const loginUrl = `${APP_ROUTES.adminLogin}?redirectUrl=${encodeURIComponent(redirectTarget)}`;
    return <Navigate to={loginUrl} replace />;
  }

  const canAccessAdmin =
    isAdminSession || hasAdminPortalAccess({ roles: user?.roles, permissions: user?.permissions });

  if (!canAccessAdmin) {
    return (
      <div className="mx-auto max-w-lg space-y-4 py-12">
        <AuthAlert variant="error" title="Không có quyền truy cập" message={ADMIN_ACCESS_DENIED_MESSAGE} />
        <p className="text-center text-sm">
          <Link to={APP_ROUTES.adminLogin} className="font-medium text-primary hover:underline">
            Đăng nhập admin portal
          </Link>
        </p>
      </div>
    );
  }

  return <Outlet />;
}
