import { formatVndPrice } from "../../social/utils/formatPrice";
import {
  ORDER_PAYMENT_STATUS_LABELS,
  PAYMENT_METHOD_LABELS,
  PAYMENT_STATUS_BADGE_CLASS,
} from "../constants/orderListConstants";

export function OrderDetailPaymentSummary({ order, shipments }) {
  if (!order) return null;

  const shippingFee =
    shipments?.reduce((sum, shipment) => sum + (shipment.shippingFee || 0), 0) ??
    Math.max(0, (order.finalAmount || 0) - (order.totalAmount || 0));

  const paymentStatusLabel =
    ORDER_PAYMENT_STATUS_LABELS[order.orderPaymentStatus] || order.orderPaymentStatus;
  const paymentStatusClass =
    PAYMENT_STATUS_BADGE_CLASS[order.orderPaymentStatus] ||
    "bg-surface-container-high text-on-surface-variant";
  const paymentMethodLabel =
    PAYMENT_METHOD_LABELS[order.paymentMethod] || order.paymentMethod;

  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm md:p-6">
      <h2 className="mb-4 text-headline-sm font-semibold text-on-surface">Tóm tắt thanh toán</h2>

      <dl className="space-y-2 text-body-sm">
        <div className="flex justify-between text-on-surface-variant">
          <dt>Tạm tính</dt>
          <dd className="text-on-surface">{formatVndPrice(order.totalAmount)}</dd>
        </div>
        <div className="flex justify-between text-on-surface-variant">
          <dt>Phí vận chuyển</dt>
          <dd className="text-on-surface">{formatVndPrice(shippingFee)}</dd>
        </div>
        <div className="flex justify-between border-t border-outline-variant pt-3 text-label-md font-semibold text-on-surface">
          <dt>Tổng cộng</dt>
          <dd className="text-headline-sm text-primary">{formatVndPrice(order.finalAmount)}</dd>
        </div>
      </dl>

      <div className="mt-4 flex flex-wrap items-center gap-2">
        <span className={`rounded-full px-2.5 py-0.5 text-label-sm ${paymentStatusClass}`}>
          {paymentStatusLabel}
        </span>
        {paymentMethodLabel ? (
          <span className="text-body-sm text-on-surface-variant">{paymentMethodLabel}</span>
        ) : null}
      </div>

      {order.payment?.paidAt ? (
        <p className="mt-3 text-body-sm text-on-surface-variant">
          Thanh toán lúc:{" "}
          <span className="text-on-surface">
            {new Date(order.payment.paidAt).toLocaleString("vi-VN")}
          </span>
        </p>
      ) : null}
    </section>
  );
}
