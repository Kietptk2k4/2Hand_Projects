import { useCallback, useState } from "react";
import { truncateSellerId } from "../utils/topSellersHelpers.js";

export function SellerDetailHeader({
  sellerId,
  shopName,
  onBackToTopSellers,
}) {
  const [copied, setCopied] = useState(false);

  const handleCopy = useCallback(async () => {
    if (!sellerId) return;
    try {
      await navigator.clipboard.writeText(sellerId);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  }, [sellerId]);

  return (
    <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
      <div className="min-w-0">
        <button
          type="button"
          onClick={onBackToTopSellers}
          className="mb-2 inline-flex items-center gap-1 text-sm font-medium text-admin-accent transition-colors hover:text-admin-accent-strong focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
        >
          <span className="material-symbols-outlined text-base" aria-hidden="true">
            arrow_back
          </span>
          Top sellers
        </button>
        <h1 className="text-balance text-xl font-semibold tracking-tight text-admin-text sm:text-2xl">
          {shopName || "Chi tiết tài chính seller"}
        </h1>
        <div className="mt-2 flex flex-wrap items-center gap-2">
          <code
            className="rounded-md bg-admin-surface-muted px-2 py-1 font-mono text-xs text-admin-text-secondary"
            title={sellerId}
          >
            {truncateSellerId(sellerId, 10, 6)}
          </code>
          <button
            type="button"
            onClick={handleCopy}
            className="inline-flex min-h-8 items-center gap-1 rounded-md border border-admin-border px-2 py-1 text-xs font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
          >
            <span className="material-symbols-outlined text-sm" aria-hidden="true">
              {copied ? "check" : "content_copy"}
            </span>
            {copied ? "Đã copy" : "Copy ID"}
          </button>
        </div>
      </div>
    </div>
  );
}
