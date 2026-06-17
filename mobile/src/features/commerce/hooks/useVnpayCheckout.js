import { useCallback, useEffect, useState } from "react";
import { createVnpayCheckoutUrl } from "../api/paymentApi";
import { mapVnpayCheckoutUrlResponse } from "../utils/paymentMapper";

export function useVnpayCheckout(paymentId) {
  const [checkoutUrl, setCheckoutUrl] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const fetchCheckoutUrl = useCallback(async () => {
    if (!paymentId) return null;

    setIsLoading(true);
    setError("");

    try {
      const raw = await createVnpayCheckoutUrl(paymentId);
      const mapped = mapVnpayCheckoutUrlResponse(raw);
      const url = mapped?.redirect || null;
      setCheckoutUrl(url);
      return mapped;
    } catch (err) {
      setError(err?.message || "Không tạo được liên kết thanh toán VNPay.");
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [paymentId]);

  useEffect(() => {
    if (paymentId) {
      fetchCheckoutUrl();
    }
  }, [paymentId, fetchCheckoutUrl]);

  return {
    checkoutUrl,
    isLoading,
    error,
    retry: fetchCheckoutUrl,
  };
}
