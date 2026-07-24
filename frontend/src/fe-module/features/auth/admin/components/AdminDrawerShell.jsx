import { useCallback } from "react";
import { AdminDrawerPortal } from "./AdminDrawerPortal.jsx";
import { useAdminDrawerDismiss } from "../hooks/useAdminDrawerDismiss.js";

/**
 * Shared shell for admin detail drawers: portal + Escape + backdrop close.
 * Desktop overlay starts after the sidebar (lg:left-64).
 */
export function AdminDrawerShell({
  open,
  labelledBy,
  onClose,
  children,
  panelClassName = "sm:max-w-xl",
}) {
  const handleClose = useCallback(() => {
    onClose?.();
  }, [onClose]);

  useAdminDrawerDismiss(handleClose, Boolean(open));

  if (!open) return null;

  return (
    <AdminDrawerPortal>
      <div
        className="fixed inset-0 z-[80] flex min-h-dvh items-stretch justify-end"
        role="dialog"
        aria-modal="true"
        aria-labelledby={labelledBy}
      >
        <button
          type="button"
          aria-label="Đóng chi tiết"
          className="absolute inset-0 bg-admin-text/40 backdrop-blur-sm lg:left-64"
          onClick={handleClose}
        />

        <aside
          className={[
            "relative z-10 flex h-full max-h-dvh w-full flex-col bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:border-l sm:border-admin-border",
            panelClassName,
          ]
            .filter(Boolean)
            .join(" ")}
          onMouseDown={(event) => event.stopPropagation()}
          onClick={(event) => event.stopPropagation()}
        >
          {typeof children === "function" ? children(handleClose) : children}
        </aside>
      </div>
    </AdminDrawerPortal>
  );
}
