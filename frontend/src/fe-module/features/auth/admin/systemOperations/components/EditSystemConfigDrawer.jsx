import { useEffect, useMemo, useState } from "react";
import { CONFIG_VIEW_MODES } from "../constants/systemConfigConstants.js";
import { mapApiFieldErrors } from "../utils/systemConfigDisplayUtils.js";
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
  onRefresh,
}) {
  const [form, setForm] = useState({ configValue: "", description: "", reason: "", toggleReason: "" });
  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => {
    if (!config) return;
    setForm({
      configValue: config.configValue || "",
      description: config.description || "",
      reason: "",
      toggleReason: "",
    });
    setFieldErrors({});
  }, [config]);

  const isHistory = configView === CONFIG_VIEW_MODES.HISTORY;

  const title = useMemo(() => {
    if (!config) return "Chi tiết cấu hình";
    return isHistory ? `Lịch sử: ${config.configKey}` : `Chi tiết cấu hình`;
  }, [config, isHistory]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!canUpdate) return;
    setFieldErrors({});
    try {
      await onSave?.(form);
      onRefresh?.();
    } catch (error) {
      setFieldErrors(mapApiFieldErrors(error?.errors));
    }
  };

  const handleToggle = async (active, reason) => {
    setFieldErrors({});
    try {
      await onToggle?.(active, reason);
      onRefresh?.();
    } catch (error) {
      setFieldErrors(mapApiFieldErrors(error?.errors));
    }
  };

  return (
    <EditSystemConfigDrawerView
      open={Boolean(config)}
      title={title}
      configId={config?.configId}
      configKey={config?.configKey}
      isHistory={isHistory}
      loading={loading}
      config={config}
      form={form}
      fieldErrors={fieldErrors}
      canUpdate={canUpdate}
      pending={pending}
      historyPanel={
        config ? (
          <SystemConfigHistoryPanel configId={config.configId} enabled={isHistory} onRefresh={onRefresh} />
        ) : null
      }
      onClose={onClose}
      onViewChange={onViewChange}
      onFieldChange={(patch) => setForm((prev) => ({ ...prev, ...patch }))}
      onSubmit={handleSubmit}
      onToggle={handleToggle}
    />
  );
}
