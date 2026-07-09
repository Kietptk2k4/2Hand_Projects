function ActionIcon({ name }) {
  if (name === "shield") {
    return (
      <svg className="h-[18px] w-[18px]" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
        <path d="M12 2 4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3Z" />
      </svg>
    );
  }
  if (name === "pause_circle") {
    return (
      <svg className="h-[18px] w-[18px]" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
        <path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20Zm-1 14H9V8h2v8Zm4 0h-2V8h2v8Z" />
      </svg>
    );
  }
  return (
    <svg className="h-[18px] w-[18px]" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2Zm-1 14H9V8h2v8Zm4 0h-2V8h2v8Z" />
    </svg>
  );
}

const ACTION_STYLES = {
  restrict:
    "border border-admin-accent-border bg-admin-surface text-admin-accent hover:bg-admin-accent-soft",
  suspend:
    "border border-admin-warning/40 bg-admin-warning-soft text-admin-warning hover:bg-admin-warning-soft/80",
  ban: "bg-admin-danger text-white hover:bg-admin-danger/90",
};

export function InvestigationActionToolbarView({ actions, onAction, disabled = false }) {
  if (!actions?.length) return null;

  return (
    <div className="flex w-full flex-wrap gap-2 sm:w-auto">
      {actions.map((action) => (
        <button
          key={action.id}
          type="button"
          disabled={disabled}
          onClick={() => onAction?.(action.id)}
          className={[
            "inline-flex min-h-11 w-full items-center justify-center gap-1.5 rounded-lg px-4 py-2 text-sm font-medium shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft disabled:cursor-not-allowed disabled:opacity-50 sm:w-auto",
            ACTION_STYLES[action.id] ?? ACTION_STYLES.restrict,
          ].join(" ")}
        >
          <ActionIcon name={action.icon} />
          {action.label}
        </button>
      ))}
    </div>
  );
}
