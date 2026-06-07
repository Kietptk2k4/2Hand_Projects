import { useCallback, useEffect } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { CommerceShell } from "../components/CommerceShell";
import { PAYOS_AUTO_REDIRECT_MS } from "../constants/paymentConstants";
import { usePayOsCheckout } from "../hooks/usePayOsCheckout";
import { useCartBadge } from "../context/CartBadgeContext";
import { APP_ROUTES } from "../../../shared/constants/routes";

function CodSuccessContent({ state }) {
  return (
    <>
      <span
        className="material-symbols-outlined mb-4 text-5xl text-primary"
        style={{ fontVariationSettings: "'FILL' 1" }}
        aria-hidden="true"
      >
        check_circle
      </span>

      <h1 className="text-headline-md font-bold text-on-surface">Đặt hàng thành công</h1>
      <p className="mt-2 text-sm text-on-surface-variant">
        Mã đơn hàng: <span className="font-mono font-medium text-on-surface">{state.orderId}</span>
      </p>

      {state.finalAmount != null ? (
        <p className="mt-4 text-headline-sm font-semibold text-primary">
          {formatVndPrice(state.finalAmount)}
        </p>
      ) : null}

      <div className="mt-6 rounded-lg bg-surface-container-low p-4 text-left text-sm text-on-surface">
        <p>Đơn hàng đang được xử lý. Bạn sẽ thanh toán khi nhận hàng.</p>
        <p className="mt-2 text-xs text-on-surface-variant">
          Trạng thái: {state.orderStatus} · Thanh toán: {state.paymentStatus}
        </p>
      </div>

      <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
        <Link
          to={APP_ROUTES.commerceHome}
          className="rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
        >
          Tiếp tục mua sắm
        </Link>
        <Link
          to={APP_ROUTES.commerceCart}
          className="rounded-lg border border-primary px-6 py-2.5 text-sm font-medium text-primary hover:bg-surface-container-low"
        >
          Xem giỏ hàng
        </Link>
      </div>
    </>
  );
}

function PayOsSuccessContent({ state }) {
  const { checkoutUrl, isLoading, error, retry } = usePayOsCheckout(state.paymentId);

  const goToPayOs = useCallback(() => {
    if (checkoutUrl) {
      window.location.assign(checkoutUrl);
    }
  }, [checkoutUrl]);

  useEffect(() => {
    if (!checkoutUrl || isLoading || error) return;

    const timer = setTimeout(() => {
      window.location.assign(checkoutUrl);
    }, PAYOS_AUTO_REDIRECT_MS);

    return () => clearTimeout(timer);
  }, [checkoutUrl, isLoading, error]);

  return (
    <>
      <span className="material-symbols-outlined mb-4 text-5xl text-amber-600" aria-hidden="true">
        payments
      </span>

      <h1 className="text-headline-md font-bold text-on-surface">Đặt hàng thành công</h1>
      <p className="mt-2 text-sm text-on-surface-variant">
        Mã đơn hàng: <span className="font-mono font-medium text-on-surface">{state.orderId}</span>
      </p>

      {state.finalAmount != null ? (
        <p className="mt-4 text-headline-sm font-semibold text-primary">
          {formatVndPrice(state.finalAmount)}
        </p>
      ) : null}

      <div className="mt-6 rounded-lg bg-surface-container-low p-4 text-left text-sm text-on-surface">
        <p className="font-medium">Chờ thanh toán qua PayOS</p>
        <p className="mt-1 text-on-surface-variant">
          Vui lòng hoàn tất thanh toán để đơn hàng được xử lý.
        </p>
        <p className="mt-2 text-xs text-on-surface-variant">
          Trạng thái đơn: {state.orderStatus} · Thanh toán: {state.paymentStatus}
        </p>
      </div>

      {isLoading ? (
        <p className="mt-6 text-sm text-on-surface-variant">Đang tạo liên kết thanh toán...</p>
      ) : null}

      {error ? (
        <div className="mt-6 space-y-3">
          <p className="text-sm text-error">{error}</p>
          <button
            type="button"
            onClick={retry}
            className="rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
          >
            Thử lại
          </button>
        </div>
      ) : null}

      {!isLoading && !error && checkoutUrl ? (
        <div className="mt-6 space-y-3">
          <p className="text-sm text-on-surface-variant">
            Bạn sẽ được chuyển tới PayOS trong giây lát...
          </p>
          <button
            type="button"
            onClick={goToPayOs}
            className="w-full rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb] sm:w-auto"
          >
            Thanh toán ngay qua PayOS
          </button>
        </div>
      ) : null}

      <Link
        to={APP_ROUTES.commerceHome}
        className="mt-6 inline-block text-sm text-primary hover:underline"
      >
        Thanh toán sau
      </Link>
    </>
  );
}

export function CommerceCheckoutSuccessPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state || {};
  const { refetch: refetchCartBadge } = useCartBadge();

  const isCod = state.paymentMethod === "COD";
  const isPayos = state.paymentMethod === "PAYOS";

  useEffect(() => {
    if (state.orderId) {
      refetchCartBadge();
    }
  }, [refetchCartBadge, state.orderId]);

  useEffect(() => {
    if (!state.orderId) {
      navigate(APP_ROUTES.commerceHome, { replace: true });
      return;
    }
    if (isPayos && !state.paymentId) {
      navigate(APP_ROUTES.commerceCart, { replace: true });
    }
  }, [navigate, state.orderId, state.paymentId, isPayos]);

  if (!state.orderId) {
    return null;
  }

  if (isPayos && !state.paymentId) {
    return null;
  }

  return (
    <CommerceShell showHomeSidebar={false}>
      <div className="mx-auto max-w-lg rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
        {isCod ? <CodSuccessContent state={state} /> : null}
        {isPayos ? <PayOsSuccessContent state={state} /> : null}
        {!isCod && !isPayos ? <CodSuccessContent state={state} /> : null}
      </div>
    </CommerceShell>
  );
}
