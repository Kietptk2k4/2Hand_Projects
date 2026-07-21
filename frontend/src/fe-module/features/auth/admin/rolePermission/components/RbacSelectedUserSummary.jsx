import { RoleCodeBadge } from "./RoleCodeBadge.jsx";
import { RbacUserStatusBadge } from "./RbacUserStatusBadge.jsx";

function getInitials(user) {
  const source = user?.display_name?.trim() || user?.email?.trim() || "";
  if (!source) return "?";
  const parts = source.split(/[\s@._-]+/).filter(Boolean);
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  }
  return source.slice(0, 2).toUpperCase();
}

export function RbacSelectedUserSummary({
  selectedUserId,
  selectedUser,
  fieldError,
  emptyHint = "Chọn một người dùng từ danh sách.",
  emptySecondaryHint = "Click một dòng để gán hoặc thu hồi vai trò.",
  onClear,
}) {
  return (
    <div>
      <div className="flex items-center justify-between gap-2">
        <p className="text-xs font-medium tracking-wide text-admin-text-muted">
          Người dùng đã chọn
        </p>
        {selectedUserId && onClear ? (
          <button
            type="button"
            onClick={onClear}
            className="text-xs font-medium text-admin-text-secondary transition-colors hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
          >
            Bỏ chọn
          </button>
        ) : null}
      </div>

      {selectedUserId ? (
        <div className="mt-3 flex items-start gap-3">
          <span
            className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-admin-accent-soft text-sm font-semibold text-admin-accent-strong"
            aria-hidden="true"
          >
            {getInitials(selectedUser)}
          </span>
          <div className="min-w-0 flex-1">
            <p className="break-all text-sm font-medium text-admin-text">
              {selectedUser?.email || selectedUserId}
            </p>
            {selectedUser?.display_name ? (
              <p className="mt-0.5 text-sm text-admin-text-secondary">{selectedUser.display_name}</p>
            ) : null}
            <div className="mt-2 flex flex-wrap items-center gap-1.5">
              {selectedUser?.status ? <RbacUserStatusBadge status={selectedUser.status} /> : null}
              {selectedUser?.role_codes?.length > 0
                ? selectedUser.role_codes.map((code) => (
                    <RoleCodeBadge key={code} code={code} />
                  ))
                : selectedUser ? (
                    <span className="text-xs text-admin-text-muted">Chưa có vai trò</span>
                  ) : null}
            </div>
          </div>
        </div>
      ) : (
        <div className="mt-3 rounded-lg border border-dashed border-admin-border bg-admin-surface-muted/40 px-3 py-3">
          <p className="text-sm text-admin-text-muted">{emptyHint}</p>
          {emptySecondaryHint ? (
            <p className="mt-1 text-xs text-admin-text-muted">{emptySecondaryHint}</p>
          ) : null}
        </div>
      )}
      {fieldError ? <p className="mt-1 text-sm text-admin-danger">{fieldError}</p> : null}
    </div>
  );
}
