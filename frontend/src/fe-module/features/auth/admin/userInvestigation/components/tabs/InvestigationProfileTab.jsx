import { useCallback, useEffect, useState } from "react";
import { getInvestigationProfile } from "../../api/userInvestigationApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import {
  AccountCard,
  AccountSkeleton,
  TabPanelHeader,
} from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";
import {
  INVESTIGATION_PROFILE_SUBTITLE,
  INVESTIGATION_PROFILE_TITLE,
} from "../../constants/userInvestigationUiStrings.js";
import { useInvestigationPermissions } from "../../hooks/useInvestigationPermissions.js";
import { getInvestigationUserLabel } from "../../utils/investigationUserLabel.js";
import { EnforcementSummaryTable } from "../EnforcementSummaryTable.jsx";
import { EnforcementApplyModal } from "../modals/EnforcementApplyModal.jsx";
import { InvestigationActionToolbar } from "../InvestigationActionToolbar.jsx";
import { InvestigationEmptyState } from "../InvestigationEmptyState.jsx";
import { INVESTIGATION_PERMISSIONS } from "../../constants/investigationPermissions.js";
import { handleInvestigationLoadError, resolveInvestigationForbiddenMessage } from "../../utils/investigationTabErrors.js";
import { InvestigationForbiddenState } from "../InvestigationForbiddenState.jsx";
import { InvestigationPermissionNotice } from "../InvestigationPermissionNotice.jsx";
import { InvestigationProfileCard } from "../InvestigationProfileCard.jsx";

export function InvestigationProfileTab({ userId, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadProfile } = useInvestigationPermissions();
  const [profile, setProfile] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [applyModalAction, setApplyModalAction] = useState(null);

  const fetchProfile = useCallback(async () => {
    if (!userId) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getInvestigationProfile(userId);
      setProfile(data);
      setStatus("ready");
    } catch (error) {
      handleInvestigationLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionCode: INVESTIGATION_PERMISSIONS.READ_PROFILE,
        actionLabel: "xem hồ sơ điều tra",
        fallbackMessage: "Không tải được hồ sơ điều tra.",
        notFoundMessage: "Không tìm thấy người dùng trong hệ thống.",
      });
    }
  }, [userId, showSessionExpired, onNotify]);

  useEffect(() => {
    if (!userId) {
      setProfile(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchProfile();
  }, [userId, fetchProfile]);

  const handleApiError = (error) => {
    if (error?.code === 401) {
      showSessionExpired(error?.message);
      return;
    }
    if (error?.code === 403) {
      onNotify?.({
        variant: "error",
        message: resolveInvestigationForbiddenMessage(error, {
          actionLabel: "thực hiện thao tác này",
        }),
      });
      return;
    }
    onNotify?.({ variant: "error", message: error?.message || "Có lỗi xảy ra." });
  };

  const onEnforcementAction = (actionId) => {
    setApplyModalAction(actionId);
  };

  const userLabel = getInvestigationUserLabel(userId, profile);

  if (!userId) {
    return (
      <div>
        <TabPanelHeader title={INVESTIGATION_PROFILE_TITLE} subtitle={INVESTIGATION_PROFILE_SUBTITLE} />
        <InvestigationEmptyState />
      </div>
    );
  }

  if (status === "loading" || status === "idle") {
    return (
      <div>
        <TabPanelHeader title={INVESTIGATION_PROFILE_TITLE} subtitle={INVESTIGATION_PROFILE_SUBTITLE} />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "forbidden") {
    return (
      <div>
        <TabPanelHeader title={INVESTIGATION_PROFILE_TITLE} subtitle={INVESTIGATION_PROFILE_SUBTITLE} />
        <InvestigationForbiddenState message={errorMessage} />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader title={INVESTIGATION_PROFILE_TITLE} subtitle={INVESTIGATION_PROFILE_SUBTITLE} />
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button
            type="button"
            onClick={fetchProfile}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
          >
            Thử lại
          </button>
        </AccountCard>
      </div>
    );
  }

  if (!profile) {
    return (
      <div>
        <TabPanelHeader title={INVESTIGATION_PROFILE_TITLE} subtitle={INVESTIGATION_PROFILE_SUBTITLE} />
        <AccountCard className="border-error/30">
          <ErrorState message="Không tải được hồ sơ điều tra." />
          <button
            type="button"
            onClick={fetchProfile}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
          >
            Thử lại
          </button>
        </AccountCard>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {!canReadProfile ? (
        <InvestigationPermissionNotice message="Tài khoản của bạn thiếu quyền USER_INVESTIGATION_READ để xem hồ sơ điều tra." />
      ) : null}

      <div className="flex flex-col gap-4 border-b border-outline-variant pb-4 sm:flex-row sm:items-end sm:justify-between">
        <header className="mb-0">
          <h1 className="text-2xl font-semibold text-on-surface md:text-3xl">
            {INVESTIGATION_PROFILE_TITLE}
          </h1>
          <p className="mt-2 text-base text-on-surface-variant">{INVESTIGATION_PROFILE_SUBTITLE}</p>
        </header>
        <InvestigationActionToolbar onAction={onEnforcementAction} />
      </div>

      <InvestigationProfileCard profile={profile} />

      <EnforcementSummaryTable enforcements={profile?.current_enforcements || []} />

      <EnforcementApplyModal
        open={Boolean(applyModalAction)}
        actionType={applyModalAction}
        userId={userId}
        userLabel={userLabel}
        onClose={() => setApplyModalAction(null)}
        onSuccess={() => {
          onNotify?.({ variant: "success", message: "Áp dụng enforcement thành công." });
          fetchProfile();
        }}
        onError={handleApiError}
      />
    </div>
  );
}
