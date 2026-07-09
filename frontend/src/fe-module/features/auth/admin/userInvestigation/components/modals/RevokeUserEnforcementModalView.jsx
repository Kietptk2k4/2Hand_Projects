import { AdminFilterSelect } from "../../../components/ui";
import { EnforcementActionBadge } from "../EnforcementBadges.jsx";

export function RevokeUserEnforcementModalView({
  enforcement,
  userLabel,
  reason,
  note,
  fieldErrors,
  submitError,
  reasonOptions,
  actionLabel,
  onReasonChange,
  onNoteChange,
}) {
  if (!enforcement) return null;

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-4 rounded-lg border border-admin-border bg-admin-surface-muted p-4">
        <div className="min-w-0 flex-1">
          <p className="font-mono text-xs text-admin-text-muted">
            ID: {enforcement.enforcement_id?.slice(0, 12)}…
          </p>
          <p className="mt-1 text-sm font-semibold text-admin-text">
            {userLabel || "Người dùng đang điều tra"}
          </p>
          <p className="mt-1 text-sm text-admin-text-secondary">{enforcement.reason_code}</p>
        </div>
        <EnforcementActionBadge actionType={enforcement.action_type} />
      </div>

      <div>
        <label htmlFor="revoke-reason" className="mb-1 block text-sm font-medium text-admin-text">
          Lý do thu hồi <span className="text-admin-danger">*</span>
        </label>
        <AdminFilterSelect
          id="revoke-reason"
          value={reason}
          onChange={(e) => onReasonChange(e.target.value)}
        >
          <option value="">Chọn lý do phù hợp</option>
          {reasonOptions.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </AdminFilterSelect>
        {fieldErrors.reason ? <p className="mt-1 text-sm text-admin-danger">{fieldErrors.reason}</p> : null}
      </div>

      <div>
        <label htmlFor="revoke-note" className="mb-1 block text-sm font-medium text-admin-text">
          Ghi chú (tùy chọn)
        </label>
        <textarea
          id="revoke-note"
          rows={3}
          value={note}
          onChange={(e) => onNoteChange(e.target.value)}
          placeholder="Nhập thêm chi tiết về quyết định này…"
          className="w-full resize-none rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-base text-admin-text outline-none transition-colors placeholder:text-admin-text-muted focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft"
        />
      </div>

      {actionLabel ? (
        <p className="text-xs text-admin-text-muted">Biện pháp: {actionLabel}</p>
      ) : null}

      {submitError ? <p className="text-sm text-admin-danger">{submitError}</p> : null}
    </div>
  );
}
