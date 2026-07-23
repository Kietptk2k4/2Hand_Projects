import { AdminFilterButton } from "../../auth/admin/components/ui";

export function ShopModerationBulkActionBar({
  selectedCount,
  canSuspendShop,
  canReopenShop,
  disabled,
  onSuspend,
  onRestore,
  onClearSelection,
}) {
  if (!selectedCount) return null;

  return (
    <div className="mb-4 flex flex-col gap-3 rounded-xl border border-admin-accent-border bg-admin-accent-soft/20 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
      <p className="text-sm font-medium text-admin-text">
        Đã chọn <span className="tabular-nums">{selectedCount}</span> cửa hàng
      </p>
      <div className="flex flex-wrap gap-2">
        {canSuspendShop ? (
          <AdminFilterButton type="button" variant="primary" disabled={disabled} onClick={onSuspend}>
            Tạm ngưng hàng loạt
          </AdminFilterButton>
        ) : null}
        {canReopenShop ? (
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
