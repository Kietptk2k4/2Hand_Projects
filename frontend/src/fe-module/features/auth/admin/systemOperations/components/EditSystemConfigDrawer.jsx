import { useEffect, useMemo, useState } from "react";
import { AccountSkeleton } from "../../../../../shared/ui/auth/authUi.jsx";
import { CONFIG_VIEW_MODES } from "../constants/systemConfigConstants.js";
import { GENERIC_CLOSE, GENERIC_SAVE } from "../constants/systemOperationsUiStrings.js";
import { SystemConfigHistoryPanel } from "./SystemConfigHistoryPanel.jsx";

export function EditSystemConfigDrawer({
  config,
  configView,
  loading,
  canUpdate,
  pending,
  onClose,
  onSave,
  onToggle,
  onViewChange,
}) {
  const [form, setForm] = useState({ configValue: "", description: "", reason: "", toggleReason: "" });

  useEffect(() => {
    if (!config) return;
    setForm({
      configValue: config.configValue || "",
      description: config.description || "",
      reason: "",
      toggleReason: "",
    });
  }, [config]);

  const isHistory = configView === CONFIG_VIEW_MODES.HISTORY;

  const title = useMemo(() => {
    if (!config) return "Chi tiết cấu hình";
    return isHistory ? `Lịch sử: ${config.configKey}` : `Sửa: ${config.configKey}`;
  }, [config, isHistory]);

  if (!config) return null;

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!canUpdate) return;
    await onSave?.(form);
  };

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <button type="button" aria-label="Đóng" className="absolute inset-0 bg-black/40" onClick={onClose} />
      <aside className="relative flex h-full w-full max-w-xl flex-col border-l border-outline-variant bg-surface shadow-xl">
        <div className="flex items-start justify-between border-b border-outline-variant px-6 py-5">
          <div>
            <h2 className="text-lg font-semibold text-on-surface">{title}</h2>
            <p className="mt-1 font-mono text-xs text-on-surface-variant">{config.configId}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg px-3 py-1.5 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
          >
            {GENERIC_CLOSE}
          </button>
        </div>

        <div className="flex gap-2 border-b border-outline-variant px-6 py-3">
          <button
            type="button"
            onClick={() => onViewChange?.(CONFIG_VIEW_MODES.EDIT)}
            className={[
              "rounded-lg px-3 py-1.5 text-sm font-medium",
              !isHistory ? "bg-primary/10 text-primary" : "text-on-surface-variant hover:bg-surface-container-low",
            ].join(" ")}
          >
            Chi tiết
          </button>
          <button
            type="button"
            onClick={() => onViewChange?.(CONFIG_VIEW_MODES.HISTORY)}
            className={[
              "rounded-lg px-3 py-1.5 text-sm font-medium",
              isHistory ? "bg-primary/10 text-primary" : "text-on-surface-variant hover:bg-surface-container-low",
            ].join(" ")}
          >
            Lịch sử
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-6 py-5">
          {loading ? <AccountSkeleton /> : null}
          {!loading && isHistory ? (
            <SystemConfigHistoryPanel configId={config.configId} enabled />
          ) : null}
          {!loading && !isHistory ? (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Key</label>
                <input disabled value={config.configKey} className="w-full rounded-lg border border-outline-variant bg-surface-container-low px-3 py-2 text-sm font-mono" />
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Kiểu</label>
                <input disabled value={config.valueType} className="w-full rounded-lg border border-outline-variant bg-surface-container-low px-3 py-2 text-sm" />
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Giá trị</label>
                <textarea
                  required
                  disabled={!canUpdate}
                  rows={4}
                  value={form.configValue}
                  onChange={(e) => setForm((prev) => ({ ...prev, configValue: e.target.value }))}
                  className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm font-mono disabled:bg-surface-container-low"
                />
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Mô tả</label>
                <input
                  disabled={!canUpdate}
                  value={form.description}
                  onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
                  className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm disabled:bg-surface-container-low"
                />
              </div>
              {canUpdate ? (
                <div>
                  <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Lý do cập nhật</label>
                  <textarea
                    required
                    rows={2}
                    value={form.reason}
                    onChange={(e) => setForm((prev) => ({ ...prev, reason: e.target.value }))}
                    className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
                  />
                </div>
              ) : null}
              {canUpdate ? (
                <div className="rounded-lg border border-outline-variant p-4">
                  <p className="text-sm font-medium text-on-surface">
                    Trạng thái: {config.active ? "Đang bật" : "Đang tắt"}
                  </p>
                  <textarea
                    rows={2}
                    placeholder="Lý do bật/tắt"
                    value={form.toggleReason}
                    onChange={(e) => setForm((prev) => ({ ...prev, toggleReason: e.target.value }))}
                    className="mt-2 w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
                  />
                  <button
                    type="button"
                    disabled={pending || !form.toggleReason.trim()}
                    onClick={() => onToggle?.(!config.active, form.toggleReason)}
                    className="mt-3 rounded-lg border border-primary px-3 py-1.5 text-sm font-medium text-primary disabled:opacity-40"
                  >
                    {config.active ? "Tắt cấu hình" : "Bật cấu hình"}
                  </button>
                </div>
              ) : null}
              {canUpdate ? (
                <button
                  type="submit"
                  disabled={pending}
                  className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white disabled:opacity-50"
                >
                  {GENERIC_SAVE}
                </button>
              ) : null}
            </form>
          ) : null}
        </div>
      </aside>
    </div>
  );
}