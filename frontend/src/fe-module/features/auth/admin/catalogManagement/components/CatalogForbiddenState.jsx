import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";

const DEFAULT_MESSAGE =
  "Tài khoản thiếu quyền CATALOG_READ. Đăng xuất và đăng nhập lại sau khi admin được gán quyền catalog.";

export function CatalogForbiddenState({ message = DEFAULT_MESSAGE }) {
  return (
    <AccountCard className="border-amber-200 bg-amber-50/50 p-4">
      <p className="text-sm text-amber-900">{message}</p>
    </AccountCard>
  );
}
