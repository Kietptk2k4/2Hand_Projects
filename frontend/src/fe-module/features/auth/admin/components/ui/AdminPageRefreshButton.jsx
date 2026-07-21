export function AdminPageRefreshButton({ onClick, disabled = false, label = "Làm mới" }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className="inline-flex w-full items-center justify-center gap-2 rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto"
    >
      <span className="material-symbols-outlined text-base" aria-hidden="true">
        refresh
      </span>
      {label}
    </button>
  );
}
