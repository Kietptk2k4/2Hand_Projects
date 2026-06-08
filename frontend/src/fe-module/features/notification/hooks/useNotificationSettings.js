import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { fetchNotificationSettings, updateNotificationSetting } from "../api/notificationApi";
import { mapNotificationSettingItem } from "../utils/notificationMapper";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function useNotificationSettings({ enabled = true } = {}) {
  const { isAuthenticated, showSessionExpired } = useAuthSession();
  const [settings, setSettings] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [updatingEventType, setUpdatingEventType] = useState(null);

  const reload = useCallback(async () => {
    if (!enabled || !isAuthenticated) {
      setSettings([]);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchNotificationSettings();
      const items = Array.isArray(data?.settings) ? data.settings.map(mapNotificationSettingItem) : [];
      setSettings(items);
      setStatus("ready");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
      } else {
        setErrorMessage(error?.message || "Khong tai duoc cai dat thong bao.");
      }
      setStatus("error");
    }
  }, [enabled, isAuthenticated, showSessionExpired]);

  useEffect(() => {
    reload();
  }, [reload]);

  const updateSetting = useCallback(
    async (eventType, nextFlags) => {
      const previous = settings;
      setUpdatingEventType(eventType);
      setSettings((current) =>
        current.map((item) =>
          item.eventType === eventType
            ? {
                ...item,
                allowPush: nextFlags.allowPush,
                allowEmail: nextFlags.allowEmail,
                allowInApp: nextFlags.allowInApp,
                explicitSetting: true,
              }
            : item
        )
      );

      try {
        const data = await updateNotificationSetting(eventType, nextFlags);
        setSettings((current) =>
          current.map((item) =>
            item.eventType === eventType ? mapNotificationSettingItem(data) : item
          )
        );
      } catch (error) {
        setSettings(previous);
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
        }
        throw error;
      } finally {
        setUpdatingEventType(null);
      }
    },
    [settings, showSessionExpired]
  );

  return {
    settings,
    status,
    errorMessage,
    updatingEventType,
    reload,
    updateSetting,
  };
}
