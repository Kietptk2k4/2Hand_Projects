import { useCallback, useEffect, useState } from "react";
import { fetchAdminShopDetail } from "../api/adminShopModerationApi.js";
import { mapAdminShopModerationApiError } from "../constants/adminShopModerationConstants.js";
import { mapShopModerationDetail } from "../utils/shopModerationDetailMapper.js";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useShopModerationDetail(shopId) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!shopId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchAdminShopDetail(shopId);
      setDetail(mapShopModerationDetail(data));
      setStatus("ready");
    } catch (error) {
      const code = String(error?.code ?? "");
      if (code === "401" || code.includes("401") || code.includes("COMMERCE-401")) {
        showSessionExpired(error?.message);
        return;
      }
      setDetail(null);
      setStatus("error");
      setErrorMessage(mapAdminShopModerationApiError(error));
    }
  }, [shopId, showSessionExpired]);

  useEffect(() => {
    fetchDetail();
  }, [fetchDetail]);

  return {
    detail,
    status,
    errorMessage,
    refetch: fetchDetail,
  };
}
