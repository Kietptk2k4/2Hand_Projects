export function RevokeAdminSessionModalView({
  session,
  userLabel,
  revokeAllSessions,
  submitError,
  onRevokeAllChange,
}) {
  if (!session) return null;

  return (
    <div className="space-y-4">
      <div className="rounded-lg border border-admin-border bg-admin-surface-muted p-4 text-sm">
        <p className="font-medium text-admin-text">{userLabel || "Admin đang điều tra"}</p>
        <p className="mt-1 break-all text-admin-text-secondary">
          Session: {session.session_id || session.id}
        </p>
        <p className="mt-1 text-admin-text-secondary">IP: {session.ip_address || "—"}</p>
      </div>

      <label className="flex items-start gap-3 rounded-lg border border-admin-warning/40 bg-admin-warning-soft p-3 text-sm text-admin-warning">
        <input
          type="checkbox"
          checked={revokeAllSessions}
          onChange={(e) => onRevokeAllChange(e.target.checked)}
          className="mt-0.5 min-h-4 min-w-4"
        />
        <span>
          Thu hồi <strong>tất cả</strong> phiên admin đang hoạt động của người dùng này
        </span>
      </label>

      {submitError ? <p className="text-sm text-admin-danger">{submitError}</p> : null}
    </div>
  );
}
