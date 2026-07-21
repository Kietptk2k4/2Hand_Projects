import { AdminDetailPageShell } from "../../../components/ui";
import { InvestigationListSkeleton } from "../ui/InvestigationListSkeleton.jsx";
import { InvestigationRetryPanel } from "../ui/InvestigationRetryPanel.jsx";
import { EnforcementSummaryTable } from "../EnforcementSummaryTable.jsx";
import { InvestigationEmptyState } from "../InvestigationEmptyState.jsx";
import { InvestigationForbiddenState } from "../InvestigationForbiddenState.jsx";
import { InvestigationPermissionNotice } from "../InvestigationPermissionNotice.jsx";
import { InvestigationProfileCard } from "../InvestigationProfileCard.jsx";

const SECTION_EYEBROW = "Điều tra người dùng";

export function InvestigationProfileTabView({
  title,
  subtitle,
  status,
  errorMessage,
  profile,
  canReadProfile,
  permissionNotice,
  onRetry,
  toolbar,
  applyModal,
}) {
  if (status === "no-user") {
    return (
      <AdminDetailPageShell eyebrow={SECTION_EYEBROW} title={title} subtitle={subtitle}>
        <InvestigationEmptyState />
      </AdminDetailPageShell>
    );
  }

  if (status === "loading") {
    return (
      <AdminDetailPageShell eyebrow={SECTION_EYEBROW} title={title} subtitle={subtitle}>
        <InvestigationListSkeleton rows={6} />
      </AdminDetailPageShell>
    );
  }

  if (status === "forbidden") {
    return (
      <AdminDetailPageShell eyebrow={SECTION_EYEBROW} title={title} subtitle={subtitle}>
        <InvestigationForbiddenState message={errorMessage} />
      </AdminDetailPageShell>
    );
  }

  if (status === "error" || (status === "ready" && !profile)) {
    return (
      <AdminDetailPageShell eyebrow={SECTION_EYEBROW} title={title} subtitle={subtitle}>
        <InvestigationRetryPanel
          message={errorMessage || "Không tải được hồ sơ điều tra."}
          onRetry={onRetry}
        />
      </AdminDetailPageShell>
    );
  }

  return (
    <AdminDetailPageShell
      eyebrow={SECTION_EYEBROW}
      title={title}
      subtitle={subtitle}
      headerActions={toolbar}
    >
      {!canReadProfile && permissionNotice ? (
        <InvestigationPermissionNotice message={permissionNotice} />
      ) : null}

      <InvestigationProfileCard profile={profile} />
      <EnforcementSummaryTable enforcements={profile?.current_enforcements || []} />
      {applyModal}
    </AdminDetailPageShell>
  );
}
