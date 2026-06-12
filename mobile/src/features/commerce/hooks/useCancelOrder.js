import { useCallback, useState } from "react";
import { cancelOrder } from "../api/orderDetailApi";
import { mapOrderActionApiError } from "../constants/orderActionConstants";
import { mapCancelOrderResponse } from "../utils/orderDetailMapper";

export function useCancelOrder({ onSuccess } = {}) {
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
        setErrorMessage(mapOrderActionApiError(error));
        throw error;
      } finally {
        setIsCancelling(false);
      }
    },
    [onSuccess],
  );

  const clearError = useCallback(() => setErrorMessage(""), []);

  return {
    isCancelling,
    errorMessage,
    cancel,
    clearError,
  };
}
