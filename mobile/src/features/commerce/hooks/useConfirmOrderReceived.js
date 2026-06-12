import { useCallback, useState } from "react";
import { confirmOrderReceived } from "../api/orderDetailApi";
import { mapOrderActionApiError } from "../constants/orderActionConstants";
import { mapConfirmOrderReceivedResponse } from "../utils/orderDetailMapper";

export function useConfirmOrderReceived({ onSuccess } = {}) {
  const [isConfirming, setIsConfirming] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const confirm = useCallback(
    async (orderId) => {
      setIsConfirming(true);
      setErrorMessage("");

      try {
        const raw = await confirmOrderReceived(orderId);
        const result = mapConfirmOrderReceivedResponse(raw);
        onSuccess?.(result);
        return result;
      } catch (error) {
        setErrorMessage(mapOrderActionApiError(error));
        throw error;
      } finally {
        setIsConfirming(false);
      }
    },
    [onSuccess],
  );

  const clearError = useCallback(() => setErrorMessage(""), []);

  return {
    isConfirming,
    errorMessage,
    confirm,
    clearError,
  };
}
