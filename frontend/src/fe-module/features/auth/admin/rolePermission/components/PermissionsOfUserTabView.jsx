import {
  AdminEmptyPanel,
  AdminErrorPanel,
  AdminForbiddenPanel,
  AdminListSkeleton,
  AdminWorkspacePageShell,
} from "../../components/ui";
import { RBAC_SECTION_EYEBROW } from "../rbacPageContract.js";
import { RbacSelectedUserSummary } from "./RbacSelectedUserSummary.jsx";
import { UserPermissionChipList } from "./UserPermissionChipList.jsx";

export function PermissionsOfUserTabView({
  title,
  subtitle,
  rbacSelectedUserId,
  selectedUser,
  resolvedUserId,
  status,
  errorMessage,
  permissions,
  userListPanel,
  onClearUser,
  onRetry,
}) {
  const detailStatus =
    !rbacSelectedUserId ? "idle" : status === "loading" ? "loading" : status;

  return (
    <AdminWorkspacePageShell
      asideSize="comfortable"
      eyebrow={RBAC_SECTION_EYEBROW}
      title={title}
      subtitle={subtitle}
      sidebar={userListPanel}
    >
      {!rbacSelectedUserId ? (
        <AdminEmptyPanel
          message="Chưa chọn người dùng"
          hint="Chọn một dòng trong danh sách để xem quyền hiệu lực."
          icon="person_search"
        />
      ) : null}

      {detailStatus === "loading" ? <AdminListSkeleton rows={4} /> : null}
      {detailStatus === "forbidden" ? (
        <AdminForbiddenPanel message={errorMessage || "Bạn không có quyền truy cập."} />
      ) : null}
      {detailStatus === "not_found" || detailStatus === "error" ? (
        <AdminErrorPanel
          message={errorMessage || "Không tải được quyền người dùng."}
          onRetry={detailStatus === "error" ? onRetry : undefined}
        />
      ) : null}

      {detailStatus === "ready" ? (
        <div className="space-y-4">
          <div className="rounded-xl border border-admin-border bg-admin-surface p-4 shadow-[var(--shadow-admin-surface)] lg:p-5">
            <RbacSelectedUserSummary
              selectedUserId={rbacSelectedUserId}
              selectedUser={selectedUser}
              onClear={onClearUser}
            />
            {resolvedUserId ? (
              <p className="mt-3 break-all border-t border-admin-border-subtle pt-3 font-mono text-[11px] text-admin-text-muted">
                ID · {resolvedUserId}
              </p>
            ) : null}
          </div>

          <div className="flex items-center justify-between gap-2 rounded-lg border border-admin-border bg-admin-surface-raised px-3 py-2">
            <p className="text-xs text-admin-text-secondary">Tổng quyền hiệu lực</p>
            <p className="tabular-nums text-sm font-semibold text-admin-text">
              {permissions.length}
            </p>
          </div>

          <UserPermissionChipList permissions={permissions} />
        </div>
      ) : null}
    </AdminWorkspacePageShell>
  );
}
