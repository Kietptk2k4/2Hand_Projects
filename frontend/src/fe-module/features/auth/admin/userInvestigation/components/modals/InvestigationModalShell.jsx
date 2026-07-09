export function InvestigationModalShell({
  open,
  title,
  subtitle,
  titleIcon,
  onClose,
  children,
  footer,
  maxWidthClass = "max-w-lg",
}) {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-[100] flex min-h-dvh items-end justify-center bg-admin-text/40 p-0 backdrop-blur-sm sm:items-center sm:p-4"
      role="dialog"
      aria-modal="true"
      onClick={(event) => {
        if (event.target === event.currentTarget) onClose?.();
      }}
    >
      <div
        className={[
          "flex max-h-[90dvh] w-full flex-col overflow-hidden rounded-t-xl border border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:rounded-xl",
          maxWidthClass,
        ].join(" ")}
      >
        <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              {titleIcon ? <span className="shrink-0 text-admin-danger">{titleIcon}</span> : null}
              <h2 className="text-lg font-semibold text-admin-text">{title}</h2>
            </div>
            {subtitle ? <p className="mt-1 text-sm text-admin-text-secondary">{subtitle}</p> : null}
          </div>
          <button
            type="button"
            onClick={onClose}
            className="flex min-h-11 min-w-11 items-center justify-center rounded-lg text-admin-text-muted transition-colors hover:bg-admin-surface hover:text-admin-text"
            aria-label="Đóng"
          >
            ×
          </button>
        </div>
        <div className="flex-1 overflow-y-auto bg-admin-surface p-4 sm:p-6">{children}</div>
        {footer ? (
          <div className="flex flex-col-reverse gap-2 border-t border-admin-border bg-admin-surface-muted px-4 py-4 sm:flex-row sm:justify-end sm:px-6">
            {footer}
          </div>
        ) : null}
      </div>
    </div>
  );
}
