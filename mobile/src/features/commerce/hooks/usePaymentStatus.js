import { useCallback, useEffect, useRef, useState } from "react";
import { fetchPaymentStatus } from "../api/paymentApi";
import {
  isTerminalPaymentStatus,
  PAYMENT_STATUS_POLL_INTERVAL_MS,
  PAYMENT_STATUS_POLL_MAX_ATTEMPTS,
} from "../constants/paymentConstants";
import { mapPaymentStatusResponse } from "../utils/paymentMapper";

export function usePaymentStatus(
  paymentId,
  { poll = false, mockPaid = false, mockFailed = false, mockExpired = false } = {}
) {
  const [payment, setPayment] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const pollAttemptsRef = useRef(0);
  const mockPaidSentRef = useRef(false);
  const mockFailedSentRef = useRef(false);
  const mockExpiredSentRef = useRef(false);

  const refresh = useCallback(
    async ({ applyMocks = false } = {}) => {
      if (!paymentId) return null;

      setIsLoading(true);
      setError("");

      const shouldMockPaid = applyMocks && mockPaid && !mockPaidSentRef.current;
      const shouldMockFailed = applyMocks && mockFailed && !mockFailedSentRef.current;
      const shouldMockExpired = applyMocks && mockExpired && !mockExpiredSentRef.current;

      try {
        const raw = await fetchPaymentStatus(paymentId, {
          mockPaid: shouldMockPaid,
          mockFailed: shouldMockFailed,
          mockExpired: shouldMockExpired,
        });
        if (shouldMockPaid) mockPaidSentRef.current = true;
        if (shouldMockFailed) mockFailedSentRef.current = true;
        if (shouldMockExpired) mockExpiredSentRef.current = true;

        const mapped = mapPaymentStatusResponse(raw);
        setPayment(mapped);
        return mapped;
      } catch (err) {
        setError(err?.message || "Không tải được trạng thái thanh toán.");
        return null;
      } finally {
        setIsLoading(false);
      }
    },
    [paymentId, mockPaid, mockFailed, mockExpired]
  );

  useEffect(() => {
    if (!paymentId) return;
    pollAttemptsRef.current = 0;
    mockPaidSentRef.current = false;
    mockFailedSentRef.current = false;
    mockExpiredSentRef.current = false;
    refresh({ applyMocks: true });
  }, [paymentId, refresh]);

  useEffect(() => {
    if (!poll || !paymentId) return;
    if (isTerminalPaymentStatus(payment?.status)) return;

    const intervalId = setInterval(async () => {
      if (pollAttemptsRef.current >= PAYMENT_STATUS_POLL_MAX_ATTEMPTS) {
        clearInterval(intervalId);
        return;
      }
      pollAttemptsRef.current += 1;
      const latest = await refresh();
      if (
        isTerminalPaymentStatus(latest?.status) ||
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
    paymentMethod: payment?.paymentMethod ?? null,
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