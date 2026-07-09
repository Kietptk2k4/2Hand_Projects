export function SystemOperationsModalShell({
  open,
  title,
  subtitle,
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
          "flex max-h-[90dvh] w-full flex-col overflow-hidden rounded-t-xl border border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:mx-4 sm:rounded-xl",
          maxWidthClass,
        ].join(" ")}
      >
        <div className="border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <h2 className="text-lg font-semibold text-admin-text">{title}</h2>
          {subtitle ? <p className="mt-1 text-sm text-admin-text-secondary">{subtitle}</p> : null}
        </div>
        <div className="flex-1 overflow-y-auto px-4 py-4 sm:px-6">{children}</div>
        {footer ? (
          <div className="flex flex-col-reverse gap-2 border-t border-admin-border bg-admin-surface-muted px-4 py-4 sm:flex-row sm:justify-end sm:px-6">
            {footer}
          </div>
        ) : null}
      </div>
    </div>
  );
}
