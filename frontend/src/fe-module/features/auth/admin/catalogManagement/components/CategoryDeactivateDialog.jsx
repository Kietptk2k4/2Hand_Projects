import { AdminFilterButton } from "../../components/ui";
import { SystemOperationsModalShell } from "../../systemOperations/components/ui/SystemOperationsModalShell.jsx";
import { countActiveChildren } from "../utils/categoryHelpers.js";

export function CategoryDeactivateDialog({
  item,
  allItems,
  pending,
  onConfirm,
  onClose,
}) {
  if (!item) return null;

  const activeChildren = countActiveChildren(item.id, allItems);
  const hasProducts = Number(item.productCount) > 0;

  return (
    <SystemOperationsModalShell
      open={Boolean(item)}
      title="Vô hiệu hóa danh mục"
      subtitle="Danh mục sẽ ẩn khỏi storefront. Thao tác có thể hoàn tác bằng kích hoạt lại."
      onClose={onClose}
      maxWidthClass="max-w-md"
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={pending} onClick={onClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={pending}
            className="border-admin-danger/30 bg-admin-danger text-white hover:bg-admin-danger/90"
            onClick={onConfirm}
          >
            Vô hiệu hóa
          </AdminFilterButton>
        </>
      }
    >
      <p className="text-sm text-admin-text-secondary">
        Bạn sắp vô hiệu hóa <span className="font-medium text-admin-text">{item.name}</span>.
      </p>

      {activeChildren > 0 ? (
        <p className="mt-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-900">
          Danh mục có {activeChildren} con đang hoạt động. API có thể từ chối thao tác này.
        </p>
      ) : null}

      {hasProducts ? (
        <p className="mt-3 rounded-lg border border-admin-border bg-admin-surface-muted px-3 py-2 text-sm text-admin-text-secondary">
          Danh mục đang gán {item.productCount} sản phẩm. Kiểm tra lại trước khi vô hiệu hóa.
        </p>
      ) : null}
    </SystemOperationsModalShell>
  );
}
