import { AdminSurfaceCard } from "../../components/ui";

const DEFAULT_MESSAGE =
  "Tài khoản thiếu quyền CATALOG_READ. Đăng xuất và đăng nhập lại sau khi admin được gán quyền catalog.";

export function CatalogForbiddenState({ message = DEFAULT_MESSAGE }) {
  return (
    <AdminSurfaceCard padding="md" className="border-admin-warning/40 bg-admin-warning-soft">
      <p className="text-sm text-admin-warning">{message}</p>
    </AdminSurfaceCard>
  );
}
