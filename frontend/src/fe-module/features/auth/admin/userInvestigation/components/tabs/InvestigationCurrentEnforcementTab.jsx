import { useCallback, useEffect, useState } from "react";
import { getCurrentEnforcements } from "../../api/userInvestigationApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import {
  INVESTIGATION_CURRENT_ENFORCEMENT_SUBTITLE,
  INVESTIGATION_CURRENT_ENFORCEMENT_TITLE,
} from "../../constants/userInvestigationUiStrings.js";
import { INVESTIGATION_PERMISSIONS } from "../../constants/investigationPermissions.js";
import { useInvestigationPermissions } from "../../hooks/useInvestigationPermissions.js";
import { getInvestigationUserLabel } from "../../utils/investigationUserLabel.js";
import {
  handleInvestigationLoadError,
  resolveInvestigationForbiddenMessage,
} from "../../utils/investigationTabErrors.js";
import { EnforcementApplyModal } from "../modals/EnforcementApplyModal.jsx";
import { RevokeUserEnforcementModal } from "../modals/RevokeUserEnforcementModal.jsx";
import { InvestigationActionToolbar, filterInvestigationToolbarActions } from "../InvestigationActionToolbar.jsx";
import { InvestigationCurrentEnforcementTabView } from "./InvestigationCurrentEnforcementTabView.jsx";

export function InvestigationCurrentEnforcementTab({ userId, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadEnforcement, canRevoke, canRestrict, canSuspend, canBan } =
    useInvestigationPermissions();
  const [enforcements, setEnforcements] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [applyModalAction, setApplyModalAction] = useState(null);
  const [revokeTarget, setRevokeTarget] = useState(null);

  const fetchEnforcements = useCallback(async () => {
    if (!userId) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getCurrentEnforcements(userId);
      setEnforcements(data?.enforcements || []);
      setStatus("ready");
    } catch (error) {
      handleInvestigationLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionCode: INVESTIGATION_PERMISSIONS.READ_ENFORCEMENT,
        actionLabel: "xem enforcement hiện tại",
        fallbackMessage: "Không tải được enforcement hiện tại.",
      });
    }
  }, [userId, showSessionExpired]);

  useEffect(() => {
    if (!userId) {
      setEnforcements([]);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchEnforcements();
  }, [userId, fetchEnforcements]);

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

  const userLabel = getInvestigationUserLabel(userId);
  const viewStatus = !userId ? "no-user" : status === "loading" ? "loading" : status;

  const toolbarActions = filterInvestigationToolbarActions({
    canRestrict,
    canSuspend,
    canBan,
  });

  return (
    <InvestigationCurrentEnforcementTabView
      title={INVESTIGATION_CURRENT_ENFORCEMENT_TITLE}
      subtitle={INVESTIGATION_CURRENT_ENFORCEMENT_SUBTITLE}
      status={viewStatus}
      errorMessage={errorMessage}
      canReadEnforcement={canReadEnforcement}
      permissionNotice="Tài khoản của bạn thiếu quyền USER_ENFORCEMENT_READ để xem enforcement."
      enforcements={enforcements}
      canRevoke={canRevoke}
      onRevoke={setRevokeTarget}
      onRetry={fetchEnforcements}
      toolbar={
        <InvestigationActionToolbar
          actions={toolbarActions}
          onAction={(actionId) => setApplyModalAction(actionId)}
        />
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
            fetchEnforcements();
          }}
          onError={handleApiError}
        />
      }
      revokeModal={
        <RevokeUserEnforcementModal
          open={Boolean(revokeTarget)}
          enforcement={revokeTarget}
          userLabel={userLabel}
          onClose={() => setRevokeTarget(null)}
          onSuccess={() => {
            onNotify?.({ variant: "success", message: "Đã thu hồi enforcement." });
            fetchEnforcements();
          }}
          onError={handleApiError}
        />
      }
    />
  );
}
