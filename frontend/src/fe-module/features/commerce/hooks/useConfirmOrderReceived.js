import { useCallback, useState } from "react";
import { confirmOrderReceived } from "../api/orderDetailApi";
import { mapOrderActionApiError } from "../constants/orderActionConstants";
import { mapConfirmOrderReceivedResponse } from "../utils/orderDetailMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useConfirmOrderReceived({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
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
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        setErrorMessage(mapOrderActionApiError(error));
        return null;
      } finally {
        setIsConfirming(false);
      }
    },
    [onSuccess, showSessionExpired],
  );

  const clearError = useCallback(() => setErrorMessage(""), []);

  return {
    isConfirming,
    errorMessage,
    confirm,
    clearError,
  };
}
