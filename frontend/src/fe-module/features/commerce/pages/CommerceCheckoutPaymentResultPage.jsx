import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { createPayOsCheckoutUrl } from "../api/paymentApi";
import { CommerceShell } from "../components/CommerceShell";
import { PaymentStatusPanel } from "../components/PaymentStatusPanel";
import { canRetryPayment } from "../constants/paymentStatusLabels";
import { usePaymentStatus } from "../hooks/usePaymentStatus";
import { mapPayOsCheckoutUrlResponse } from "../utils/paymentMapper";
import { APP_ROUTES } from "../../../shared/constants/routes";

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export function CommerceCheckoutPaymentResultPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const paymentId = searchParams.get("paymentId")?.trim() || "";
  const mockPaid = searchParams.get("mockPaid") === "1";
  const mockFailed = searchParams.get("mockFailed") === "1";
  const mockExpired = searchParams.get("mockExpired") === "1";
  const [isRetrying, setIsRetrying] = useState(false);

  const isValidPaymentId = useMemo(() => UUID_REGEX.test(paymentId), [paymentId]);

  useEffect(() => {
    if (!isValidPaymentId) {
      navigate(APP_ROUTES.commerceHome, { replace: true });
    }
  }, [isValidPaymentId, navigate]);

  const {
    status,
    orderId,
    amount,
    paidAt,
    expiredAt,
    orderStatus,
    orderPaymentStatus,
    payosCheckoutUrl,
    isLoading,
    error,
    refresh,
  } = usePaymentStatus(isValidPaymentId ? paymentId : null, {
    poll: true,
    mockPaid,
    mockFailed,
    mockExpired,
  });

  const showRetry = useMemo(
    () => canRetryPayment({ paymentStatus: status, orderStatus }),
    [status, orderStatus]
  );

  const handleRetryPayment = useCallback(async () => {
    if (!paymentId) return;

    setIsRetrying(true);
    try {
      let url = payosCheckoutUrl;
      if (!url) {
        const raw = await createPayOsCheckoutUrl(paymentId);
        url = mapPayOsCheckoutUrlResponse(raw)?.payosCheckoutUrl;
      }
      if (url) {
        window.location.assign(url);
      } else {
        await refresh();
      }
    } catch {
      await refresh();
    } finally {
      setIsRetrying(false);
    }
  }, [paymentId, payosCheckoutUrl, refresh]);

  if (!isValidPaymentId) {
    return null;
  }

  return (
    <CommerceShell showHomeSidebar={false}>
      <div className="mx-auto max-w-lg rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
        <PaymentStatusPanel
          status={status}
          orderId={orderId}
          amount={amount}
          paidAt={paidAt}
          expiredAt={expiredAt}
          orderStatus={orderStatus}
          orderPaymentStatus={orderPaymentStatus}
          isLoading={isLoading}
          error={error}
          showRetry={showRetry}
          onRetryPayment={showRetry ? handleRetryPayment : undefined}
          isRetrying={isRetrying}
        />
      </div>
    </CommerceShell>
  );
}
