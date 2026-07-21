import { LogoutIcon } from "../../components/AdminAuthIcons.jsx";
import { useAuthSession } from "../../hooks/useAuthSession.jsx";
import { useAdminShell } from "./AdminShellContext.jsx";

function getDisplayName(user) {
  const name = user?.display_name?.trim();
  if (name) return name;
  return "Admin User";
}

function getUserInitials(user) {
  const name = user?.display_name?.trim();
  if (name) {
    const parts = name.split(/\s+/).filter(Boolean);
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  }

  const email = user?.email?.trim() || "";
  if (email) {
    return email.slice(0, 2).toUpperCase();
  }

  return "AU";
}

function SearchIcon({ className = "h-4 w-4" }) {
  return (
    <svg className={className} fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function BellIcon({ className = "h-5 w-5" }) {
  return (
    <svg className={className} fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export function AdminMainHeader() {
  const { user } = useAuthSession();
  const { searchRef, isLoggingOut, openLogoutConfirm } = useAdminShell();

  const displayName = getDisplayName(user);
  const displayEmail = user?.email?.trim() || "admin@2hands.vn";
  const initials = getUserInitials(user);

  return (
    <header className="sticky top-0 z-20 flex h-14 shrink-0 items-center gap-3 border-b border-admin-border-subtle bg-admin-surface px-4 shadow-[var(--shadow-admin-header)] sm:gap-4 sm:px-6 lg:px-8">
      <div className="min-w-0 flex-1">
        <label className="relative block max-w-xl">
          <span className="sr-only">Search across console</span>
          <SearchIcon className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-admin-text-muted" />
          <input
            ref={searchRef}
            type="search"
            placeholder="Search across console..."
            className="w-full rounded-lg border border-admin-border bg-admin-surface-raised py-2 pl-10 pr-14 text-sm text-admin-text outline-none transition-colors placeholder:text-admin-text-muted focus:border-admin-accent-border focus:bg-admin-surface focus:ring-2 focus:ring-admin-accent-soft"
          />
          <kbd className="pointer-events-none absolute right-2.5 top-1/2 hidden -translate-y-1/2 rounded border border-admin-border bg-admin-surface-muted px-1.5 py-0.5 text-[10px] font-medium text-admin-text-muted sm:inline">
            ⌘K
          </kbd>
        </label>
      </div>

      <div className="flex shrink-0 items-center gap-2 sm:gap-3">
        <button
          type="button"
          className="relative rounded-lg p-2 text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
          aria-label="Thông báo"
        >
          <BellIcon className="h-5 w-5" />
          <span
            className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-admin-danger ring-2 ring-admin-surface"
            aria-hidden="true"
          />
        </button>

        <span className="hidden h-7 w-px bg-admin-border sm:block" aria-hidden="true" />

        <div className="hidden items-center gap-2.5 sm:flex">
          <div className="text-right">
            <p className="text-sm font-medium leading-tight text-admin-text">{displayName}</p>
            <p className="text-xs text-admin-text-muted">{displayEmail}</p>
          </div>
          <div
            className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-admin-accent-soft text-xs font-semibold text-admin-accent-strong"
            aria-hidden="true"
          >
            {initials}
          </div>
        </div>

        <button
          type="button"
          onClick={openLogoutConfirm}
          disabled={isLoggingOut}
          className="inline-flex items-center gap-2 rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft disabled:cursor-not-allowed disabled:opacity-60"
        >
          <LogoutIcon className="h-4 w-4" />
          <span className="hidden sm:inline">Đăng xuất</span>
        </button>
      </div>
    </header>
  );
}
