import { useCallback, useEffect, useId, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { APP_ROUTES } from "../constants/routes";

const MENU_ITEMS = [
  { label: "Profile Setting", to: APP_ROUTES.account },
  { label: "Account Security", to: APP_ROUTES.accountSecurity },
  { label: "Account Password", to: APP_ROUTES.accountPassword },
];

export function HeaderAccountMenu({ avatarUrl, isAuthenticated, userLabel = "" }) {
  const navigate = useNavigate();
  const menuId = useId();
  const rootRef = useRef(null);
  const [isOpen, setIsOpen] = useState(false);

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
        </div>
      ) : null}
    </div>
  );
}
