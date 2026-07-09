export function SupportTimeline({ title, entries, formatDateTime }) {
  if (!entries?.length) return null;

  return (
    <div>
      {title ? <h3 className="mb-3 text-base font-semibold text-admin-text">{title}</h3> : null}
      <ul className="space-y-3">
        {entries.map((entry, index) => (
          <li
            key={`${entry.occurred_at || entry.received_at}-${index}`}
            className="border-l-2 border-admin-accent/30 pl-4"
          >
            <p className="text-sm font-medium text-admin-text">
              {entry.old_status || "—"} → {entry.new_status || "—"}
              {entry.raw_status ? ` (${entry.raw_status})` : ""}
            </p>
            <p className="text-xs text-admin-text-muted">
              {formatDateTime(entry.occurred_at || entry.received_at)}
            </p>
            {entry.note ? <p className="mt-1 text-xs text-admin-text-secondary">{entry.note}</p> : null}
          </li>
        ))}
      </ul>
    </div>
  );
}
