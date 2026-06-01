import { Link } from "react-router-dom";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { PAYMENT_STATUS } from "../constants/paymentConstants";
import { APP_ROUTES } from "../../../shared/constants/routes";

function formatDateTime(iso) {
  if (!iso) return "";
  try {
    return new Date(iso).toLocaleString("vi-VN");
  } catch {
    return iso;
  }
}

export function PaymentStatusPanel({
  status,
  orderId,
  amount,
  paidAt,
  expiredAt,
  orderStatus,
  orderPaymentStatus,
  isLoading,
  error,
  onRetryPayment,
  isRetrying,
}) {
  if (isLoading && !status) {
    return (
      <p className="text-sm text-on-surface-variant">Đang kiểm tra trạng thái thanh toán...</p>
    );
  }

  if (error) {
    return (
      <div className="rounded-lg border border-error/30 bg-error-container/20 p-4 text-left text-sm">
        <p className="text-on-surface">{error}</p>
      </div>
    );
  }

  if (status === PAYMENT_STATUS.PAID) {
    return (
      <>
        <span
          className="material-symbols-outlined mb-4 text-5xl text-primary"
          style={{ fontVariationSettings: "'FILL' 1" }}
          aria-hidden="true"
        >
          check_circle
        </span>
        <h1 className="text-headline-md font-bold text-on-surface">Thanh toán thành công</h1>
        <p className="mt-2 text-sm text-on-surface-variant">
          Cảm ơn bạn. Đơn hàng đã được xác nhận thanh toán.
        </p>
        {orderId ? (
          <p className="mt-2 text-sm text-on-surface-variant">
            Mã đơn: <span className="font-mono font-medium text-on-surface">{orderId}</span>
          </p>
        ) : null}
        {amount != null ? (
          <p className="mt-4 text-headline-sm font-semibold text-primary">{formatVndPrice(amount)}</p>
        ) : null}
        {paidAt ? (
          <p className="mt-2 text-xs text-on-surface-variant">Thanh toán lúc: {formatDateTime(paidAt)}</p>
        ) : null}
        <p className="mt-4 text-xs text-on-surface-variant">
          Đơn hàng: {orderStatus} · Thanh toán: {orderPaymentStatus}
        </p>
        <Link
          to={APP_ROUTES.commerceHome}
          className="mt-8 inline-block rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
        >
          Tiếp tục mua sắm
        </Link>
      </>
    );
  }

  if (status === PAYMENT_STATUS.PENDING) {
    return (
      <>
        <span className="material-symbols-outlined mb-4 text-5xl text-amber-600" aria-hidden="true">
          schedule
        </span>
        <h1 className="text-headline-md font-bold text-on-surface">Chưa nhận được xác nhận thanh toán</h1>
        <p className="mt-2 text-sm text-on-surface-variant">
          Bạn đã quay lại từ PayOS. Hệ thống chưa ghi nhận thanh toán thành công — vui lòng đợi hoặc thử
          thanh toán lại.
        </p>
        {orderId ? (
          <p className="mt-2 text-sm text-on-surface-variant">
            Mã đơn: <span className="font-mono font-medium text-on-surface">{orderId}</span>
          </p>
        ) : null}
        {amount != null ? (
          <p className="mt-4 text-headline-sm font-semibold text-primary">{formatVndPrice(amount)}</p>
        ) : null}
        {expiredAt ? (
          <p className="mt-2 text-xs text-on-surface-variant">
            Hạn thanh toán: {formatDateTime(expiredAt)}
          </p>
        ) : null}
        <p className="mt-4 text-xs text-on-surface-variant">
          Đơn hàng: {orderStatus} · Thanh toán: {orderPaymentStatus}
        </p>
        {isLoading ? (
          <p className="mt-4 text-sm text-on-surface-variant">Đang cập nhật trạng thái...</p>
        ) : null}
        <button
          type="button"
          onClick={onRetryPayment}
          disabled={isRetrying}
          className="mt-6 rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-60"
        >
          {isRetrying ? "Đang tạo liên kết..." : "Thanh toán lại"}
        </button>
        <Link
          to={APP_ROUTES.commerceHome}
          className="mt-3 block text-sm text-primary hover:underline"
        >
          Về trang chủ mua sắm
        </Link>
      </>
    );
  }

  const isExpired = status === PAYMENT_STATUS.EXPIRED;

  return (
    <>
      <span
        className="material-symbols-outlined mb-4 text-5xl text-error"
        style={{ fontVariationSettings: "'FILL' 1" }}
        aria-hidden="true"
      >
        error
      </span>
      <h1 className="text-headline-md font-bold text-on-surface">
        {isExpired ? "Thanh toán đã hết hạn" : "Thanh toán không thành công"}
      </h1>
      <p className="mt-2 text-sm text-on-surface-variant">
        {isExpired
          ? "Liên kết hoặc phiên thanh toán đã hết hạn. Bạn có thể tạo liên kết mới."
          : "Giao dịch thất bại hoặc bị hủy. Vui lòng thử lại."}
      </p>
      {orderId ? (
        <p className="mt-2 text-sm text-on-surface-variant">
          Mã đơn: <span className="font-mono font-medium text-on-surface">{orderId}</span>
        </p>
      ) : null}
      <button
        type="button"
        onClick={onRetryPayment}
        disabled={isRetrying}
        className="mt-6 rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-60"
      >
        {isRetrying ? "Đang tạo liên kết..." : "Tạo liên kết thanh toán mới"}
      </button>
      <Link
        to={APP_ROUTES.commerceHome}
        className="mt-3 block text-sm text-primary hover:underline"
      >
        Về trang chủ mua sắm
      </Link>
    </>
  );
}
