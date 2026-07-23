import { AdminFilterButton } from "../../components/ui";
import { SystemOperationsModalShell } from "../../systemOperations/components/ui/SystemOperationsModalShell.jsx";
import { isProtectedBrand } from "../utils/brandHelpers.js";

export function BrandDeactivateDialog({ item, pending, onConfirm, onClose }) {
  if (!item) return null;

  const hasProducts = Number(item.productCount) > 0;
  const protectedBrand = isProtectedBrand(item);

  return (
    <SystemOperationsModalShell
      open={Boolean(item)}
      title="Vô hiệu hóa thương hiệu"
      subtitle="Thương hiệu sẽ ẩn khỏi storefront. Có thể kích hoạt lại sau."
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
            disabled={pending || protectedBrand}
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

      {protectedBrand ? (
        <p className="mt-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-900">
          Thương hiệu hệ thống không thể vô hiệu hóa.
        </p>
      ) : null}

      {hasProducts ? (
        <p className="mt-3 rounded-lg border border-admin-border bg-admin-surface-muted px-3 py-2 text-sm text-admin-text-secondary">
          Thương hiệu đang gán {item.productCount} sản phẩm. Kiểm tra lại trước khi vô hiệu hóa.
        </p>
      ) : null}
    </SystemOperationsModalShell>
  );
}
