import { AdminPageHeader, AdminSurfaceCard } from "../../../components/ui";
import { InvestigationEmptyState } from "../InvestigationEmptyState.jsx";
import { InvestigationForbiddenState } from "../InvestigationForbiddenState.jsx";
import { InvestigationPermissionNotice } from "../InvestigationPermissionNotice.jsx";
import { InvestigationListSkeleton } from "../ui/InvestigationListSkeleton.jsx";
import { InvestigationRetryPanel } from "../ui/InvestigationRetryPanel.jsx";
import { InvestigationLoginHistoryFilterBar } from "./InvestigationLoginHistoryFilterBar.jsx";
import { InvestigationLoginHistoryListView } from "./InvestigationLoginHistoryListView.jsx";

export function InvestigationLoginHistoryTabView({
  title,
  subtitle,
  status,
  errorMessage,
  canReadProfile,
  permissionNotice,
  successDraft,
  fromDraft,
  toDraft,
  items,
  hasNext,
  loadMoreStatus,
  inlineError,
  onSuccessChange,
  onFromChange,
  onToChange,
  onApplyFilters,
  onClearFilters,
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
    <div>
      <AdminPageHeader title={title} subtitle={subtitle} />

      {!canReadProfile && permissionNotice ? (
        <InvestigationPermissionNotice message={permissionNotice} />
      ) : null}

      <AdminSurfaceCard padding="md" className="mb-6">
        <p className="mb-3 text-sm font-semibold text-admin-text">Bộ lọc</p>
        <InvestigationLoginHistoryFilterBar
          successDraft={successDraft}
          fromDraft={fromDraft}
          toDraft={toDraft}
          onSuccessChange={onSuccessChange}
          onFromChange={onFromChange}
          onToChange={onToChange}
          onApply={onApplyFilters}
          onClear={onClearFilters}
        />
      </AdminSurfaceCard>

      <InvestigationLoginHistoryListView
        items={items}
        hasNext={hasNext}
        loadMoreStatus={loadMoreStatus}
        onLoadMore={onLoadMore}
      />

      {inlineError ? <p className="mt-4 text-sm text-admin-danger">{inlineError}</p> : null}
    </div>
  );
}
