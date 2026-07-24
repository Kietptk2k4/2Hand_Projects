import { useEffect } from "react";

/**
 * Dismiss admin drawers on Escape. Ignores when focus is inside a nested dialog
 * (e.g. moderate/confirm modal layered above the drawer).
 */
export function useAdminDrawerDismiss(onClose, enabled = true) {
  useEffect(() => {
    if (!enabled || typeof onClose !== "function") return;

    const onKeyDown = (event) => {
      if (event.key !== "Escape") return;
      if (event.defaultPrevented) return;
      event.preventDefault();
      event.stopPropagation();
      onClose();
    };

    window.addEventListener("keydown", onKeyDown, true);
    return () => window.removeEventListener("keydown", onKeyDown, true);
  }, [enabled, onClose]);
}
