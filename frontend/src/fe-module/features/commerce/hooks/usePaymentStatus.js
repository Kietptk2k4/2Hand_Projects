import { useCallback, useEffect, useRef, useState } from "react";
import { fetchPaymentStatus } from "../api/paymentApi";
import {
  PAYMENT_STATUS,
  PAYMENT_STATUS_POLL_INTERVAL_MS,
  PAYMENT_STATUS_POLL_MAX_ATTEMPTS,
} from "../constants/paymentConstants";
import { mapPaymentStatusResponse } from "../utils/paymentMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function usePaymentStatus(paymentId, { poll = false, mockPaid = false } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [payment, setPayment] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const pollAttemptsRef = useRef(0);
  const mockPaidSentRef = useRef(false);

  const refresh = useCallback(
    async ({ applyMockPaid = false } = {}) => {
      if (!paymentId) return null;

      setIsLoading(true);
      setError("");

      const shouldMockPaid = applyMockPaid && mockPaid && !mockPaidSentRef.current;

      try {
        const raw = await fetchPaymentStatus(paymentId, { mockPaid: shouldMockPaid });
        if (shouldMockPaid) {
          mockPaidSentRef.current = true;
        }
        const mapped = mapPaymentStatusResponse(raw);
        setPayment(mapped);
        return mapped;
      } catch (err) {
        if (isUnauthorizedError(err)) {
          showSessionExpired(err?.message);
          return null;
        }
        setError(err?.message || "Không tải được trạng thái thanh toán.");
        return null;
      } finally {
        setIsLoading(false);
      }
    },
    [paymentId, mockPaid, showSessionExpired]
  );

  useEffect(() => {
    if (!paymentId) return;
    pollAttemptsRef.current = 0;
    mockPaidSentRef.current = false;
    refresh({ applyMockPaid: true });
  }, [paymentId, refresh]);

  useEffect(() => {
    if (!poll || !paymentId) return;
    if (payment?.status !== PAYMENT_STATUS.PENDING) return;

    const intervalId = setInterval(async () => {
      if (pollAttemptsRef.current >= PAYMENT_STATUS_POLL_MAX_ATTEMPTS) {
        clearInterval(intervalId);
        return;
      }
      pollAttemptsRef.current += 1;
      const latest = await refresh();
      if (
        latest?.status !== PAYMENT_STATUS.PENDING ||
        pollAttemptsRef.current >= PAYMENT_STATUS_POLL_MAX_ATTEMPTS
      ) {
        clearInterval(intervalId);
      }
    }, PAYMENT_STATUS_POLL_INTERVAL_MS);

    return () => clearInterval(intervalId);
  }, [poll, paymentId, payment?.status, refresh]);

  return {
    payment,
    status: payment?.status ?? null,
    orderStatus: payment?.orderStatus ?? null,
    orderPaymentStatus: payment?.orderPaymentStatus ?? null,
    amount: payment?.amount ?? null,
    paidAt: payment?.paidAt ?? null,
    expiredAt: payment?.expiredAt ?? null,
    payosCheckoutUrl: payment?.payosCheckoutUrl ?? null,
    orderId: payment?.orderId ?? null,
    isLoading,
    error,
    refresh,
  };
}
