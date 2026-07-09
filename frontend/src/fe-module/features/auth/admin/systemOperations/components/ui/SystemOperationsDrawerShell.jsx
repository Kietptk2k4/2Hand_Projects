export function SystemOperationsDrawerShell({
  open,
  title,
  subtitle,
  onClose,
  children,
  footer,
  headerExtra,
  maxWidthClass = "max-w-xl",
}) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[100] flex min-h-dvh justify-end bg-admin-text/40 backdrop-blur-sm">
      <button type="button" aria-label="Đóng" className="absolute inset-0" onClick={onClose} />
      <aside
        className={[
          "relative flex h-full min-h-dvh w-full flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]",
          maxWidthClass,
        ].join(" ")}
      >
        <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="min-w-0">
            <h2 className="text-lg font-semibold text-admin-text">{title}</h2>
            {subtitle ? <p className="mt-1 break-all font-mono text-xs text-admin-text-muted">{subtitle}</p> : null}
          </div>
          <button
            type="button"
            onClick={onClose}
            className="flex min-h-11 min-w-11 shrink-0 items-center justify-center rounded-lg text-admin-text-muted hover:bg-admin-surface hover:text-admin-text"
            aria-label="Đóng"
          >
            ×
          </button>
        </div>
        {headerExtra}
        <div className="flex-1 overflow-y-auto px-4 py-4 sm:px-6">{children}</div>
        {footer ? (
          <div className="flex flex-col-reverse gap-2 border-t border-admin-border bg-admin-surface-muted px-4 py-4 sm:flex-row sm:justify-end sm:px-6">
            {footer}
          </div>
        ) : null}
      </aside>
    </div>
  );
}
