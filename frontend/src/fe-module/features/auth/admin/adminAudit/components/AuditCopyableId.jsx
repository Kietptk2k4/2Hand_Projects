import { useState } from "react";

export function AuditCopyableId({ label, value, mono = true, className = "" }) {
  const [copied, setCopied] = useState(false);
  const text = value || "—";

  const handleCopy = async () => {
    if (!value) return;
    try {
      await navigator.clipboard.writeText(String(value));
      setCopied(true);
      window.setTimeout(() => setCopied(false), 1600);
    } catch {
      setCopied(false);
    }
  };

  return (
    <div className={["min-w-0", className].filter(Boolean).join(" ")}>
      <dt className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">{label}</dt>
      <dd className="mt-1 flex items-start gap-2">
        <span
          className={[
            "min-w-0 flex-1 text-sm text-admin-text",
            mono ? "break-all font-mono" : "",
          ]
            .filter(Boolean)
            .join(" ")}
        >
          {text}
        </span>
        {value ? (
          <button
            type="button"
            onClick={handleCopy}
            className="inline-flex min-h-8 shrink-0 items-center rounded-lg border border-admin-border px-2 text-xs font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
          >
            {copied ? "Đã copy" : "Copy"}
          </button>
        ) : null}
      </dd>
    </div>
  );
}
