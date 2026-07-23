import { useState } from "react";
import { copyToClipboard, truncateUuid } from "../utils/orderSupportDisplayUtils.js";

export function OrderSupportUuidCell({ value, onCopied }) {
  const [copied, setCopied] = useState(false);

  if (!value) return <span className="text-admin-text-muted">—</span>;

  const handleCopy = async (event) => {
    event.stopPropagation();
    const ok = await copyToClipboard(value);
    if (ok) {
      setCopied(true);
      onCopied?.();
      window.setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="flex items-center gap-1">
      <span className="font-mono text-xs text-admin-text" title={value}>
        {truncateUuid(value)}
      </span>
      <button
        type="button"
        onClick={handleCopy}
        className="inline-flex min-h-7 min-w-7 items-center justify-center rounded text-admin-text-muted hover:bg-admin-surface-muted hover:text-admin-text"
        aria-label="Sao chép UUID"
      >
        <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
          {copied ? "check" : "content_copy"}
        </span>
      </button>
    </div>
  );
}
