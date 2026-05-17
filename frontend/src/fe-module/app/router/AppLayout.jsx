import { Link, Outlet } from "react-router-dom";
import { APP_ROUTES } from "../../shared/constants/routes";

export function AppLayout() {
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
    </div>
  );
}

