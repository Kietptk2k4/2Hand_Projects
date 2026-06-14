import { useCallback, useState } from "react";
import { retryVnpayPayment } from "../api/paymentApi";
import { mapVnpayRetryResponse } from "../utils/paymentMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function useVnpayRetry(orderId) {
  const { showSessionExpired } = useAuthSession();
  const [isRetrying, setIsRetrying] = useState(false);
  const [error, setError] = useState("");

  const retry = useCallback(async () => {
    if (!orderId) return false;

    setIsRetrying(true);
    setError("");

    try {
      const raw = await retryVnpayPayment(orderId);
      const mapped = mapVnpayRetryResponse(raw);
      const redirect = mapped?.redirect;

      if (redirect) {
        window.location.assign(redirect);
        return true;
      }

      setError("Không tạo được liên kết thanh toán VNPay.");
      return false;
    } catch (err) {
      if (isUnauthorizedError(err)) {
        showSessionExpired(err?.message);
        return false;
      }
      setError(err?.message || "Không thể tạo lại liên kết thanh toán.");
      return false;
    } finally {
      setIsRetrying(false);
    }
  }, [orderId, showSessionExpired]);

  return { retry, isRetrying, error, clearError: () => setError("") };
}
