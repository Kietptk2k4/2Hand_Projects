export function AuditStatusBadge({ status }) {
  const normalized = String(status || "").toUpperCase();
  const className =
    normalized === "SUCCESS"
      ? "bg-emerald-100 text-emerald-800"
      : normalized === "FAILURE"
        ? "bg-amber-100 text-amber-900"
        : "bg-surface-container-high text-on-surface-variant";

  return (
    <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${className}`}>
      {normalized || "—"}
    </span>
  );
}