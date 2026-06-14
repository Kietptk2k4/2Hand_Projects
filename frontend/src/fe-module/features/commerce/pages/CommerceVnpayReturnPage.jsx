import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { CommerceShell } from "../components/CommerceShell";
import { APP_ROUTES } from "../../../shared/constants/routes";

const FALLBACK_COUNTDOWN_SEC = 5;

/**
 * Backend đã verify chữ ký + cập nhật DB trước khi redirect về trang này.
 * Trang chỉ parse query và điều hướng nhanh tới danh sách đơn — không gọi API.
 */
export function CommerceVnpayReturnPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [countdown, setCountdown] = useState(FALLBACK_COUNTDOWN_SEC);

  const status = searchParams.get("status");
  const orderId = searchParams.get("orderId");

  const statusLabel = useMemo(() => {
    if (status === "success") return "success";
    if (status === "failed") return "failed";
    return status || "—";
  }, [status]);

  useEffect(() => {
    if (status === "success") {
      navigate(`${APP_ROUTES.commerceOrders}?status=PROCESSING`, { replace: true });
      return;
    }
    if (status === "failed") {
      navigate(`${APP_ROUTES.commerceOrders}?status=AWAITING_PAYMENT`, { replace: true });
      return;
    }

    const timer = setInterval(() => {
      setCountdown((value) => Math.max(0, value - 1));
    }, 1000);

    return () => clearInterval(timer);
  }, [navigate, status]);

  useEffect(() => {
    if (status) return;
    if (countdown === 0) {
      navigate(APP_ROUTES.commerceOrders, { replace: true });
    }
  }, [countdown, navigate, status]);

  return (
    <CommerceShell showHomeSidebar={false}>
      <div className="mx-auto max-w-lg rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
        <div
          className="mx-auto mb-4 h-10 w-10 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
          aria-hidden="true"
        />
        <h1 className="text-headline-md font-bold text-on-surface">Đang xử lý kết quả thanh toán</h1>
        <p className="mt-3 text-sm text-on-surface-variant">Đang điều hướng…</p>
        {orderId ? (
          <p className="mt-4 text-xs text-on-surface-variant">
            OrderId: <span className="font-mono text-on-surface">{orderId}</span>
          </p>
        ) : null}
        <p className="mt-1 text-xs text-on-surface-variant">Status: {statusLabel}</p>
        {!status ? (
          <p className="mt-4 text-xs text-on-surface-variant">
            Tự chuyển về danh sách đơn hàng sau {countdown}s…
          </p>
        ) : null}
      </div>
    </CommerceShell>
  );
}
