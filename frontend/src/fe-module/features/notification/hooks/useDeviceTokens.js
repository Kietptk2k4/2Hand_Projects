import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { fetchUserDeviceTokens, revokeDeviceToken } from "../api/notificationApi";
import { mapDeviceTokenItem } from "../utils/notificationMapper";

const LOCAL_DEVICE_TOKEN_KEY = "2hands_fcm_device_token";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function getStoredDeviceToken() {
  try {
    return window.localStorage.getItem(LOCAL_DEVICE_TOKEN_KEY);
  } catch {
    return null;
  }
}

export function useDeviceTokens({ enabled = true } = {}) {
  const { isAuthenticated, showSessionExpired } = useAuthSession();
  const [items, setItems] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [revokingId, setRevokingId] = useState(null);

  const reload = useCallback(async () => {
    if (!enabled || !isAuthenticated) {
      setItems([]);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchUserDeviceTokens();
      const mapped = Array.isArray(data?.items) ? data.items.map(mapDeviceTokenItem) : [];
      setItems(mapped);
      setStatus("ready");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
      } else {
        setErrorMessage(error?.message || "Khong tai duoc danh sach thiet bi.");
      }
      setStatus("error");
    }
  }, [enabled, isAuthenticated, showSessionExpired]);

  useEffect(() => {
    reload();
  }, [reload]);

  const revokeToken = useCallback(
    async (deviceToken) => {
      if (!deviceToken) {
        throw { message: "Khong tim thay token thiet bi tren may nay." };
      }

      setRevokingId(deviceToken);
      try {
        await revokeDeviceToken(deviceToken);
        try {
          window.localStorage.removeItem(LOCAL_DEVICE_TOKEN_KEY);
        } catch {
          // ignore storage errors
        }
        await reload();
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
        }
        throw error;
      } finally {
        setRevokingId(null);
      }
    },
    [reload, showSessionExpired]
  );

  return {
    items,
    status,
    errorMessage,
    revokingId,
    reload,
    revokeToken,
    localDeviceToken: getStoredDeviceToken(),
  };
}
