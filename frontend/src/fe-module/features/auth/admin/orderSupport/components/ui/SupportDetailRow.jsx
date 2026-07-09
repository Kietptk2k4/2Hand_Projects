export function SupportDetailRow({ label, value, mono = false }) {
  return (
    <div className="flex flex-col gap-0.5 sm:flex-row sm:justify-between sm:gap-4">
      <dt className="text-sm text-admin-text-secondary">{label}</dt>
      <dd
        className={[
          "text-sm font-medium text-admin-text",
          mono ? "break-all font-mono text-xs" : "",
        ]
          .filter(Boolean)
          .join(" ")}
      >
        {value ?? "—"}
      </dd>
    </div>
  );
}
