import { useCallback, useEffect, useState } from "react";
import { fetchAdminProductDetail } from "../api/adminProductRemovalApi.js";
import { mapAdminProductRemovalApiError } from "../constants/adminProductRemovalConstants.js";
import { mapProductModerationDetail } from "../utils/productModerationDetailMapper.js";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useProductModerationDetail(productId) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!productId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchAdminProductDetail(productId);
      setDetail(mapProductModerationDetail(data));
      setStatus("ready");
    } catch (error) {
      const code = String(error?.code ?? "");
      if (code === "401" || code.includes("401") || code.includes("COMMERCE-401")) {
        showSessionExpired(error?.message);
        return;
      }
      setDetail(null);
      setStatus("error");
      setErrorMessage(mapAdminProductRemovalApiError(error));
    }
  }, [productId, showSessionExpired]);

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
