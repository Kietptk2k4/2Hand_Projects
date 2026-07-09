import { useCallback, useEffect, useState } from "react";
import { getInvestigationProfile } from "../../api/userInvestigationApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import {
  INVESTIGATION_PROFILE_SUBTITLE,
  INVESTIGATION_PROFILE_TITLE,
} from "../../constants/userInvestigationUiStrings.js";
import { useInvestigationPermissions } from "../../hooks/useInvestigationPermissions.js";
import { getInvestigationUserLabel } from "../../utils/investigationUserLabel.js";
import { EnforcementApplyModal } from "../modals/EnforcementApplyModal.jsx";
import { InvestigationActionToolbar, filterInvestigationToolbarActions } from "../InvestigationActionToolbar.jsx";
import { INVESTIGATION_PERMISSIONS } from "../../constants/investigationPermissions.js";
import {
  handleInvestigationLoadError,
  resolveInvestigationForbiddenMessage,
} from "../../utils/investigationTabErrors.js";
import { InvestigationProfileTabView } from "./InvestigationProfileTabView.jsx";

export function InvestigationProfileTab({ userId, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadProfile, canRestrict, canSuspend, canBan } = useInvestigationPermissions();
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
  }, [userId, showSessionExpired]);

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

  const viewStatus = !userId
    ? "no-user"
    : status === "loading" || status === "idle"
      ? "loading"
      : status;

  const toolbarActions = filterInvestigationToolbarActions({
    canRestrict,
    canSuspend,
    canBan,
  });

  return (
    <InvestigationProfileTabView
      title={INVESTIGATION_PROFILE_TITLE}
      subtitle={INVESTIGATION_PROFILE_SUBTITLE}
      status={viewStatus}
      errorMessage={errorMessage}
      profile={profile}
      canReadProfile={canReadProfile}
      permissionNotice="Tài khoản của bạn thiếu quyền USER_INVESTIGATION_READ để xem hồ sơ điều tra."
      onRetry={fetchProfile}
      toolbar={
        <InvestigationActionToolbar actions={toolbarActions} onAction={onEnforcementAction} />
      }
      applyModal={
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
      }
    />
  );
}
