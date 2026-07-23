import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import { CONFIG_VALUE_TYPES } from "../constants/systemConfigConstants.js";
import { SYSTEM_CONFIG_VALUE_TYPE_LABELS } from "../constants/systemConfigListConstants.js";
import { GENERIC_CANCEL, GENERIC_CREATE } from "../constants/systemOperationsUiStrings.js";
import { SystemConfigValueEditor } from "./SystemConfigValueEditor.jsx";
import { SystemOperationsModalShell } from "./ui/SystemOperationsModalShell.jsx";

export function CreateSystemConfigModalView({
  open,
  form,
  fieldErrors = {},
  pending,
  onFieldChange,
  onClose,
  onSubmit,
}) {
  return (
    <SystemOperationsModalShell
      open={open}
      title="Tạo cấu hình mới"
      onClose={onClose}
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={pending} onClick={onClose}>
            {GENERIC_CANCEL}
          </AdminFilterButton>
          <AdminFilterButton type="submit" form="create-system-config-form" variant="primary" disabled={pending}>
            {GENERIC_CREATE}
          </AdminFilterButton>
        </>
      }
    >
      <form id="create-system-config-form" onSubmit={onSubmit} className="space-y-4">
        <AdminFilterField label="Config key" htmlFor="create-config-key">
          <AdminFilterInput
            id="create-config-key"
            required
            className="text-base font-mono"
            value={form.configKey}
            onChange={(e) => onFieldChange({ configKey: e.target.value })}
          />
          {fieldErrors.config_key ? (
            <p className="mt-1 text-xs text-admin-danger">{fieldErrors.config_key}</p>
          ) : null}
        </AdminFilterField>
        <AdminFilterField label="Giá trị" htmlFor="create-config-value">
          <SystemConfigValueEditor
            valueType={form.valueType}
            configKey={form.configKey}
            value={form.configValue}
            fieldError={fieldErrors.config_value}
            onChange={(nextValue) => onFieldChange({ configValue: nextValue })}
          />
        </AdminFilterField>
        <div className="grid gap-4 sm:grid-cols-2">
          <AdminFilterField label="Kiểu" htmlFor="create-config-type">
            <AdminFilterSelect
              id="create-config-type"
              className="text-base"
              value={form.valueType}
              onChange={(e) => onFieldChange({ valueType: e.target.value })}
            >
              {CONFIG_VALUE_TYPES.map((type) => (
                <option key={type} value={type}>
                  {SYSTEM_CONFIG_VALUE_TYPE_LABELS[type] || type}
                </option>
              ))}
            </AdminFilterSelect>
            {fieldErrors.value_type ? (
              <p className="mt-1 text-xs text-admin-danger">{fieldErrors.value_type}</p>
            ) : null}
          </AdminFilterField>
          <div className="flex items-end pb-1">
            <label className="flex min-h-11 items-center gap-2 text-sm text-admin-text">
              <input
                type="checkbox"
                checked={form.active}
                onChange={(e) => onFieldChange({ active: e.target.checked })}
                className="min-h-4 min-w-4"
              />
              Kích hoạt ngay
            </label>
          </div>
        </div>
        <AdminFilterField label="Mô tả" htmlFor="create-config-desc">
          <AdminFilterInput
            id="create-config-desc"
            className="text-base"
            value={form.description}
            onChange={(e) => onFieldChange({ description: e.target.value })}
          />
          {fieldErrors.description ? (
            <p className="mt-1 text-xs text-admin-danger">{fieldErrors.description}</p>
          ) : null}
        </AdminFilterField>
        <AdminFilterField label="Lý do thay đổi" htmlFor="create-config-reason">
          <textarea
            id="create-config-reason"
            required
            rows={2}
            value={form.reason}
            onChange={(e) => onFieldChange({ reason: e.target.value })}
            className="w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-base text-admin-text outline-none focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft"
          />
          {fieldErrors.reason ? (
            <p className="mt-1 text-xs text-admin-danger">{fieldErrors.reason}</p>
          ) : null}
        </AdminFilterField>
      </form>
    </SystemOperationsModalShell>
  );
}
