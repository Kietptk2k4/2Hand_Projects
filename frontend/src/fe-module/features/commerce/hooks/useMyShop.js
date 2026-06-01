import { useCallback, useEffect, useState } from "react";
import { fetchMyShop } from "../api/sellerShopApi";
import { mapShopSettingsApiError } from "../constants/shopSettingsConstants";
import { mapMyShopResponse } from "../utils/shopSettingsMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function isNotFoundError(error) {
  const code = String(error?.code ?? "");
  return code === "404" || code.includes("404") || code.includes("COMMERCE-404-SHOP");
}

export function useMyShop({ enabled = true } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [shop, setShop] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    if (!enabled) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const raw = await fetchMyShop();
      setShop(mapMyShopResponse(raw));
      setStatus("ready");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        setStatus("error");
        return;
      }
      if (isNotFoundError(error)) {
        setShop(null);
        setStatus("not_found");
        setErrorMessage(mapShopSettingsApiError(error));
        return;
      }
      setShop(null);
      setStatus("error");
      setErrorMessage(mapShopSettingsApiError(error));
    }
  }, [enabled, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    shop,
    isLoading: status === "loading",
    isNotFound: status === "not_found",
    isError: status === "error",
    errorMessage,
    reload: load,
  };
}
