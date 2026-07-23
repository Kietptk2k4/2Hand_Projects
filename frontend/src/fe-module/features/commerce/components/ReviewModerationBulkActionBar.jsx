import { AdminFilterButton } from "../../auth/admin/components/ui";

export function ReviewModerationBulkActionBar({
  selectedCount,
  canHideReview,
  canRestoreReview,
  disabled,
  onHide,
  onRestore,
  onClearSelection,
}) {
  if (!selectedCount) return null;

  return (
    <div className="mb-4 flex flex-col gap-3 rounded-xl border border-admin-accent-border bg-admin-accent-soft/20 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
      <p className="text-sm font-medium text-admin-text">
        Đã chọn <span className="tabular-nums">{selectedCount}</span> đánh giá
      </p>
      <div className="flex flex-wrap gap-2">
        {canHideReview ? (
          <AdminFilterButton
            type="button"
            variant="primary"
            className="!bg-admin-danger !text-white hover:!brightness-95"
            disabled={disabled}
            onClick={onHide}
          >
            Ẩn hàng loạt
          </AdminFilterButton>
        ) : null}
        {canRestoreReview ? (
          <AdminFilterButton type="button" variant="secondary" disabled={disabled} onClick={onRestore}>
            Khôi phục hàng loạt
          </AdminFilterButton>
        ) : null}
        <AdminFilterButton type="button" variant="secondary" disabled={disabled} onClick={onClearSelection}>
          Bỏ chọn
        </AdminFilterButton>
      </div>
    </div>
  );
}
