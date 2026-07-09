import { AdminFilterButton } from "../../components/ui";

export function CatalogItemActions({
  canWrite,
  isProtected = false,
  active,
  actionId,
  itemId,
  onEdit,
  onDeactivate,
  onActivate,
}) {
  if (!canWrite) return null;

  const isBusy = actionId === itemId;

  return (
    <div className="flex flex-wrap gap-2">
      <AdminFilterButton
        type="button"
        variant="secondary"
        className="min-h-11 border-transparent px-2 py-1 text-admin-accent hover:bg-admin-accent-soft"
        onClick={onEdit}
        disabled={isProtected}
      >
        Sửa
      </AdminFilterButton>
      {!isProtected ? (
        active ? (
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-11 border-transparent px-2 py-1 text-admin-danger hover:bg-admin-danger-soft"
            disabled={isBusy}
            onClick={onDeactivate}
          >
            Vô hiệu
          </AdminFilterButton>
        ) : (
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-11 border-transparent px-2 py-1 text-admin-accent hover:bg-admin-accent-soft"
            disabled={isBusy}
            onClick={onActivate}
          >
            Kích hoạt
          </AdminFilterButton>
        )
      ) : null}
    </div>
  );
}
