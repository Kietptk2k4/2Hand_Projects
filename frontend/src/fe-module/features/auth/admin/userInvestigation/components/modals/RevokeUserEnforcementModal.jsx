import { useEffect, useState } from "react";
import { revokeUserEnforcement } from "../../api/userInvestigationApi.js";
import { REVOKE_REASON_OPTIONS } from "../../constants/enforcementFormConstants.js";
import { getEnforcementActionLabel } from "../../utils/investigationLabels.js";
import { validateRevokeForm } from "../../utils/enforcementFormUtils.js";
import { AdminFilterButton } from "../../../components/ui";
import { InvestigationModalShell } from "./InvestigationModalShell.jsx";
import { RevokeUserEnforcementModalView } from "./RevokeUserEnforcementModalView.jsx";

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
      titleIcon={<span aria-hidden>↩</span>}
      onClose={onClose}
      maxWidthClass="max-w-[480px]"
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Hủy bỏ
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={isSubmitting}
            onClick={onSubmit}
          >
            {isSubmitting ? "Đang xử lý…" : "Xác nhận thu hồi"}
          </AdminFilterButton>
        </>
      }
    >
      <RevokeUserEnforcementModalView
        enforcement={enforcement}
        userLabel={userLabel}
        reason={reason}
        note={note}
        fieldErrors={fieldErrors}
        submitError={submitError}
        reasonOptions={REVOKE_REASON_OPTIONS}
        actionLabel={getEnforcementActionLabel(enforcement.action_type)}
        onReasonChange={setReason}
        onNoteChange={setNote}
      />
    </InvestigationModalShell>
  );
}
