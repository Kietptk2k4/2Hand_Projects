import { useCallback, useEffect, useState } from "react";
import { createPayOsCheckoutUrl } from "../api/paymentApi";
import { mapPayOsCheckoutUrlResponse } from "../utils/paymentMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function usePayOsCheckout(paymentId) {
  const { showSessionExpired } = useAuthSession();
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
      if (isUnauthorizedError(err)) {
        showSessionExpired(err?.message);
        return null;
      }
      setError(err?.message || "Không tạo được liên kết thanh toán.");
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [paymentId, showSessionExpired]);

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
