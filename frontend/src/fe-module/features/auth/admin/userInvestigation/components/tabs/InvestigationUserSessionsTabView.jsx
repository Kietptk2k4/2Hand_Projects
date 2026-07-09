import { AdminPageHeader, AdminSurfaceCard } from "../../../components/ui";
import { InvestigationEmptyState } from "../InvestigationEmptyState.jsx";
import { InvestigationForbiddenState } from "../InvestigationForbiddenState.jsx";
import { InvestigationPermissionNotice } from "../InvestigationPermissionNotice.jsx";
import { InvestigationListSkeleton } from "../ui/InvestigationListSkeleton.jsx";
import { InvestigationRetryPanel } from "../ui/InvestigationRetryPanel.jsx";
import { InvestigationSessionsFilterBar } from "./InvestigationSessionsFilterBar.jsx";
import { InvestigationSessionsListView } from "./InvestigationSessionsListView.jsx";

export function InvestigationUserSessionsTabView({
  title,
  subtitle,
  status,
  errorMessage,
  canReadProfile,
  permissionNotice,
  adminRevokeNotice,
  statusFilter,
  statusOptions,
  sessions,
  getStatusLabel,
  canRevokeSession,
  hasNext,
  loadMoreStatus,
  inlineError,
  onStatusChange,
  onRevokeSession,
  onLoadMore,
  onRetry,
  revokeModal,
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
        <InvestigationListSkeleton rows={4} />
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
        <InvestigationRetryPanel
          message={errorMessage}
          onRetry={onRetry}
        />
        <p className="mt-2 text-sm text-admin-text-muted">
          Hệ thống tạm thời không phản hồi. Vui lòng thử lại sau vài phút.
        </p>
      </div>
    );
  }

  return (
    <div>
      <AdminPageHeader title={title} subtitle={subtitle} />

      {!canReadProfile && permissionNotice ? (
        <InvestigationPermissionNotice message={permissionNotice} />
      ) : null}

      {adminRevokeNotice ? <InvestigationPermissionNotice message={adminRevokeNotice} /> : null}

      <AdminSurfaceCard padding="md" className="mb-6">
        <InvestigationSessionsFilterBar
          statusFilter={statusFilter}
          statusOptions={statusOptions}
          onStatusChange={onStatusChange}
        />
      </AdminSurfaceCard>

      <InvestigationSessionsListView
        sessions={sessions}
        getStatusLabel={getStatusLabel}
        canRevokeSession={canRevokeSession}
        hasNext={hasNext}
        loadMoreStatus={loadMoreStatus}
        onRevokeSession={onRevokeSession}
        onLoadMore={onLoadMore}
      />

      {inlineError ? <p className="mt-4 text-sm text-admin-danger">{inlineError}</p> : null}
      {revokeModal}
    </div>
  );
}
