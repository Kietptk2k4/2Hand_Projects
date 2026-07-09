import {
  AdminFilterInput,
  AdminFilterSelect,
} from "../../../components/ui";

export function EnforcementApplyModalView({
  config,
  reasonCode,
  description,
  durationMode,
  expiresAt,
  fieldErrors,
  submitError,
  reasonOptions,
  onReasonChange,
  onDescriptionChange,
  onDurationModeChange,
  onExpiresAtChange,
}) {
  if (!config) return null;

  return (
    <div className="space-y-5">
      <div className="flex items-start gap-2 rounded-lg border border-admin-danger/30 bg-admin-danger-soft p-3 text-sm text-admin-danger">
        <span aria-hidden>!</span>
        <p>{config.warning}</p>
      </div>

      <div>
        <label htmlFor="enforcement-reason" className="mb-1 block text-sm font-medium text-admin-text">
          {config.reasonLabel} <span className="text-admin-danger">*</span>
        </label>
        <AdminFilterSelect
          id="enforcement-reason"
          value={reasonCode}
          onChange={(e) => onReasonChange(e.target.value)}
        >
          <option value="">Chọn lý do</option>
          {reasonOptions.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </AdminFilterSelect>
        {fieldErrors.reasonCode ? (
          <p className="mt-1 text-sm text-admin-danger">{fieldErrors.reasonCode}</p>
        ) : null}
      </div>

      <div>
        <label
          htmlFor="enforcement-description"
          className="mb-1 block text-sm font-medium text-admin-text"
        >
          Mô tả chi tiết <span className="text-admin-danger">*</span>
        </label>
        <textarea
          id="enforcement-description"
          rows={3}
          value={description}
          onChange={(e) => onDescriptionChange(e.target.value)}
          placeholder="Nhập chi tiết vi phạm…"
          className="w-full resize-none rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-base text-admin-text outline-none transition-colors placeholder:text-admin-text-muted focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft"
        />
        {fieldErrors.description ? (
          <p className="mt-1 text-sm text-admin-danger">{fieldErrors.description}</p>
        ) : null}
      </div>

      {config.supportsTemporary ? (
        <div className="space-y-3">
          <p className="text-sm font-medium text-admin-text">{config.durationLabel}</p>
          <div className="flex rounded-lg bg-admin-surface-muted p-1">
            <button
              type="button"
              onClick={() => onDurationModeChange("temporary")}
              className={[
                "min-h-11 flex-1 rounded-md py-2 text-sm font-medium transition-colors",
                durationMode === "temporary"
                  ? "border border-admin-border bg-admin-surface text-admin-text shadow-sm"
                  : "text-admin-text-muted hover:text-admin-text",
              ].join(" ")}
            >
              Tạm thời
            </button>
            <button
              type="button"
              onClick={() => onDurationModeChange("permanent")}
              className={[
                "min-h-11 flex-1 rounded-md py-2 text-sm font-medium transition-colors",
                durationMode === "permanent"
                  ? "border border-admin-border bg-admin-surface text-admin-text shadow-sm"
                  : "text-admin-text-muted hover:text-admin-text",
              ].join(" ")}
            >
              Vĩnh viễn
            </button>
          </div>
          {durationMode === "temporary" ? (
            <div>
              <label htmlFor="enforcement-expires" className="mb-1 block text-xs text-admin-text-muted">
                Đến ngày
              </label>
              <AdminFilterInput
                id="enforcement-expires"
                type="datetime-local"
                value={expiresAt}
                onChange={(e) => onExpiresAtChange(e.target.value)}
              />
              {fieldErrors.expiresAt ? (
                <p className="mt-1 text-sm text-admin-danger">{fieldErrors.expiresAt}</p>
              ) : null}
            </div>
          ) : null}
        </div>
      ) : null}

      {submitError ? <p className="text-sm text-admin-danger">{submitError}</p> : null}
    </div>
  );
}
