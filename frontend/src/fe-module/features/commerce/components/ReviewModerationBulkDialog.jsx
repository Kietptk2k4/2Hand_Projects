import { useEffect, useState } from "react";
import { AdminFilterButton } from "../../auth/admin/components/ui";
import { REASON_MAX_LENGTH } from "../constants/adminReviewModerationConstants.js";

export function ReviewModerationBulkDialog({
  open,
  mode,
  selectedCount,
  isSubmitting,
  submitError,
  onClose,
  onSubmit,
}) {
  const [reason, setReason] = useState("");
  const [note, setNote] = useState("");

  useEffect(() => {
    if (!open) return;
    setReason("");
    setNote("");
  }, [open]);

  if (!open) return null;

  const trimmedReason = reason.trim();
  const trimmedNote = note.trim();
  const title = mode === "restore" ? "Khôi phục hàng loạt" : "Ẩn đánh giá hàng loạt";

  return (
    <div
      className="fixed inset-0 z-[60] flex items-end justify-center bg-admin-text/40 p-0 sm:items-center sm:p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="review-bulk-title"
    >
      <div className="flex max-h-[90dvh] w-full max-w-lg flex-col overflow-hidden rounded-t-2xl border border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:rounded-xl">
        <div className="border-b border-admin-border-subtle px-4 py-4 sm:px-6">
          <h2 id="review-bulk-title" className="text-lg font-semibold text-admin-text">
            {title}
          </h2>
          <p className="mt-1 text-sm text-admin-text-secondary">
            Áp dụng cho {selectedCount} đánh giá đã chọn.
          </p>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto px-4 py-4 sm:px-6">
          <label className="block">
            <span className="text-sm font-medium text-admin-text">
              Lý do <span className="text-admin-danger">*</span>
            </span>
            <textarea
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              rows={4}
              maxLength={REASON_MAX_LENGTH}
              disabled={isSubmitting}
              placeholder="Mô tả lý do kiểm duyệt…"
              className="mt-1 w-full resize-y rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm text-admin-text focus:border-admin-accent focus:outline-none focus:ring-2 focus:ring-admin-accent-soft disabled:opacity-50"
            />
          </label>

          <label className="block">
            <span className="text-sm font-medium text-admin-text">Ghi chú nội bộ (tuỳ chọn)</span>
            <textarea
              value={note}
              onChange={(event) => setNote(event.target.value)}
              rows={2}
              maxLength={REASON_MAX_LENGTH}
              disabled={isSubmitting}
              placeholder="Ghi chú chỉ hiển thị trong lịch sử moderation…"
              className="mt-1 w-full resize-y rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm text-admin-text focus:border-admin-accent focus:outline-none focus:ring-2 focus:ring-admin-accent-soft disabled:opacity-50"
            />
          </label>

          {submitError ? <p className="text-sm text-admin-danger">{submitError}</p> : null}
        </div>

        <div className="flex flex-col-reverse gap-2 border-t border-admin-border-subtle px-4 py-4 sm:flex-row sm:justify-end sm:px-6">
          <AdminFilterButton type="button" variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={isSubmitting || !trimmedReason}
            onClick={() => onSubmit?.({ mode, reason: trimmedReason, note: trimmedNote })}
          >
            {isSubmitting ? "Đang xử lý…" : "Xác nhận"}
          </AdminFilterButton>
        </div>
      </div>
    </div>
  );
}
