import { useEffect, useMemo, useState } from "react";
import { CONFIG_VIEW_MODES } from "../constants/systemConfigConstants.js";
import { EditSystemConfigDrawerView } from "./EditSystemConfigDrawerView.jsx";
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

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!canUpdate) return;
    await onSave?.(form);
  };

  return (
    <EditSystemConfigDrawerView
      open={Boolean(config)}
      title={title}
      configId={config?.configId}
      isHistory={isHistory}
      loading={loading}
      config={config}
      form={form}
      canUpdate={canUpdate}
      pending={pending}
      historyPanel={
        config ? <SystemConfigHistoryPanel configId={config.configId} enabled={isHistory} /> : null
      }
      onClose={onClose}
      onViewChange={onViewChange}
      onFieldChange={(patch) => setForm((prev) => ({ ...prev, ...patch }))}
      onSubmit={handleSubmit}
      onToggle={onToggle}
    />
  );
}
