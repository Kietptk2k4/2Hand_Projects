import { useCallback, useEffect, useState } from "react";
import { createPayOsCheckoutUrl } from "../api/paymentApi";
import { mapPayOsCheckoutUrlResponse } from "../utils/paymentMapper";

export function usePayOsCheckout(paymentId) {
  const [checkoutUrl, setCheckoutUrl] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const fetchCheckoutUrl = useCallback(async () => {
    if (!paymentId) return null;

    setIsLoading(true);
    setError("");

    try {
      const raw = await createPayOsCheckoutUrl(paymentId);
      const mapped = mapPayOsCheckoutUrlResponse(raw);
      const url = mapped?.payosCheckoutUrl || null;
      setCheckoutUrl(url);
      return mapped;
    } catch (err) {
      setError(err?.message || "Không tạo được liên kết thanh toán.");
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