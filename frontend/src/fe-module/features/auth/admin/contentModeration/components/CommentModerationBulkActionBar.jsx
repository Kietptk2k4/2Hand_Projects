import { AdminFilterButton } from "../../components/ui";

export function CommentModerationBulkActionBar({
  selectedCount,
  canModerateComment,
  canRestoreComment,
  disabled,
  onModerate,
  onRestore,
  onClearSelection,
}) {
  if (!selectedCount) return null;

  return (
    <div className="mb-4 flex flex-col gap-3 rounded-xl border border-admin-accent-border bg-admin-accent-soft/20 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
      <p className="text-sm font-medium text-admin-text">
        Đã chọn <span className="tabular-nums">{selectedCount}</span> bình luận
      </p>
      <div className="flex flex-wrap gap-2">
        {canModerateComment ? (
          <AdminFilterButton type="button" variant="primary" disabled={disabled} onClick={onModerate}>
            Kiểm duyệt hàng loạt
          </AdminFilterButton>
        ) : null}
        {canRestoreComment ? (
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
