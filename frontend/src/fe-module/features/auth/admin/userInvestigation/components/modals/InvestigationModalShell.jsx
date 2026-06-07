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
      className="fixed inset-0 z-[100] flex items-center justify-center bg-on-surface/40 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      onClick={(event) => {
        if (event.target === event.currentTarget) onClose?.();
      }}
    >
      <div
        className={[
          "flex w-full flex-col overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1)]",
          maxWidthClass,
        ].join(" ")}
      >
        <div className="flex items-start justify-between gap-3 border-b border-outline-variant/40 bg-surface-bright px-6 py-4">
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              {titleIcon ? <span className="shrink-0 text-error">{titleIcon}</span> : null}
              <h2 className="text-lg font-semibold text-on-surface">{title}</h2>
            </div>
            {subtitle ? <p className="mt-1 text-sm text-on-surface-variant">{subtitle}</p> : null}
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full p-1 text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-on-surface"
            aria-label="Đóng"
          >
            ×
          </button>
        </div>
        <div className="flex-1 overflow-y-auto bg-surface-container-lowest p-6">{children}</div>
        {footer ? (
          <div className="flex justify-end gap-2 border-t border-outline-variant/40 bg-surface-bright px-6 py-4">
            {footer}
          </div>
        ) : null}
      </div>
    </div>
  );
}