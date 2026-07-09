import { AdminPageHeader } from "../../../components/ui";
import { InvestigationEmptyState } from "../InvestigationEmptyState.jsx";
import { InvestigationForbiddenState } from "../InvestigationForbiddenState.jsx";
import { InvestigationPermissionNotice } from "../InvestigationPermissionNotice.jsx";
import { InvestigationListSkeleton } from "../ui/InvestigationListSkeleton.jsx";
import { InvestigationRetryPanel } from "../ui/InvestigationRetryPanel.jsx";
import { InvestigationEnforcementHistoryTable } from "./InvestigationEnforcementHistoryTable.jsx";

export function InvestigationEnforcementHistoryTabView({
  title,
  subtitle,
  userId,
  status,
  errorMessage,
  canReadEnforcement,
  permissionNotice,
  items,
  expandedId,
  hasMore,
  loadMoreStatus,
  inlineError,
  onToggle,
  onLoadMore,
  onRetry,
}) {
  if (status === "no-user") {
    return (
      <div>
        <AdminPageHeader title={title} subtitle={subtitle} />
        <InvestigationEmptyState />
      </div>
    );
  }

  if (status === "loading") {
    return (
      <div>
        <AdminPageHeader title={title} subtitle={subtitle} />
        <InvestigationListSkeleton rows={5} />
      </div>
    );
  }

  if (status === "forbidden") {
    return (
      <div>
        <AdminPageHeader title={title} subtitle={subtitle} />
        <InvestigationForbiddenState message={errorMessage} />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <AdminPageHeader title={title} subtitle={subtitle} />
        <InvestigationRetryPanel message={errorMessage} onRetry={onRetry} />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <AdminPageHeader
        title={title}
        subtitle={
          <>
            {subtitle}{" "}
            <span className="font-mono text-sm text-admin-text-muted">userId: {userId}</span>
          </>
        }
      />

      {!canReadEnforcement && permissionNotice ? (
        <InvestigationPermissionNotice message={permissionNotice} />
      ) : null}

      <InvestigationEnforcementHistoryTable
        items={items}
        expandedId={expandedId}
        onToggle={onToggle}
        hasMore={hasMore}
        loadMoreStatus={loadMoreStatus}
        onLoadMore={onLoadMore}
      />

      {inlineError ? <p className="text-sm text-admin-danger">{inlineError}</p> : null}
    </div>
  );
}
