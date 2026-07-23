import { useCallback, useEffect, useState } from "react";
import { getWebhookLogDetailForSupport } from "../api/orderSupportApi.js";

export function useWebhookSupportDetail(logId, provider, { enabled = true } = {}) {
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const refetch = useCallback(async () => {
    if (!enabled || !logId || !provider) {
      setDetail(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");
    try {
      const data = await getWebhookLogDetailForSupport(logId, provider);
      setDetail(data);
      setStatus("ready");
    } catch (error) {
      setDetail(null);
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được chi tiết webhook.");
    }
  }, [enabled, logId, provider]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { detail, status, errorMessage, refetch };
}
