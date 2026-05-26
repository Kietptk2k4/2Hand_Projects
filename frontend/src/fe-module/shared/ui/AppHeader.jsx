import { Link } from "react-router-dom";
import { APP_ROUTES } from "../constants/routes";
import { useAuthSession } from "../../features/auth/hooks/useAuthSession.jsx";

const DEFAULT_AVATAR_URL = "https://i.pravatar.cc/96?img=11";

const NAV_LINKS = [
  { label: "Dashboard", to: APP_ROUTES.home },
  { label: "Services", to: "#services" },
  { label: "My Bookings", to: "#bookings" },
];

function SearchIcon() {
  return (
    <svg
      className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-header-muted"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      aria-hidden="true"
    >
      <circle cx="11" cy="11" r="7" />
      <path d="M20 20L17 17" strokeLinecap="round" />
    </svg>
  );
}

function HeaderIconButton({ label, children, to }) {
  const className =
    "flex h-9 w-9 items-center justify-center rounded-full text-header-nav transition-colors hover:bg-header-border/60";

  if (to) {
    return (
      <Link to={to} className={className} aria-label={label}>
        {children}
      </Link>
    );
  }

  return (
    <button type="button" className={className} aria-label={label}>
      {children}
    </button>
  );
}

function BellIcon() {
  return (
    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" aria-hidden="true">
      <path d="M18 8a6 6 0 10-12 0c0 7-3 9-3 9h18s-3-2-3-9" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M13.73 21a2 2 0 01-3.46 0" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function HelpIcon() {
  return (
    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" aria-hidden="true">
      <circle cx="12" cy="12" r="9" />
      <path d="M9.5 9.5a2.5 2.5 0 014.5 1.5c0 2-2.5 2-2.5 4" strokeLinecap="round" />
      <circle cx="12" cy="17" r="0.75" fill="currentColor" stroke="none" />
    </svg>
  );
}

function SettingsIcon() {
  return (
    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" aria-hidden="true">
      <circle cx="12" cy="12" r="3" />
      <path
        d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"
        strokeLinecap="round"
      />
    </svg>
  );
}

export function AppHeader({ className = "" }) {
  const { isAuthenticated, user } = useAuthSession();
  const avatarUrl = user?.avatar_url || user?.profile?.avatar_url || DEFAULT_AVATAR_URL;
  const profileLink = isAuthenticated ? APP_ROUTES.account : APP_ROUTES.login;

  return (
    <header
      className={[
        "sticky top-0 z-50 w-full border-b border-header-border bg-header-surface",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <div className="mx-auto flex h-16 w-full max-w-[1280px] items-center justify-between gap-3 px-4 md:gap-6 md:px-8">
        <div className="flex min-w-0 flex-1 items-center gap-4 md:gap-6">
          <Link to={APP_ROUTES.home} className="shrink-0 text-xl font-bold text-header-brand md:text-2xl">
            2Hands
          </Link>

          <label className="relative hidden min-w-0 max-w-[280px] flex-1 sm:block lg:max-w-[320px]">
            <span className="sr-only">Tìm kiếm</span>
            <SearchIcon />
            <input
              type="search"
              name="global-search"
              placeholder="Tìm kiếm..."
              className="w-full rounded-lg border border-header-border bg-white py-2 pl-10 pr-3 text-sm text-on-surface outline-none transition placeholder:text-header-muted focus:border-primary focus:ring-1 focus:ring-primary/30"
            />
          </label>
        </div>

        <div className="flex shrink-0 items-center gap-4 md:gap-6">
          <nav
            className="hidden items-center gap-6 text-sm font-medium text-header-nav md:flex"
            aria-label="Main"
          >
            {NAV_LINKS.map((item) =>
              item.to.startsWith("#") ? (
                <a key={item.label} href={item.to} className="transition-colors hover:text-header-brand">
                  {item.label}
                </a>
              ) : (
                <Link key={item.label} to={item.to} className="transition-colors hover:text-header-brand">
                  {item.label}
                </Link>
              )
            )}
          </nav>

          <div className="flex items-center gap-2 md:gap-3">
            <HeaderIconButton label="Thông báo">
              <BellIcon />
            </HeaderIconButton>
            <HeaderIconButton label="Trợ giúp">
              <HelpIcon />
            </HeaderIconButton>
            <HeaderIconButton label="Cài đặt" to={APP_ROUTES.account}>
              <SettingsIcon />
            </HeaderIconButton>

            <Link
              to={profileLink}
              className="ml-1 flex h-9 w-9 overflow-hidden rounded-full border border-header-border ring-2 ring-transparent transition hover:ring-primary/20"
              aria-label={isAuthenticated ? "Tài khoản của tôi" : "Đăng nhập"}
            >
              <img src={avatarUrl} alt="" className="h-full w-full object-cover" />
            </Link>
          </div>
        </div>
      </div>
    </header>
  );
}
