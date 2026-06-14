import { useEffect } from "react";

export function FeedToast({ message, onDismiss, autoDismissMs }) {
  useEffect(() => {
    if (!message || !autoDismissMs) {
      return undefined;
    }

    const timeoutId = window.setTimeout(onDismiss, autoDismissMs);
    return () => window.clearTimeout(timeoutId);
  }, [autoDismissMs, message, onDismiss]);

  if (!message) return null;

  return (
    <div
      className="fixed bottom-6 left-1/2 z-[60] flex max-w-sm -translate-x-1/2 items-center gap-3 rounded-xl border border-outline-variant bg-surface-container-lowest px-4 py-3 shadow-lg"
      role="status"
    >
      <span className="text-sm text-on-surface">{message}</span>
      <button
        type="button"
        onClick={onDismiss}
        className="text-sm font-medium text-primary hover:underline"
        aria-label="Đóng thông báo"
      >
        Đóng
      </button>
    </div>
  );
}
