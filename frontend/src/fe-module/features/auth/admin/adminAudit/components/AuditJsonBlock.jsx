import { useState } from "react";
import { extractAuditPayloadHighlights } from "../utils/auditPayloadUtils.js";

export function AuditJsonBlock({ label, value }) {
  const [expanded, setExpanded] = useState(true);
  const highlights = extractAuditPayloadHighlights(value);
  const hasValue = value != null;

  return (
    <div className="min-w-0 rounded-lg border border-admin-border bg-admin-surface-muted/40">
      <button
        type="button"
        onClick={() => setExpanded((prev) => !prev)}
        className="flex w-full items-center justify-between gap-3 px-3 py-2.5 text-left transition-colors hover:bg-admin-surface-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-admin-accent-soft"
        aria-expanded={expanded}
      >
        <span className="text-sm font-semibold text-admin-text">{label}</span>
        <span className="material-symbols-outlined text-[20px] text-admin-text-muted" aria-hidden="true">
          {expanded ? "expand_less" : "expand_more"}
        </span>
      </button>

      {expanded ? (
        <div className="space-y-3 border-t border-admin-border px-3 py-3">
          {highlights.length ? (
            <dl className="grid grid-cols-1 gap-2 sm:grid-cols-2">
              {highlights.map((item) => (
                <div key={item.key} className="rounded-lg bg-admin-surface px-2.5 py-2">
                  <dt className="text-[11px] font-medium tracking-wide text-admin-text-muted uppercase">
                    {item.key}
                  </dt>
                  <dd className="mt-1 break-words text-sm text-admin-text">{item.value}</dd>
                </div>
              ))}
            </dl>
          ) : null}

          <pre className="max-h-72 max-w-full overflow-x-auto overflow-y-auto rounded-lg border border-admin-border bg-admin-surface p-3 text-xs leading-relaxed text-admin-text [scrollbar-width:thin]">
            {hasValue ? JSON.stringify(value, null, 2) : "—"}
          </pre>
        </div>
      ) : null}
    </div>
  );
}
