export function CartWarningsBanner({ warnings = [] }) {
  if (!warnings.length) return null;

  return (
    <div
      className="mb-6 rounded-lg border border-surface-tint/30 bg-surface-container-low p-4"
      role="alert"
    >
      <ul className="space-y-1">
        {warnings.map((message) => (
          <li key={message} className="flex items-start gap-2 text-sm text-on-surface">
            <span
              className="material-symbols-outlined mt-0.5 shrink-0 text-surface-tint"
              aria-hidden="true"
            >
              info
            </span>
            <span>{message}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
