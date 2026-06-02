import { Link } from "react-router-dom";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { PAYMENT_STATUS } from "../constants/paymentConstants";
import { isOrderCancelledDueToPayment } from "../constants/paymentStatusLabels";
import {
  formatOrderPaymentStatusLabel,
  formatOrderStatusLabel,
  formatPaymentStatusLabel,
} from "../utils/paymentDisplay";
import { APP_ROUTES } from "../../../shared/constants/routes";

function formatDateTime(iso) {
  if (!iso) return "";
  try {
    return new Date(iso).toLocaleString("vi-VN");
  } catch {
    return iso;
  }
}

function OrderStatusLines({ orderStatus, orderPaymentStatus }) {
  return (
    <p className="mt-4 text-xs text-on-surface-variant">
      Đơn hàng: {formatOrderStatusLabel(orderStatus)} · Thanh toán đơn:{" "}
      {formatOrderPaymentStatusLabel(orderPaymentStatus)}
    </p>
  );
}

function PaymentResultLinks({ orderId }) {
  return (
    <div className="mt-6 flex flex-col items-center gap-2">
      {orderId ? (
        <Link
          to={APP_ROUTES.commerceOrderDetail.replace(":orderId", orderId)}
          className="text-sm font-medium text-primary hover:underline"
        >
          Xem chi tiết đơn
        </Link>
      ) : null}
      <Link to={APP_ROUTES.commerceOrders} className="text-sm text-primary hover:underline">
        Danh sách đơn hàng
      </Link>
      <Link to={APP_ROUTES.commerceHome} className="text-sm text-on-surface-variant hover:underline">
        Về trang chủ mua sắm
      </Link>
    </div>
  );
}

