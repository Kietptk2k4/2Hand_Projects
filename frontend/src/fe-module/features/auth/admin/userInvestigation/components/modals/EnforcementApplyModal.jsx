import { useEffect, useState } from "react";
import {
  banUser,
  restrictUser,
  suspendUser,
} from "../../api/userInvestigationApi.js";
import {
  ENFORCEMENT_APPLY_CONFIG,
  ENFORCEMENT_REASON_OPTIONS,
} from "../../constants/enforcementFormConstants.js";
import {
  buildEnforcementPayload,
  validateEnforcementForm,
} from "../../utils/enforcementFormUtils.js";
import { AdminFilterButton } from "../../../components/ui";
import { InvestigationModalShell } from "./InvestigationModalShell.jsx";
import { EnforcementApplyModalView } from "./EnforcementApplyModalView.jsx";

const SUBMITTERS = {
  suspend: suspendUser,
  restrict: restrictUser,
  ban: banUser,
};

const CONFIRM_VARIANT = {
  ban: "bg-admin-danger text-white hover:bg-admin-danger/90",
  suspend: "bg-admin-warning text-white hover:bg-admin-warning/90",
  restrict: "bg-admin-accent text-white hover:bg-admin-accent-strong",
};

export function EnforcementApplyModal({
  open,
  actionType,
  userId,
  userLabel,
  onClose,
  onSuccess,
  onError,
}) {
  const config = ENFORCEMENT_APPLY_CONFIG[actionType];
  const [reasonCode, setReasonCode] = useState("");
  const [description, setDescription] = useState("");
  const [durationMode, setDurationMode] = useState("permanent");
  const [expiresAt, setExpiresAt] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!open) return;
    setReasonCode("");
    setDescription("");
    setDurationMode("permanent");
    setExpiresAt("");
    setFieldErrors({});
    setSubmitError("");
    setIsSubmitting(false);
  }, [open, actionType, userId]);

  if (!config) return null;

  const onSubmit = async () => {
    const errors = validateEnforcementForm({
      reasonCode,
      description,
      durationMode,
      expiresAt,
    });
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

    setIsSubmitting(true);
    setSubmitError("");

    try {
      const payload = buildEnforcementPayload({
        reasonCode,
        description,
        durationMode,
        expiresAt,
      });
      const submit = SUBMITTERS[actionType];
      const result = await submit(userId, payload);
      onSuccess?.(result);
      onClose?.();
    } catch (error) {
      const message = error?.message || "Không thể thực hiện hành động.";
      setSubmitError(message);
      onError?.(error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <InvestigationModalShell
      open={open}
      title={config.title}
      subtitle={userLabel ? `Đối tượng: ${userLabel}` : undefined}
      titleIcon={<span aria-hidden>!</span>}
      onClose={onClose}
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={isSubmitting}
            className={CONFIRM_VARIANT[actionType] ?? ""}
            onClick={onSubmit}
          >
            {isSubmitting ? "Đang xử lý…" : config.confirmLabel}
          </AdminFilterButton>
        </>
      }
    >
      <EnforcementApplyModalView
        config={config}
        reasonCode={reasonCode}
        description={description}
        durationMode={durationMode}
        expiresAt={expiresAt}
        fieldErrors={fieldErrors}
        submitError={submitError}
        reasonOptions={ENFORCEMENT_REASON_OPTIONS}
        onReasonChange={setReasonCode}
        onDescriptionChange={setDescription}
        onDurationModeChange={setDurationMode}
        onExpiresAtChange={setExpiresAt}
      />
    </InvestigationModalShell>
  );
}
