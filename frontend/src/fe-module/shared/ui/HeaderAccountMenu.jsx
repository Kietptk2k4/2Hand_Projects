import { useCallback, useEffect, useId, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useLogout } from "../../features/auth/hooks/useLogout.js";
import { PrimaryButton } from "./auth/authUi.jsx";
import { APP_ROUTES } from "../constants/routes";

const MENU_ITEMS = [
  { label: "Profile Setting", to: APP_ROUTES.account },
  { label: "Account Security", to: APP_ROUTES.accountSecurity },
  { label: "Account Password", to: APP_ROUTES.accountPassword },
];

export function HeaderAccountMenu({ avatarUrl, isAuthenticated, userLabel = "" }) {
  const navigate = useNavigate();
  const { performLogout, isLoggingOut } = useLogout();
  const menuId = useId();
  const rootRef = useRef(null);
  const [isOpen, setIsOpen] = useState(false);
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);

  const closeMenu = useCallback(() => {
    setIsOpen(false);
  }, []);

  useEffect(() => {
    if (!isOpen) return undefined;

    const onPointerDown = (event) => {
      if (rootRef.current && !rootRef.current.contains(event.target)) {
        closeMenu();
      }
    };

    const onKeyDown = (event) => {
      if (event.key === "Escape") {
        closeMenu();
      }
    };

    document.addEventListener("mousedown", onPointerDown);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("mousedown", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [isOpen, closeMenu]);

  const onTriggerClick = () => {
    if (!isAuthenticated) {
      navigate(APP_ROUTES.login);
      return;
    }
    setIsOpen((prev) => !prev);
  };

  const onTriggerKeyDown = (event) => {
    if (!isAuthenticated) return;
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      setIsOpen((prev) => !prev);
    }
  };

  const handleLogoutClick = () => {
    closeMenu();
    setIsConfirmOpen(true);
  };

  const handleConfirmLogout = async () => {
    await performLogout();
    setIsConfirmOpen(false);
  };

  const displayLabel = userLabel?.trim();

  return (
    <div ref={rootRef} className="relative ml-1">
      <button
        type="button"
        onClick={onTriggerClick}
        onKeyDown={onTriggerKeyDown}
        className="flex h-9 w-9 overflow-hidden rounded-full border border-header-border ring-2 ring-transparent transition hover:ring-primary/20 focus:outline-none focus:ring-primary/30"
        aria-label={isAuthenticated ? "Account menu" : "Sign in"}
        aria-haspopup={isAuthenticated ? "menu" : undefined}
        aria-expanded={isAuthenticated ? isOpen : undefined}
        aria-controls={isAuthenticated && isOpen ? menuId : undefined}
      >
        <img src={avatarUrl} alt="" className="h-full w-full object-cover" />
      </button>

      {isAuthenticated && isOpen ? (
        <div
          id={menuId}
          role="menu"
          className="absolute right-0 top-full z-[60] mt-2 min-w-[220px] overflow-hidden rounded-lg border border-header-border bg-white shadow-md"
        >
          {displayLabel ? (
            <div className="border-b border-header-border px-4 py-3">
              <p className="truncate text-sm font-semibold text-on-surface">{displayLabel}</p>
            </div>
          ) : null}

          <nav className="py-1" aria-label="Account">
            {MENU_ITEMS.map((item) => (
              <Link
                key={item.to}
                to={item.to}
                role="menuitem"
                onClick={closeMenu}
                className="flex min-h-[44px] items-center px-4 py-2.5 text-sm text-on-surface transition-colors hover:bg-account-surface-low focus:bg-account-surface-low focus:outline-none"
              >
                {item.label}
              </Link>
            ))}
          </nav>

          <div className="border-t border-header-border py-1">
            <button
              type="button"
              role="menuitem"
              onClick={handleLogoutClick}
              disabled={isLoggingOut}
              className="flex min-h-[44px] w-full items-center px-4 py-2.5 text-left text-sm font-medium text-account-danger transition-colors hover:bg-red-50 focus:bg-red-50 focus:outline-none disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isLoggingOut ? "Dang xuat..." : "Logout"}
            </button>
          </div>
        </div>
      ) : null}

      {isConfirmOpen ? (
        <div
          className="fixed inset-0 z-[100] flex items-center justify-center bg-on-surface/40 p-4 backdrop-blur-sm"
          role="dialog"
          aria-modal="true"
          aria-labelledby="logout-confirm-title"
          onClick={(e) => {
            if (e.target === e.currentTarget && !isLoggingOut) setIsConfirmOpen(false);
          }}
        >
          <div className="w-full max-w-md overflow-hidden rounded-xl bg-white shadow-lg">
            <div className="p-6">
              <h3 id="logout-confirm-title" className="text-lg font-semibold text-on-surface">
                Dang xuat?
              </h3>
              <p className="mt-2 text-sm text-on-surface-variant">
                Ban co chac chan muon dang xuat khoi tai khoan tren thiet bi nay?
              </p>
            </div>
            <div className="flex justify-end gap-3 border-t border-outline-variant bg-account-surface-low px-6 py-4">
              <button
                type="button"
                disabled={isLoggingOut}
                onClick={() => setIsConfirmOpen(false)}
                className="rounded-lg px-4 py-2 text-sm font-medium text-on-surface-variant hover:text-on-surface"
              >
                Huy
              </button>
              <PrimaryButton type="button" onClick={handleConfirmLogout} loading={isLoggingOut}>
                Dang xuat
              </PrimaryButton>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
