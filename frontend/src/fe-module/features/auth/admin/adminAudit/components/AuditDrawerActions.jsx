import { useSearchParams } from "react-router-dom";
import { AdminFilterButton } from "../../components/ui";
import { listAuditTargetNavigations } from "../utils/auditTargetNavigation.js";

export function AuditDrawerActions({
  entry,
  onFilterSameAdmin,
  onFilterSameTarget,
  onClose,
}) {
  const [, setSearchParams] = useSearchParams();
  const navigations = listAuditTargetNavigations(entry?.targetType, entry?.targetId);

  const handleNavigate = (navigation) => {
    if (!navigation?.buildParams) return;
    setSearchParams(navigation.buildParams(), { replace: false });
    onClose?.();
  };

  return (
    <div className="space-y-3 rounded-lg border border-admin-border bg-admin-surface-muted/50 p-3">
      <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
        Hành động nhanh
      </p>
      <div className="flex flex-col gap-2">
        {navigations.map((navigation) => (
          <AdminFilterButton
            key={navigation.id}
            type="button"
            variant="secondary"
            className="w-full justify-start text-admin-accent"
            disabled={!navigation.buildParams}
            onClick={() => handleNavigate(navigation)}
          >
            {navigation.label}
          </AdminFilterButton>
        ))}
        {entry?.adminId ? (
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="w-full justify-start"
            onClick={onFilterSameAdmin}
          >
            Lọc nhật ký cùng admin
          </AdminFilterButton>
        ) : null}
        {entry?.targetType && entry?.targetId ? (
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="w-full justify-start"
            onClick={onFilterSameTarget}
          >
            Lọc nhật ký cùng đối tượng
          </AdminFilterButton>
        ) : null}
      </div>
    </div>
  );
}
