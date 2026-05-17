import { Link, Outlet, useLocation } from "react-router-dom";
import { APP_ROUTES } from "../../shared/constants/routes";
import { useAuthSession } from "../../features/auth/hooks/useAuthSession.jsx";
import { SessionExpiredModal } from "../../features/auth/components/SessionExpiredModal.jsx";
import { useNavigate } from "react-router-dom";

export function AppLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { sessionExpiredState, hideSessionExpired } = useAuthSession();
  const isAuthImmersiveRoute =
    location.pathname === APP_ROUTES.login ||
    location.pathname === APP_ROUTES.register ||
    location.pathname === APP_ROUTES.verifyEmail;

  const isSensitiveRoute = location.pathname.startsWith("/account");
  const allowClose = !isSensitiveRoute;

  const onSignIn = () => {
    hideSessionExpired();
    navigate(APP_ROUTES.login, { replace: true });
  };

  const onClose = () => {
    if (!allowClose) {
      onSignIn();
      return;
    }
    hideSessionExpired();
  };

  if (isAuthImmersiveRoute) {
    return (
      <div className="min-h-screen bg-surface text-on-surface">
        <main className="px-4 py-4 sm:px-6 sm:py-6 lg:px-8 lg:py-8">
          <Outlet />
        </main>
        <SessionExpiredModal
          open={sessionExpiredState.isOpen}
          message={sessionExpiredState.message}
          allowClose={allowClose}
          onSignIn={onSignIn}
          onClose={onClose}
        />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-surface text-on-surface">
      <header className="border-b border-outline-variant/40 bg-surface-container-low">
        <nav className="mx-auto flex w-full max-w-6xl items-center justify-between px-4 py-3">
          <Link to={APP_ROUTES.home} className="text-lg font-semibold text-primary">
            2Hands FE Module
          </Link>
          <div className="flex items-center gap-4 text-sm text-on-surface-variant">
            <Link to={APP_ROUTES.login}>Login</Link>
            <Link to={APP_ROUTES.register}>Register</Link>
            <Link to={APP_ROUTES.account}>Account</Link>
          </div>
        </nav>
      </header>

      <main className="mx-auto w-full max-w-6xl px-4 py-8">
        <Outlet />
      </main>
      <SessionExpiredModal
        open={sessionExpiredState.isOpen}
        message={sessionExpiredState.message}
        allowClose={allowClose}
        onSignIn={onSignIn}
        onClose={onClose}
      />
    </div>
  );
}

