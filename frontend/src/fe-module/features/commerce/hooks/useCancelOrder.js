import { useCallback, useState } from "react";
import { cancelOrder } from "../api/orderDetailApi";
import { mapOrderActionApiError } from "../constants/orderActionConstants";
import { mapCancelOrderResponse } from "../utils/orderDetailMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useCancelOrder({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const [isCancelling, setIsCancelling] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const cancel = useCallback(
    async (orderId, { reason } = {}) => {
      setIsCancelling(true);
      setErrorMessage("");

      try {
        const raw = await cancelOrder(orderId, reason);
        const result = mapCancelOrderResponse(raw);
        onSuccess?.(result);
        return result;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        setErrorMessage(mapOrderActionApiError(error));
        return null;
      } finally {
        setIsCancelling(false);
      }
    },
    [onSuccess, showSessionExpired],
  );

  const clearError = useCallback(() => setErrorMessage(""), []);

  return {
    isCancelling,
    errorMessage,
    cancel,
    clearError,
  };
}
