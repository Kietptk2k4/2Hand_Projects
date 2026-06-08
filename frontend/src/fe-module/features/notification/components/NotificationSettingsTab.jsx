import { useMemo } from "react";
import { AccountCard, TabPanelHeader } from "../../../shared/ui/auth/authUi.jsx";
import { NOTIFICATION_EVENT_GROUPS } from "../constants/notificationConstants";
import { useNotificationSettings } from "../hooks/useNotificationSettings";
import { NotificationDeviceTokensSection } from "./NotificationDeviceTokensSection";

function SettingToggle({ label, checked, disabled, onChange }) {
  return (
    <label className="inline-flex items-center gap-2 text-xs text-on-surface-variant">
      <span>{label}</span>
      <input
        type="checkbox"
        checked={checked}
        disabled={disabled}
        onChange={(event) => onChange(event.target.checked)}
        className="h-4 w-4 rounded border-outline-variant text-primary focus:ring-primary/30"
      />
    </label>
  );
}

function SettingRow({ setting, disabled, onUpdate }) {
  const handleToggle = (field) => async (value) => {
    await onUpdate(setting.eventType, {
      allowPush: field === "allowPush" ? value : setting.allowPush,
      allowEmail: field === "allowEmail" ? value : setting.allowEmail,
      allowInApp: field === "allowInApp" ? value : setting.allowInApp,
    });
  };

  return (
    <div className="flex flex-col gap-3 border-b border-outline-variant/60 py-4 last:border-b-0 sm:flex-row sm:items-center sm:justify-between">
      <div className="min-w-0">
        <p className="text-sm font-medium text-on-surface">{setting.label}</p>
        {!setting.explicitSetting ? (
          <span className="mt-1 inline-flex rounded-full bg-surface-container-low px-2 py-0.5 text-[11px] font-medium text-on-surface-variant">
            Mac dinh
          </span>
        ) : null}
      </div>

      <div className="flex flex-wrap items-center gap-4">
        <SettingToggle
          label="Push"
          checked={setting.allowPush}
          disabled={disabled}
          onChange={handleToggle("allowPush")}
        />
        <SettingToggle
          label="Email"
          checked={setting.allowEmail}
          disabled={disabled}
          onChange={handleToggle("allowEmail")}
        />
        <SettingToggle
          label="Trong app"
          checked={setting.allowInApp}
          disabled={disabled}
          onChange={handleToggle("allowInApp")}
        />
      </div>
    </div>
  );
}

export function NotificationSettingsTab({ onNotify }) {
  const { settings, status, errorMessage, updatingEventType, reload, updateSetting } =
    useNotificationSettings();

  const groupedSettings = useMemo(() => {
    const settingsByType = new Map(settings.map((item) => [item.eventType, item]));

    return NOTIFICATION_EVENT_GROUPS.map((group) => ({
      ...group,
      items: group.eventTypes
        .map((eventType) => settingsByType.get(eventType))
        .filter(Boolean),
    })).filter((group) => group.items.length > 0);
  }, [settings]);

  const ungroupedSettings = useMemo(() => {
    const groupedTypes = new Set(NOTIFICATION_EVENT_GROUPS.flatMap((group) => group.eventTypes));
    return settings.filter((item) => !groupedTypes.has(item.eventType));
  }, [settings]);

  const handleUpdate = async (eventType, nextFlags) => {
    try {
      await updateSetting(eventType, nextFlags);
      onNotify?.({ variant: "success", message: "Da cap nhat cai dat thong bao." });
    } catch (error) {
      onNotify?.({ variant: "error", message: error?.message || "Khong the cap nhat cai dat." });
    }
  };

  return (
    <div>
      <TabPanelHeader
        title="Thong bao"
        subtitle="Chon kenh nhan thong bao theo tung loai su kien."
      />

      <AccountCard>
        {status === "loading" ? (
          <p className="text-sm text-on-surface-variant">Dang tai cai dat...</p>
        ) : null}

        {status === "error" ? (
          <div>
            <p className="text-sm text-error">{errorMessage}</p>
            <button
              type="button"
              onClick={reload}
              className="mt-3 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
            >
              Thu lai
            </button>
          </div>
        ) : null}

        {status === "ready" ? (
          <div className="space-y-8">
            {groupedSettings.map((group) => (
              <section key={group.id}>
                <h2 className="mb-2 text-base font-semibold text-on-surface">{group.label}</h2>
                <div>
                  {group.items.map((setting) => (
                    <SettingRow
                      key={setting.eventType}
                      setting={setting}
                      disabled={updatingEventType === setting.eventType}
                      onUpdate={handleUpdate}
                    />
                  ))}
                </div>
              </section>
            ))}

            {ungroupedSettings.length > 0 ? (
              <section>
                <h2 className="mb-2 text-base font-semibold text-on-surface">Khac</h2>
                <div>
                  {ungroupedSettings.map((setting) => (
                    <SettingRow
                      key={setting.eventType}
                      setting={setting}
                      disabled={updatingEventType === setting.eventType}
                      onUpdate={handleUpdate}
                    />
                  ))}
                </div>
              </section>
            ) : null}
          </div>
        ) : null}
      </AccountCard>

      <NotificationDeviceTokensSection />
    </div>
  );
}
