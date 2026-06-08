import { useEffect, useRef, useState } from "react";
import { AdminLogoutConfirmModal } from "../../components/AdminLogoutConfirmModal.jsx";
import { LogoutIcon } from "../../components/AdminAuthIcons.jsx";
import { useAdminLogout } from "../../hooks/useAdminLogout.js";
import { useAuthSession } from "../../hooks/useAuthSession.jsx";

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

export function AdminShell({ children }) {
  const { user } = useAuthSession();
  const { performAdminLogout, isLoggingOut } = useAdminLogout();
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const searchRef = useRef(null);

  const displayName = getDisplayName(user);
  const displayEmail = user?.email?.trim() || "admin@2hands.vn";
  const initials = getUserInitials(user);

  useEffect(() => {
    const onKeyDown = (event) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        searchRef.current?.focus();
      }
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, []);

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-center gap-4 rounded-xl border border-outline-variant/60 bg-surface-container-lowest px-4 py-3 shadow-[0_1px_3px_rgba(0,0,0,0.06)] sm:gap-6 sm:px-6">
        <div className="shrink-0">
          <p className="text-xl font-bold leading-tight text-primary">2Hands Admin</p>
          <p className="text-[10px] font-semibold uppercase tracking-[0.14em] text-on-surface-variant">
            Admin Console
          </p>
        </div>

        <div className="order-last min-w-0 flex-1 basis-full md:order-none md:max-w-xl md:basis-auto lg:max-w-2xl">
          <label className="relative block">
            <span className="sr-only">Search across console</span>
            <SearchIcon className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-on-surface-variant" />
            <input
              ref={searchRef}
              type="search"
              placeholder="Search across console..."
              className="w-full rounded-full border border-outline-variant/70 bg-surface-container-low py-2.5 pl-11 pr-16 text-sm text-on-surface outline-none transition-colors placeholder:text-on-surface-variant/70 focus:border-primary/40 focus:bg-white focus:ring-2 focus:ring-primary/15"
            />
            <kbd className="pointer-events-none absolute right-3 top-1/2 hidden -translate-y-1/2 rounded-md border border-outline-variant/80 bg-surface-container-lowest px-1.5 py-0.5 text-[11px] font-medium text-on-surface-variant sm:inline">
              ⌘K
            </kbd>
          </label>
        </div>

        <div className="ml-auto flex shrink-0 items-center gap-3 sm:gap-4">
          <button
            type="button"
            className="relative rounded-lg p-2 text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-on-surface"
            aria-label="Thông báo"
          >
            <BellIcon />
            <span
              className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-error ring-2 ring-surface-container-lowest"
              aria-hidden="true"
            />
          </button>

          <span className="hidden h-8 w-px bg-outline-variant/80 sm:block" aria-hidden="true" />

          <div className="hidden items-center gap-3 sm:flex">
            <div className="text-right">
              <p className="text-sm font-semibold leading-tight text-on-surface">{displayName}</p>
              <p className="text-xs text-on-surface-variant">{displayEmail}</p>
            </div>
            <div
              className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary text-sm font-semibold text-on-primary"
              aria-hidden="true"
            >
              {initials}
            </div>
          </div>

          <button
            type="button"
            onClick={() => setIsConfirmOpen(true)}
            disabled={isLoggingOut}
            className="inline-flex items-center gap-2 rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm font-medium text-on-surface transition-colors hover:bg-surface-container-low disabled:cursor-not-allowed disabled:opacity-60"
          >
            <LogoutIcon className="h-4 w-4" />
            <span className="hidden sm:inline">Đăng xuất</span>
          </button>
        </div>
      </header>

      {children}

      <AdminLogoutConfirmModal
        open={isConfirmOpen}
        isLoggingOut={isLoggingOut}
        onCancel={() => setIsConfirmOpen(false)}
        onConfirm={async () => {
          await performAdminLogout();
          setIsConfirmOpen(false);
        }}
      />
    </div>
  );
}
