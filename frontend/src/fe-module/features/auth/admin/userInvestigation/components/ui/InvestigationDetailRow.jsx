export function InvestigationDetailRow({ label, children, className = "" }) {
  return (
    <div className={["min-w-0", className].filter(Boolean).join(" ")}>
      <p className="mb-1 text-xs font-medium text-admin-text-muted">{label}</p>
      <div className="text-sm text-admin-text">{children}</div>
    </div>
  );
}
