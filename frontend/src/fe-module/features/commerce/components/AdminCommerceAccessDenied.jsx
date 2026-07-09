import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { AdminSurfaceCard } from "../../auth/admin/components/ui";

export function AdminCommerceAccessDenied() {
  return (
    <AdminSurfaceCard padding="lg" className="border-admin-danger/30 text-center">
      <p className="text-sm text-admin-danger">
        Bạn không có quyền truy cập. Đăng nhập bằng tài khoản admin (
        <span className="font-mono">admin@2hands.vn</span>).
      </p>
      <Link
        to={APP_ROUTES.login}
        className="mt-4 inline-block text-sm font-medium text-admin-accent hover:underline"
      >
        Đăng nhập
      </Link>
    </AdminSurfaceCard>
  );
}
