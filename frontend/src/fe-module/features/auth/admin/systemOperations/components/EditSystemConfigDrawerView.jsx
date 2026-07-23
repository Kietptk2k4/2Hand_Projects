import { Link } from "react-router-dom";
import { buildAdminSearchParams } from "../../adminUrlParams.js";
import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminSurfaceCard,
} from "../../components/ui";
import { CONFIG_VIEW_MODES } from "../constants/systemConfigConstants.js";
import { GENERIC_SAVE } from "../constants/systemOperationsUiStrings.js";
import { isSecretLikeConfigKey } from "../utils/systemConfigDisplayUtils.js";
import { ConfigActiveBadge } from "./ui/SystemOperationsBadges.jsx";
import { SystemConfigValueEditor } from "./SystemConfigValueEditor.jsx";
import { SystemOperationsListSkeleton } from "./ui/SystemOperationsListSkeleton.jsx";

function AuditLogLink({ configId }) {
  if (!configId) return null;

  const to = `/admin?${buildAdminSearchParams({
    section: "adminAudit",
    tab: "action-logs",
    auditFilters: {
      target_type: "CONFIG",
      target_id: configId,
    },
  }).toString()}`;

  return (
    <Link
      to={to}
      className="inline-flex min-h-9 items-center gap-1 text-xs font-medium text-admin-accent hover:underline"
    >
      <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
        history
      </span>
      Xem trong nhật ký audit
    </Link>
  );
}

export function EditSystemConfigDrawerView({
  open,
  title,
  configId,
  configKey,
  isHistory,
  loading,
  config,
  form,
  fieldErrors = {},
  canUpdate,
  pending,
  historyPanel,
  onClose,
  onViewChange,
  onFieldChange,
  onSubmit,
  onToggle,
}) {
  if (!open || !config) return null;

  const showSecretWarning =
    config.valueMasked || isSecretLikeConfigKey(config.configKey || configKey);

  const tabClass = (active) =>
    [
      "min-h-11 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
      active
        ? "bg-admin-accent-soft text-admin-accent-strong"
        : "text-admin-text-secondary hover:bg-admin-surface-muted",
    ].join(" ");

  return (
    <>
      <div className="fixed inset-0 z-[100] flex min-h-dvh justify-end bg-admin-text/40 backdrop-blur-sm">
        <button type="button" aria-label="Đóng" className="absolute inset-0" onClick={onClose} />
        <aside className="relative flex h-full min-h-dvh w-full max-w-xl flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]">
          <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
            <div className="min-w-0">
              <div className="mb-2 flex flex-wrap items-center gap-2">
                <ConfigActiveBadge active={config.active} />
              </div>
              <h2 className="break-all font-mono text-base font-semibold text-admin-text">
                {config.configKey || configKey}
              </h2>
              <p className="mt-1 text-sm text-admin-text-secondary">{title}</p>
            </div>
            <button
              type="button"
              onClick={onClose}
              className="flex min-h-11 min-w-11 shrink-0 items-center justify-center rounded-lg text-admin-text-muted hover:bg-admin-surface hover:text-admin-text"
              aria-label="Đóng"
            >
              ×
            </button>
          </div>

          <div className="flex flex-wrap items-center justify-between gap-2 border-b border-admin-border px-4 py-3 sm:px-6">
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => onViewChange?.(CONFIG_VIEW_MODES.EDIT)}
                className={tabClass(!isHistory)}
              >
                Chi tiết
              </button>
              <button
                type="button"
                onClick={() => onViewChange?.(CONFIG_VIEW_MODES.HISTORY)}
                className={tabClass(isHistory)}
              >
                Lịch sử
              </button>
            </div>
            <AuditLogLink configId={configId} />
          </div>

          <div className="flex-1 overflow-y-auto px-4 py-4 sm:px-6">
            {loading ? <SystemOperationsListSkeleton rows={4} /> : null}
            {!loading && isHistory ? historyPanel : null}
            {!loading && !isHistory ? (
              <form onSubmit={onSubmit} className="space-y-4">
                {showSecretWarning ? (
                  <p className="rounded-lg border border-admin-warning/40 bg-admin-warning-soft px-3 py-2 text-xs text-admin-warning">
                    Key này có thể chứa dữ liệu nhạy cảm. Giá trị có thể được che trên giao diện.
                  </p>
                ) : null}

                <AdminFilterField label="Key" htmlFor="edit-config-key">
                  <AdminFilterInput
                    id="edit-config-key"
                    disabled
                    className="font-mono"
                    value={config.configKey}
                    readOnly
                  />
                </AdminFilterField>
                <AdminFilterField label="Kiểu" htmlFor="edit-config-type">
                  <AdminFilterInput id="edit-config-type" disabled value={config.valueType} readOnly />
                </AdminFilterField>
                <AdminFilterField label="Giá trị" htmlFor="edit-config-value">
                  <SystemConfigValueEditor
                    valueType={config.valueType}
                    configKey={config.configKey}
                    value={form.configValue}
                    valueMasked={config.valueMasked}
                    disabled={!canUpdate}
                    fieldError={fieldErrors.config_value}
                    onChange={(nextValue) => onFieldChange({ configValue: nextValue })}
                  />
                </AdminFilterField>
                <AdminFilterField label="Mô tả" htmlFor="edit-config-desc">
                  <AdminFilterInput
                    id="edit-config-desc"
                    className="text-base"
                    disabled={!canUpdate}
                    value={form.description}
                    onChange={(e) => onFieldChange({ description: e.target.value })}
                  />
                  {fieldErrors.description ? (
                    <p className="mt-1 text-xs text-admin-danger">{fieldErrors.description}</p>
                  ) : null}
                </AdminFilterField>
                {canUpdate ? (
                  <AdminFilterField label="Lý do cập nhật" htmlFor="edit-config-reason">
                    <textarea
                      id="edit-config-reason"
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
                ) : null}
                {canUpdate ? (
                  <AdminSurfaceCard padding="md">
                    <p className="text-sm font-medium text-admin-text">
                      Trạng thái: {config.active ? "Đang bật" : "Đã tắt"}
                    </p>
                    <textarea
                      rows={2}
                      placeholder="Lý do bật/tắt"
                      value={form.toggleReason}
                      onChange={(e) => onFieldChange({ toggleReason: e.target.value })}
                      className="mt-2 w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-base text-admin-text outline-none focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft"
                    />
                    <AdminFilterButton
                      type="button"
                      variant="secondary"
                      className="mt-3 min-h-11 border-admin-accent-border text-admin-accent"
                      disabled={pending || !form.toggleReason.trim()}
                      onClick={() => onToggle?.(!config.active, form.toggleReason)}
                    >
                      {config.active ? "Tắt cấu hình" : "Bật cấu hình"}
                    </AdminFilterButton>
                  </AdminSurfaceCard>
                ) : null}
                {canUpdate ? (
                  <AdminFilterButton type="submit" variant="primary" className="min-h-11" disabled={pending}>
                    {GENERIC_SAVE}
                  </AdminFilterButton>
                ) : null}
              </form>
            ) : null}
          </div>
        </aside>
      </div>
    </>
  );
}
