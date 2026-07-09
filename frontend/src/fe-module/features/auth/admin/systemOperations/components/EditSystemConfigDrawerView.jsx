import { AdminFilterButton, AdminFilterField, AdminFilterInput, AdminSurfaceCard } from "../../components/ui";
import { CONFIG_VIEW_MODES } from "../constants/systemConfigConstants.js";
import { GENERIC_SAVE } from "../constants/systemOperationsUiStrings.js";
import { SystemOperationsListSkeleton } from "./ui/SystemOperationsListSkeleton.jsx";

export function EditSystemConfigDrawerView({
  open,
  title,
  configId,
  isHistory,
  loading,
  config,
  form,
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
              <h2 className="text-lg font-semibold text-admin-text">{title}</h2>
              <p className="mt-1 break-all font-mono text-xs text-admin-text-muted">{configId}</p>
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

          <div className="flex flex-wrap gap-2 border-b border-admin-border px-4 py-3 sm:px-6">
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

          <div className="flex-1 overflow-y-auto px-4 py-4 sm:px-6">
            {loading ? <SystemOperationsListSkeleton rows={4} /> : null}
            {!loading && isHistory ? historyPanel : null}
            {!loading && !isHistory ? (
              <form onSubmit={onSubmit} className="space-y-4">
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
                  <textarea
                    id="edit-config-value"
                    required
                    disabled={!canUpdate}
                    rows={4}
                    value={form.configValue}
                    onChange={(e) => onFieldChange({ configValue: e.target.value })}
                    className="w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-base font-mono text-admin-text outline-none focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft disabled:bg-admin-surface-muted"
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
                  </AdminFilterField>
                ) : null}
                {canUpdate ? (
                  <AdminSurfaceCard padding="md">
                    <p className="text-sm font-medium text-admin-text">
                      Trạng thái: {config.active ? "Đang bật" : "Đang tắt"}
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
