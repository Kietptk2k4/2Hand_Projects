import { AdminPageHeader } from "../../../components/ui";
import { InvestigationEmptyState } from "../InvestigationEmptyState.jsx";
import { InvestigationForbiddenState } from "../InvestigationForbiddenState.jsx";
import { InvestigationPermissionNotice } from "../InvestigationPermissionNotice.jsx";
import { InvestigationListSkeleton } from "../ui/InvestigationListSkeleton.jsx";
import { InvestigationRetryPanel } from "../ui/InvestigationRetryPanel.jsx";
import { InvestigationCurrentEnforcementPanel } from "./InvestigationCurrentEnforcementTable.jsx";

export function InvestigationCurrentEnforcementTabView({
  title,
  subtitle,
  status,
  errorMessage,
  canReadEnforcement,
  permissionNotice,
  enforcements,
  canRevoke,
  onRevoke,
  onRetry,
  toolbar,
  applyModal,
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
        <InvestigationRetryPanel message={errorMessage} onRetry={onRetry} />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {!canReadEnforcement && permissionNotice ? (
        <InvestigationPermissionNotice message={permissionNotice} />
      ) : null}

      <AdminPageHeader title={title} subtitle={subtitle} actions={toolbar} />

      <InvestigationCurrentEnforcementPanel
        enforcements={enforcements}
        canRevoke={canRevoke}
        onRevoke={onRevoke}
      />

      {applyModal}
      {revokeModal}
    </div>
  );
}
