import { useSearchParams } from "react-router-dom";
import { resolveAuditTargetNavigation } from "../utils/auditTargetNavigation.js";

export function AuditTargetLink({ targetType, targetId, className = "" }) {
  const [, setSearchParams] = useSearchParams();
  const navigation = resolveAuditTargetNavigation(targetType, targetId);

  if (!navigation?.buildParams) return null;

  return (
    <button
      type="button"
      onClick={(event) => {
        event.stopPropagation();
        setSearchParams(navigation.buildParams(), { replace: false });
      }}
      className={[
        "inline-flex min-h-8 items-center gap-1 rounded-md px-1.5 text-xs font-medium text-admin-accent transition-colors hover:bg-admin-accent-soft focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      title={navigation.label}
    >
      <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
        open_in_new
      </span>
      <span className="sr-only">{navigation.label}</span>
    </button>
  );
}
