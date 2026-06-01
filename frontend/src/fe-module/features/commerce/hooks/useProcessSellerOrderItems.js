import { useCallback, useState } from "react";
import { processSellerOrderItems } from "../api/sellerOrderApi";
import { mapSellerOrderApiError } from "../constants/sellerOrderConstants";
import { mapProcessSellerOrderItemsResponse } from "../utils/sellerOrderMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useProcessSellerOrderItems({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const [isProcessing, setIsProcessing] = useState(false);
  const [processError, setProcessError] = useState("");

  const process = useCallback(
    async (orderItemIds) => {
      const ids = (orderItemIds || []).filter(Boolean);
      if (ids.length === 0) {
        setProcessError("Chưa chọn mục đơn.");
        return null;
      }

      setIsProcessing(true);
      setProcessError("");

      try {
        const raw = await processSellerOrderItems(ids);
        const result = mapProcessSellerOrderItemsResponse(raw);
        onSuccess?.(result);
        return result;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        const message = mapSellerOrderApiError(error);
        setProcessError(message);
        return null;
      } finally {
        setIsProcessing(false);
      }
    },
    [onSuccess, showSessionExpired],
  );

  const clearError = useCallback(() => setProcessError(""), []);

  return {
    isProcessing,
    processError,
    process,
    clearError,
  };
}
