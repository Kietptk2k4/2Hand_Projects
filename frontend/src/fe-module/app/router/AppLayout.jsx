import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { APP_ROUTES } from "../../shared/constants/routes";
import { useAuthSession } from "../../features/auth/hooks/useAuthSession.jsx";
import { SessionExpiredModal } from "../../features/auth/components/SessionExpiredModal.jsx";
import { AppFooter } from "../../shared/ui/AppFooter.jsx";
import { AppHeader } from "../../shared/ui/AppHeader.jsx";

export function AppLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { sessionExpiredState, hideSessionExpired } = useAuthSession();
  const isAuthImmersiveRoute =
    location.pathname === APP_ROUTES.login ||
    location.pathname === APP_ROUTES.register ||
    location.pathname === APP_ROUTES.verifyEmail;

  const isSocialRoute =
    location.pathname === APP_ROUTES.socialFeed ||
    location.pathname.startsWith(`${APP_ROUTES.socialFeed}/`);
  const isWideLayoutRoute =
    location.pathname.startsWith(APP_ROUTES.account) ||
    location.pathname.startsWith(APP_ROUTES.admin) ||
    isSocialRoute;
  const isSensitiveRoute = location.pathname.startsWith(APP_ROUTES.account);
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
      <div className="flex min-h-screen flex-col bg-surface text-on-surface">
        <AppHeader />
        <main className="flex-1 px-4 py-4 sm:px-6 sm:py-6 lg:px-8 lg:py-8">
          <Outlet />
        </main>
        <AppFooter />
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
    <div className="flex min-h-screen flex-col bg-surface text-on-surface">
      <AppHeader />

      <main
        className={[
          "mx-auto w-full flex-1",
          isSocialRoute ? "max-w-none p-0" : "px-4 py-8",
          !isSocialRoute && isWideLayoutRoute ? "max-w-[1280px]" : "",
          !isSocialRoute && !isWideLayoutRoute ? "max-w-6xl" : "",
        ]
          .filter(Boolean)
          .join(" ")}
      >
        <Outlet />
      </main>
      <AppFooter />
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

