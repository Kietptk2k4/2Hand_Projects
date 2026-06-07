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
import { InvestigationModalShell } from "./InvestigationModalShell.jsx";

const SUBMITTERS = {
  suspend: suspendUser,
  restrict: restrictUser,
  ban: banUser,
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
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Huy
          </button>
          <button
            type="button"
            onClick={onSubmit}
            disabled={isSubmitting}
            className={[
              "rounded-lg px-4 py-2 text-sm font-semibold shadow-sm disabled:cursor-not-allowed disabled:opacity-50",
              config.confirmClass,
            ].join(" ")}
          >
            {isSubmitting ? "Đang xử lý..." : config.confirmLabel}
          </button>
        </>
      }
    >
      <div className="space-y-5">
        <div className="flex items-start gap-2 rounded-lg border border-error-container bg-error-container/30 p-3 text-sm text-on-error-container">
          <span aria-hidden>i</span>
          <p>{config.warning}</p>
        </div>

        <div>
          <label htmlFor="enforcement-reason" className="mb-1 block text-sm font-medium text-on-surface">
            {config.reasonLabel} <span className="text-error">*</span>
          </label>
          <select
            id="enforcement-reason"
            value={reasonCode}
            onChange={(e) => setReasonCode(e.target.value)}
            className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary"
          >
            <option value="">Chọn lý do</option>
            {ENFORCEMENT_REASON_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
          {fieldErrors.reasonCode ? (
            <p className="mt-1 text-sm text-error">{fieldErrors.reasonCode}</p>
          ) : null}
        </div>

        <div>
          <label htmlFor="enforcement-description" className="mb-1 block text-sm font-medium text-on-surface">
            Mô tả chi tiết <span className="text-error">*</span>
          </label>
          <textarea
            id="enforcement-description"
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Nhập chi tiết vi phạm..."
            className="w-full resize-none rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary"
          />
          {fieldErrors.description ? (
            <p className="mt-1 text-sm text-error">{fieldErrors.description}</p>
          ) : null}
        </div>

        {config.supportsTemporary ? (
          <div className="space-y-3">
            <p className="text-sm font-medium text-on-surface">{config.durationLabel}</p>
            <div className="flex rounded-lg bg-surface-container-low p-1">
              <button
                type="button"
                onClick={() => setDurationMode("temporary")}
                className={[
                  "flex-1 rounded-md py-2 text-sm font-medium transition-colors",
                  durationMode === "temporary"
                    ? "border border-outline-variant/20 bg-surface-container-lowest text-on-surface shadow-sm"
                    : "text-on-surface-variant hover:text-on-surface",
                ].join(" ")}
              >
                Tạm thời
              </button>
              <button
                type="button"
                onClick={() => setDurationMode("permanent")}
                className={[
                  "flex-1 rounded-md py-2 text-sm font-medium transition-colors",
                  durationMode === "permanent"
                    ? "border border-outline-variant/20 bg-surface-container-lowest text-on-surface shadow-sm"
                    : "text-on-surface-variant hover:text-on-surface",
                ].join(" ")}
              >
                Vĩnh viễn
              </button>
            </div>
            {durationMode === "temporary" ? (
              <div>
                <label htmlFor="enforcement-expires" className="mb-1 block text-xs text-on-surface-variant">
                  Đến ngày
                </label>
                <input
                  id="enforcement-expires"
                  type="datetime-local"
                  value={expiresAt}
                  onChange={(e) => setExpiresAt(e.target.value)}
                  className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary"
                />
                {fieldErrors.expiresAt ? (
                  <p className="mt-1 text-sm text-error">{fieldErrors.expiresAt}</p>
                ) : null}
              </div>
            ) : null}
          </div>
        ) : null}

        {submitError ? <p className="text-sm text-error">{submitError}</p> : null}
      </div>
    </InvestigationModalShell>
  );
}
