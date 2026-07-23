import { useState } from "react";
import { AdminSurfaceCard } from "../../components/ui";
import { isValidSellerUuid } from "../utils/sellerDetailHelpers.js";

export function SellerDetailPicker({ onSubmitSellerId, onOpenTopSellers }) {
  const [draftId, setDraftId] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = (event) => {
    event.preventDefault();
    const trimmed = draftId.trim();
    if (!trimmed) {
      setError("Nhập seller ID (UUID).");
      return;
    }
    if (!isValidSellerUuid(trimmed)) {
      setError("Seller ID không đúng định dạng UUID.");
      return;
    }
    setError("");
    onSubmitSellerId?.(trimmed);
  };

  return (
    <AdminSurfaceCard padding="lg" className="max-w-2xl">
      <div className="flex items-start gap-4">
        <span
          className="material-symbols-outlined flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-admin-accent-soft text-2xl text-admin-accent"
          aria-hidden="true"
        >
          storefront
        </span>
        <div className="min-w-0 flex-1">
          <h2 className="text-lg font-semibold text-admin-text">Chọn seller để xem tài chính</h2>
          <p className="mt-1 text-sm text-admin-text-secondary">
            Dán seller ID (UUID) hoặc mở Top sellers để chọn từ bảng xếp hạng.
          </p>

          <form onSubmit={handleSubmit} className="mt-5 space-y-3">
            <label className="block">
              <span className="text-xs font-medium uppercase tracking-[0.06em] text-admin-text-muted">
                Seller ID
              </span>
              <input
                type="text"
                value={draftId}
                onChange={(event) => {
                  setDraftId(event.target.value);
                  if (error) setError("");
                }}
                placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                className="mt-1.5 w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2.5 font-mono text-sm text-admin-text placeholder:text-admin-text-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
                autoComplete="off"
                spellCheck={false}
              />
            </label>
            {error ? <p className="text-sm text-rose-600">{error}</p> : null}
            <div className="flex flex-wrap gap-2">
              <button
                type="submit"
                className="inline-flex min-h-10 items-center rounded-lg bg-admin-accent px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-admin-accent-strong focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
              >
                Xem chi tiết
              </button>
              <button
                type="button"
                onClick={onOpenTopSellers}
                className="inline-flex min-h-10 items-center gap-2 rounded-lg border border-admin-border bg-admin-surface px-4 py-2 text-sm font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
              >
                <span className="material-symbols-outlined text-base" aria-hidden="true">
                  leaderboard
                </span>
                Mở Top sellers
              </button>
            </div>
          </form>

          <p className="mt-4 text-xs text-admin-text-muted">
            Hoặc thêm vào URL:{" "}
            <code className="rounded bg-admin-surface-muted px-1.5 py-0.5 font-mono">
              ?sellerId=...
            </code>
          </p>
        </div>
      </div>
    </AdminSurfaceCard>
  );
}
