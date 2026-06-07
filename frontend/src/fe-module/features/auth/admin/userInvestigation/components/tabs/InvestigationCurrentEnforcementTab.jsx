import { useCallback, useEffect, useState } from "react";
import { getCurrentEnforcements } from "../../api/userInvestigationApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import {
  AccountCard,
  AccountSkeleton,
  TabPanelHeader,
} from "../../../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../../../shared/ui/PageState.jsx";
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
import {
  EnforcementActionBadge,
  EnforcementStatusBadge,
} from "../EnforcementBadges.jsx";
import { EnforcementApplyModal } from "../modals/EnforcementApplyModal.jsx";
import { RevokeUserEnforcementModal } from "../modals/RevokeUserEnforcementModal.jsx";
import { InvestigationActionToolbar } from "../InvestigationActionToolbar.jsx";
import { InvestigationEmptyState } from "../InvestigationEmptyState.jsx";
import { InvestigationForbiddenState } from "../InvestigationForbiddenState.jsx";
import { InvestigationPermissionNotice } from "../InvestigationPermissionNotice.jsx";

function EnforcementRow({ item, canRevoke, onRevoke }) {
  return (
    <tr className="hover:bg-surface-container-low/50">
      <td className="px-4 py-4 font-mono text-xs text-on-surface-variant sm:px-6">
        {item.enforcement_id?.slice(0, 8)}…
      </td>
      <td className="px-4 py-4 sm:px-6">
        <div className="flex flex-col items-start gap-1">
          <EnforcementActionBadge actionType={item.action_type} />
          <span className="text-sm font-medium text-on-surface">{item.reason_code}</span>
        </div>
      </td>
      <td className="max-w-xs px-4 py-4 sm:px-6">
        <p className="truncate text-sm text-on-surface" title={item.description}>
          {item.description || "—"}
        </p>
        <p className="mt-1 text-xs text-on-surface-variant">
          Tạo: {formatDateTime(item.created_at)}
        </p>
      </td>
      <td className="px-4 py-4 text-sm text-on-surface sm:px-6">
        {item.expires_at ? formatDateTime(item.expires_at) : "Không giới hạn"}
      </td>
      <td className="px-4 py-4 font-mono text-xs text-on-surface-variant sm:px-6">
        {item.enforced_by?.slice(0, 8) || "—"}…
      </td>
      <td className="px-4 py-4 sm:px-6">
        <EnforcementStatusBadge
          status="ACTIVE"
          possiblyExpired={item.possibly_expired}
        />
      </td>
      <td className="px-4 py-4 text-right sm:px-6">
        {canRevoke ? (
          <button
            type="button"
            onClick={() => onRevoke?.(item)}
            className="rounded-md border border-transparent px-3 py-1.5 text-sm font-medium text-error transition-colors hover:border-error-container hover:bg-error-container hover:text-on-error-container"
          >
            Thu hồi
          </button>
        ) : null}
      </td>
    </tr>
  );
}

export function InvestigationCurrentEnforcementTab({ userId, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadEnforcement, canRevoke } = useInvestigationPermissions();
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

  const onEnforcementAction = (actionId) => {
    setApplyModalAction(actionId);
  };

  const onRevoke = (item) => {
    setRevokeTarget(item);
  };

  const userLabel = getInvestigationUserLabel(userId);

  if (!userId) {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_CURRENT_ENFORCEMENT_TITLE}
          subtitle={INVESTIGATION_CURRENT_ENFORCEMENT_SUBTITLE}
        />
        <InvestigationEmptyState />
      </div>
    );
  }

  if (status === "loading") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_CURRENT_ENFORCEMENT_TITLE}
          subtitle={INVESTIGATION_CURRENT_ENFORCEMENT_SUBTITLE}
        />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "forbidden") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_CURRENT_ENFORCEMENT_TITLE}
          subtitle={INVESTIGATION_CURRENT_ENFORCEMENT_SUBTITLE}
        />
        <InvestigationForbiddenState message={errorMessage} />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_CURRENT_ENFORCEMENT_TITLE}
          subtitle={INVESTIGATION_CURRENT_ENFORCEMENT_SUBTITLE}
        />
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button
            type="button"
            onClick={fetchEnforcements}
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
      {!canReadEnforcement ? (
        <InvestigationPermissionNotice message="Tài khoản của bạn thiếu quyền USER_ENFORCEMENT_READ để xem enforcement." />
      ) : null}

      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <TabPanelHeader
          title={INVESTIGATION_CURRENT_ENFORCEMENT_TITLE}
          subtitle={INVESTIGATION_CURRENT_ENFORCEMENT_SUBTITLE}
        />
        <InvestigationActionToolbar onAction={onEnforcementAction} />
      </div>

      <AccountCard className="!p-0 overflow-hidden">
        <div className="flex items-center justify-between border-b border-outline-variant bg-surface-container-low px-6 py-3">
          <h3 className="text-sm font-semibold text-on-surface">
            Danh sách thực thi ({enforcements.length})
          </h3>
        </div>

        {enforcements.length === 0 ? (
          <div className="p-6">
            <EmptyState message="Không có enforcement đang hiệu lực." />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[900px] border-collapse text-left text-sm">
              <thead>
                <tr className="border-b border-outline-variant bg-surface-container-low text-xs font-semibold uppercase tracking-wide text-on-surface-variant">
                  <th className="px-4 py-3 sm:px-6">ID thực thi</th>
                  <th className="px-4 py-3 sm:px-6">Loại / Mã lý do</th>
                  <th className="px-4 py-3 sm:px-6">Chi tiết</th>
                  <th className="px-4 py-3 sm:px-6">Thời hạn</th>
                  <th className="px-4 py-3 sm:px-6">Người xử lý</th>
                  <th className="px-4 py-3 sm:px-6">Trạng thái</th>
                  <th className="px-4 py-3 text-right sm:px-6">Hành động</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-outline-variant/50">
                {enforcements.map((item) => (
                  <EnforcementRow
                    key={item.enforcement_id}
                    item={item}
                    canRevoke={canRevoke}
                    onRevoke={onRevoke}
                  />
                ))}
              </tbody>
            </table>
          </div>
        )}
      </AccountCard>

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
    </div>
  );
}
