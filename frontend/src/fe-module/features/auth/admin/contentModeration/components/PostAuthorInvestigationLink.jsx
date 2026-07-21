import { useSearchParams } from "react-router-dom";
import { resolveAuditTargetNavigation } from "../../adminAudit/utils/auditTargetNavigation.js";
import { truncateModerationId } from "../utils/moderationDisplayUtils.js";

export function PostAuthorInvestigationLink({ authorId, authorSummary, className = "" }) {
  const [, setSearchParams] = useSearchParams();
  const navigation = resolveAuditTargetNavigation("USER", authorId);

  if (!authorId || !navigation?.buildParams) {
    return <span className="font-mono text-xs text-admin-text-muted">—</span>;
  }

  const displayName = authorSummary?.displayName || authorSummary?.display_name || "";
  const label = displayName || truncateModerationId(authorId);

  return (
    <button
      type="button"
      onClick={(event) => {
        event.stopPropagation();
        setSearchParams(navigation.buildParams(), { replace: false });
      }}
      className={[
        "inline-flex min-h-8 max-w-full items-center gap-1 rounded-md text-xs text-admin-accent transition-colors hover:bg-admin-accent-soft focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft",
        displayName ? "font-medium" : "font-mono",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      title={navigation.label}
    >
      <span className="truncate" title={displayName || authorId}>
        {label}
      </span>
      {displayName ? (
        <span className="truncate font-mono text-[10px] text-admin-text-muted" title={authorId}>
          {truncateModerationId(authorId)}
        </span>
      ) : null}
      <span className="material-symbols-outlined shrink-0 text-[16px]" aria-hidden="true">
        open_in_new
      </span>
    </button>
  );
}