function OrderCancelledNotice() {
  return (
    <p className="mt-3 rounded-lg border border-outline-variant bg-surface-container-low px-3 py-2 text-sm text-on-surface-variant">
      Đơn hàng đã bị hủy. Sản phẩm đã được trả lại kho — bạn có thể đặt lại nếu còn hàng.
    </p>
  );
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
  showRetry = false,
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
        <PaymentResultLinks orderId={orderId} />
      </div>
    );
  }

  const orderCancelled = isOrderCancelledDueToPayment(orderStatus, orderPaymentStatus);

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
        <OrderStatusLines orderStatus={orderStatus} orderPaymentStatus={orderPaymentStatus} />
        <Link
          to={APP_ROUTES.commerceHome}
          className="mt-8 inline-block rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
        >
          Tiếp tục mua sắm
        </Link>
        {orderId ? (
          <div className="mt-3">
            <Link
              to={APP_ROUTES.commerceOrderDetail.replace(":orderId", orderId)}
              className="text-sm text-primary hover:underline"
            >
              Xem chi tiết đơn
            </Link>
          </div>
        ) : null}
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
          Bạn đã quay lại từ PayOS. Hệ thống đang chờ xác nhận từ cổng thanh toán — vui lòng đợi
          thêm vài giây hoặc thử thanh toán lại nếu đã quá lâu.
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
        <p className="mt-2 text-xs text-on-surface-variant">
          Trạng thái thanh toán: {formatPaymentStatusLabel(status)}
        </p>
        <OrderStatusLines orderStatus={orderStatus} orderPaymentStatus={orderPaymentStatus} />
        {isLoading ? (
          <p className="mt-4 text-sm text-on-surface-variant">Đang cập nhật trạng thái...</p>
        ) : null}
        {showRetry ? (
          <button
            type="button"
            onClick={onRetryPayment}
            disabled={isRetrying}
            className="mt-6 rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-60"
          >
            {isRetrying ? "Đang tạo liên kết..." : "Thanh toán lại"}
          </button>
        ) : null}
        <PaymentResultLinks orderId={orderId} />
      </>
    );
  }

  if (status === PAYMENT_STATUS.EXPIRED) {
    return (
      <>
        <span className="material-symbols-outlined mb-4 text-5xl text-amber-700" aria-hidden="true">
          timer_off
        </span>
        <h1 className="text-headline-md font-bold text-on-surface">Thanh toán đã hết hạn</h1>
        <p className="mt-2 text-sm text-on-surface-variant">
          Phiên thanh toán hoặc hạn thanh toán đã kết thúc trước khi giao dịch hoàn tất.
        </p>
        {orderCancelled ? <OrderCancelledNotice /> : null}
        {orderId ? (
          <p className="mt-2 text-sm text-on-surface-variant">
            Mã đơn: <span className="font-mono font-medium text-on-surface">{orderId}</span>
          </p>
        ) : null}
        {amount != null ? (
          <p className="mt-4 text-headline-sm font-semibold text-primary">{formatVndPrice(amount)}</p>
        ) : null}
        <OrderStatusLines orderStatus={orderStatus} orderPaymentStatus={orderPaymentStatus} />
        {showRetry ? (
          <button
            type="button"
            onClick={onRetryPayment}
            disabled={isRetrying}
            className="mt-6 rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-60"
          >
            {isRetrying ? "Đang tạo liên kết..." : "Tạo liên kết thanh toán mới"}
          </button>
        ) : null}
        <PaymentResultLinks orderId={orderId} />
      </>
    );
  }

  if (status === PAYMENT_STATUS.CANCELLED) {
    return (
      <>
        <span className="material-symbols-outlined mb-4 text-5xl text-on-surface-variant" aria-hidden="true">
          cancel
        </span>
        <h1 className="text-headline-md font-bold text-on-surface">Thanh toán đã hủy</h1>
        <p className="mt-2 text-sm text-on-surface-variant">
          Giao dịch đã bị hủy trên cổng thanh toán. Đơn hàng không được xác nhận thanh toán.
        </p>
        {orderCancelled ? <OrderCancelledNotice /> : null}
        {orderId ? (
          <p className="mt-2 text-sm text-on-surface-variant">
            Mã đơn: <span className="font-mono font-medium text-on-surface">{orderId}</span>
          </p>
        ) : null}
        <OrderStatusLines orderStatus={orderStatus} orderPaymentStatus={orderPaymentStatus} />
        <PaymentResultLinks orderId={orderId} />
      </>
    );
  }

  if (status === PAYMENT_STATUS.FAILED) {
    return (
      <>
        <span
          className="material-symbols-outlined mb-4 text-5xl text-error"
          style={{ fontVariationSettings: "'FILL' 1" }}
          aria-hidden="true"
        >
          error
        </span>
        <h1 className="text-headline-md font-bold text-on-surface">Thanh toán không thành công</h1>
        <p className="mt-2 text-sm text-on-surface-variant">
          Giao dịch thất bại hoặc không được cổng thanh toán chấp nhận.
        </p>
        {orderCancelled ? <OrderCancelledNotice /> : null}
        {orderId ? (
          <p className="mt-2 text-sm text-on-surface-variant">
            Mã đơn: <span className="font-mono font-medium text-on-surface">{orderId}</span>
          </p>
        ) : null}
        {amount != null ? (
          <p className="mt-4 text-headline-sm font-semibold text-primary">{formatVndPrice(amount)}</p>
        ) : null}
        <OrderStatusLines orderStatus={orderStatus} orderPaymentStatus={orderPaymentStatus} />
        {showRetry ? (
          <button
            type="button"
            onClick={onRetryPayment}
            disabled={isRetrying}
            className="mt-6 rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-60"
          >
            {isRetrying ? "Đang tạo liên kết..." : "Tạo liên kết thanh toán mới"}
          </button>
        ) : null}
        <PaymentResultLinks orderId={orderId} />
      </>
    );
  }

  return (
    <>
      <p className="text-sm text-on-surface-variant">
        Trạng thái thanh toán: {formatPaymentStatusLabel(status) || "Không xác định"}
      </p>
      <PaymentResultLinks orderId={orderId} />
    </>
  );
}
