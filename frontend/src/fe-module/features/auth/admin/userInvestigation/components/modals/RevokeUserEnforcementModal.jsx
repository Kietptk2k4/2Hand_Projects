import { useEffect, useState } from "react";
import { revokeUserEnforcement } from "../../api/userInvestigationApi.js";
import { REVOKE_REASON_OPTIONS } from "../../constants/enforcementFormConstants.js";
import { getEnforcementActionLabel } from "../../utils/investigationLabels.js";
import { validateRevokeForm } from "../../utils/enforcementFormUtils.js";
import { EnforcementActionBadge } from "../EnforcementBadges.jsx";
import { InvestigationModalShell } from "./InvestigationModalShell.jsx";

export function RevokeUserEnforcementModal({
  open,
  enforcement,
  userLabel,
  onClose,
  onSuccess,
  onError,
}) {
  const [reason, setReason] = useState("");
  const [note, setNote] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!open) return;
    setReason("");
    setNote("");
    setFieldErrors({});
    setSubmitError("");
    setIsSubmitting(false);
  }, [open, enforcement?.enforcement_id]);

  if (!enforcement) return null;

  const onSubmit = async () => {
    const errors = validateRevokeForm({ reason });
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

    setIsSubmitting(true);
    setSubmitError("");

    try {
      const payload = { reason };
      const trimmedNote = note.trim();
      if (trimmedNote) payload.note = trimmedNote;

      const result = await revokeUserEnforcement(enforcement.enforcement_id, payload);
      onSuccess?.(result);
      onClose?.();
    } catch (error) {
      const message = error?.message || "Không thể thu hồi enforcement.";
      setSubmitError(message);
      onError?.(error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <InvestigationModalShell
      open={open}
      title={`Thu hồi biện pháp: ${getEnforcementActionLabel(enforcement.action_type)}`}
      subtitle="Hành động này sẽ khôi phục quyền truy cập cho người dùng (nếu áp dụng)."
      titleIcon={<span aria-hidden>뿯↽</span>}
      onClose={onClose}
      maxWidthClass="max-w-[480px]"
      footer={
        <>
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="rounded-lg px-4 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Hủy bỏ
          </button>
          <button
            type="button"
            onClick={onSubmit}
            disabled={isSubmitting}
            className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-on-primary hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isSubmitting ? "Đang xử lý..." : "Xác nhận thu hồi"}
          </button>
        </>
      }
    >
      <div className="space-y-5">
        <div className="flex items-center gap-4 rounded-lg border border-outline-variant bg-surface-container-low p-4">
          <div className="min-w-0 flex-1">
            <p className="font-mono text-xs text-on-surface-variant">
              ID: {enforcement.enforcement_id?.slice(0, 12)}...
            </p>
            <p className="mt-1 text-sm font-semibold text-on-surface">
              {userLabel || "Người dùng đang điều tra"}
            </p>
            <p className="mt-1 text-sm text-on-surface-variant">{enforcement.reason_code}</p>
          </div>
          <EnforcementActionBadge actionType={enforcement.action_type} />
        </div>

        <div>
          <label htmlFor="revoke-reason" className="mb-1 block text-sm font-medium text-on-surface">
            Lý do thu hồi <span className="text-error">*</span>
          </label>
          <select
            id="revoke-reason"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary"
          >
            <option value="">Chọn lý do phù hợp</option>
            {REVOKE_REASON_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
          {fieldErrors.reason ? <p className="mt-1 text-sm text-error">{fieldErrors.reason}</p> : null}
        </div>

        <div>
          <label htmlFor="revoke-note" className="mb-1 block text-sm font-medium text-on-surface">
            Ghi chú (tùy chọn)
          </label>
          <textarea
            id="revoke-note"
            rows={3}
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="Nhập thêm chi tiết về quyết định này..."
            className="w-full resize-none rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary"
          />
        </div>

        {submitError ? <p className="text-sm text-error">{submitError}</p> : null}
      </div>
    </InvestigationModalShell>
  );
}
