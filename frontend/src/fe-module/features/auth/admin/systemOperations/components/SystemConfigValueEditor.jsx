import { useMemo, useState } from "react";
import { AdminFilterButton } from "../../components/ui";
import { isSecretLikeConfigKey } from "../utils/systemConfigDisplayUtils.js";

function FieldError({ message }) {
  if (!message) return null;
  return <p className="mt-1 text-xs text-admin-danger">{message}</p>;
}

function JsonPreview({ value }) {
  const preview = useMemo(() => {
    if (!value?.trim()) return { ok: true, text: "" };
    try {
      return { ok: true, text: JSON.stringify(JSON.parse(value), null, 2) };
    } catch {
      return { ok: false, text: "JSON không hợp lệ." };
    }
  }, [value]);

  if (!value?.trim()) return null;

  return (
    <div
      className={`mt-2 rounded-lg border px-3 py-2 text-xs ${
        preview.ok
          ? "border-admin-border bg-admin-surface-muted text-admin-text-secondary"
          : "border-admin-danger/40 bg-admin-danger-soft/20 text-admin-danger"
      }`}
    >
      {preview.ok ? (
        <pre className="max-h-32 overflow-auto whitespace-pre-wrap font-mono">{preview.text}</pre>
      ) : (
        preview.text
      )}
    </div>
  );
}

export function SystemConfigValueEditor({
  valueType,
  configKey,
  value,
  valueMasked,
  disabled,
  fieldError,
  onChange,
}) {
  const [showSecret, setShowSecret] = useState(false);
  const masked = valueMasked || isSecretLikeConfigKey(configKey);
  const displayMasked = masked && !showSecret;

  if (displayMasked) {
    return (
      <div className="space-y-2">
        <div className="flex items-center gap-2 rounded-lg border border-admin-warning/40 bg-admin-warning-soft px-3 py-2 text-xs text-admin-warning">
          <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
            lock
          </span>
          Giá trị được che vì lý do bảo mật.
        </div>
        {canRevealSecret(disabled) ? (
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-9 text-xs"
            onClick={() => setShowSecret(true)}
          >
            Hiện giá trị
          </AdminFilterButton>
        ) : null}
        <FieldError message={fieldError} />
      </div>
    );
  }

  if (valueType === "BOOLEAN") {
    const checked = String(value).toLowerCase() === "true";
    return (
      <div className="space-y-2">
        <label className="flex min-h-11 items-center gap-3 text-sm text-admin-text">
          <input
            type="checkbox"
            checked={checked}
            disabled={disabled}
            onChange={(event) => onChange?.(event.target.checked ? "true" : "false")}
            className="min-h-4 min-w-4"
          />
          <span>{checked ? "true" : "false"}</span>
        </label>
        <FieldError message={fieldError} />
      </div>
    );
  }

  if (valueType === "INTEGER" || valueType === "DECIMAL") {
    return (
      <div>
        <input
          type="number"
          step={valueType === "DECIMAL" ? "any" : "1"}
          disabled={disabled}
          value={value}
          onChange={(event) => onChange?.(event.target.value)}
          className="w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-base font-mono text-admin-text outline-none focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft disabled:bg-admin-surface-muted"
        />
        <FieldError message={fieldError} />
      </div>
    );
  }

  if (valueType === "JSON") {
    return (
      <div>
        <textarea
          rows={5}
          disabled={disabled}
          value={value}
          onChange={(event) => onChange?.(event.target.value)}
          className="w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-base font-mono text-admin-text outline-none focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft disabled:bg-admin-surface-muted"
        />
        {!disabled ? (
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="mt-2 min-h-9 text-xs"
            onClick={() => {
              try {
                onChange?.(JSON.stringify(JSON.parse(value), null, 2));
              } catch {
                // keep invalid JSON as-is
              }
            }}
          >
            Định dạng JSON
          </AdminFilterButton>
        ) : null}
        <JsonPreview value={value} />
        <FieldError message={fieldError} />
      </div>
    );
  }

  return (
    <div>
      <textarea
        rows={3}
        disabled={disabled}
        value={value}
        onChange={(event) => onChange?.(event.target.value)}
        className="w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-base font-mono text-admin-text outline-none focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft disabled:bg-admin-surface-muted"
      />
      <FieldError message={fieldError} />
    </div>
  );
}

function canRevealSecret(disabled) {
  return !disabled;
}
