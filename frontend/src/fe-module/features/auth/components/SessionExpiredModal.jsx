import { useEffect, useRef } from "react";
import {
  SESSION_EXPIRED_CLOSE,
  SESSION_EXPIRED_DEFAULT_MESSAGE,
  SESSION_EXPIRED_SIGN_IN,
  SESSION_EXPIRED_TITLE,
} from "../constants/authUiStrings";

export function SessionExpiredModal({ open, message, allowClose, onSignIn, onClose }) {
  const dialogRef = useRef(null);
  const signInButtonRef = useRef(null);

  useEffect(() => {
    if (!open) return;
    signInButtonRef.current?.focus();

    const handleKeyDown = (event) => {
      if (event.key === "Escape") {
        if (allowClose) {
          onClose?.();
        } else {
          onSignIn?.();
        }
        return;
      }

      if (event.key === "Tab" && dialogRef.current) {
        const focusable = dialogRef.current.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
        if (focusable.length === 0) return;
        const first = focusable[0];
        const last = focusable[focusable.length - 1];

        if (event.shiftKey && document.activeElement === first) {
          event.preventDefault();
          last.focus();
        } else if (!event.shiftKey && document.activeElement === last) {
          event.preventDefault();
          first.focus();
        }
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    return () => {
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [allowClose, onClose, onSignIn, open]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[1000] flex items-center justify-center bg-on-background/35 p-4 backdrop-blur-sm">
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="session-expired-title"
        aria-describedby="session-expired-description"
        className="w-full max-w-[420px] rounded-xl border border-outline-variant bg-surface p-8 text-center shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1)]"
      >
        <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-full border border-outline-variant bg-surface-container">
          <span className="text-3xl text-primary" aria-hidden="true">
            ⏳
          </span>
        </div>

        <h2 id="session-expired-title" className="text-3xl font-semibold text-on-surface">
          {SESSION_EXPIRED_TITLE}
        </h2>
        <p id="session-expired-description" className="mt-2 text-base text-on-surface-variant">
          {message || SESSION_EXPIRED_DEFAULT_MESSAGE}
        </p>

        <div className="mt-6 flex flex-col gap-3">
          <button
            ref={signInButtonRef}
            type="button"
            onClick={onSignIn}
            className="w-full rounded bg-primary px-4 py-3 text-sm font-semibold text-white transition hover:opacity-90"
          >
            {SESSION_EXPIRED_SIGN_IN}
          </button>
          <button
            type="button"
            onClick={allowClose ? onClose : onSignIn}
            className="w-full rounded px-4 py-3 text-sm font-semibold text-on-surface-variant transition hover:bg-surface-container-low"
          >
            {SESSION_EXPIRED_CLOSE}
          </button>
        </div>
      </div>
    </div>
  );
}
