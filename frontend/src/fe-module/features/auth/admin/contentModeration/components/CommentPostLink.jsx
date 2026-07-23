import { useSearchParams } from "react-router-dom";
import { resolveAuditTargetNavigation } from "../../adminAudit/utils/auditTargetNavigation.js";
import { truncateModerationId } from "../utils/moderationDisplayUtils.js";

export function CommentPostLink({ postId }) {
  const [, setSearchParams] = useSearchParams();

  if (!postId) return <span className="text-admin-text-muted">—</span>;

  const navigation = resolveAuditTargetNavigation("POST", postId);
  const label = truncateModerationId(postId);

  if (!navigation?.buildParams) {
    return (
      <span className="font-mono text-xs text-admin-text-secondary" title={postId}>
        {label}
      </span>
    );
  }

  return (
    <button
      type="button"
      onClick={(event) => {
        event.stopPropagation();
        setSearchParams(navigation.buildParams(), { replace: false });
      }}
      className="inline-flex min-h-8 items-center gap-1 font-mono text-xs text-admin-accent transition-colors hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
      title={navigation.label}
    >
      {label}
      <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
        open_in_new
      </span>
    </button>
  );
}
