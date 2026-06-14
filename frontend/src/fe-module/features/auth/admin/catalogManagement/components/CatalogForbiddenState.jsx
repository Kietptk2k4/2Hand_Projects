import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";

export function CatalogForbiddenState() {
  return (
    <AccountCard className="border-amber-200 bg-amber-50/50 p-4">
      <p className="text-sm text-amber-900">
        Ban khong co quyen xem catalog. Can quyen CATALOG_READ tren tai khoan admin.
      </p>
    </AccountCard>
  );
}
