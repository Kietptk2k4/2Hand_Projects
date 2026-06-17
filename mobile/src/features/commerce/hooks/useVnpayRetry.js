import { useCallback, useState } from "react";
import { retryVnpayPayment } from "../api/paymentApi";
import { mapVnpayRetryResponse } from "../utils/paymentMapper";
import { openPayOsBrowser } from "../utils/openPayOsBrowser";

export function useVnpayRetry(orderId) {
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
        await openPayOsBrowser(redirect);
        return true;
      }

      setError("Không tạo được liên kết thanh toán VNPay.");
      return false;
    } catch (err) {
      setError(err?.message || "Không thể tạo lại liên kết thanh toán.");
      return false;
    } finally {
      setIsRetrying(false);
    }
  }, [orderId]);

  return { retry, isRetrying, error, clearError: () => setError("") };
}
