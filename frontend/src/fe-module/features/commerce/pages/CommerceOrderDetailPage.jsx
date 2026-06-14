import { useCallback, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { CancelOrderConfirmDialog } from "../components/CancelOrderConfirmDialog";
import { ConfirmOrderReceivedDialog } from "../components/ConfirmOrderReceivedDialog";
import { OrderDetailItemsSection } from "../components/OrderDetailItemsSection";
import { OrderDetailPaymentSummary } from "../components/OrderDetailPaymentSummary";
import { OrderDetailShipmentsSection } from "../components/OrderDetailShipmentsSection";
import { OrderDetailShippingAddress } from "../components/OrderDetailShippingAddress";
import { OrderDetailSkeleton } from "../components/OrderDetailSkeleton";
import { OrderDetailTimeline } from "../components/OrderDetailTimeline";
import {
  buildCancelOrderSuccessToast,
  buildConfirmOrderReceivedSuccessToast,
} from "../constants/orderActionConstants";
import {
  ORDER_STATUS_BADGE_CLASS,
  ORDER_STATUS_LABELS,
} from "../constants/orderListConstants";
import { useCancelOrder } from "../hooks/useCancelOrder";
import { useConfirmOrderReceived } from "../hooks/useConfirmOrderReceived";
import { useOrderDetail } from "../hooks/useOrderDetail";
import { useOrderTrackStatus } from "../hooks/useOrderTrackStatus";
import { isOrderCancelledDueToPayment } from "../constants/paymentStatusLabels";
import { canCancelOrder, canConfirmOrderReceived, isPendingRefund } from "../utils/orderActionDisplay";
import { formatOrderDate, formatShortOrderId } from "../utils/formatOrderDate";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { ONLINE_PAYMENT_METHODS } from "../constants/checkoutConstants";
import { RetryVnpayPaymentButton } from "../components/RetryVnpayPaymentButton";

export function CommerceOrderDetailPage() {
  const { orderId } = useParams();
  const [toastMessage, setToastMessage] = useState("");
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);

  const { order, isLoading, isNotFound, isError, errorMessage, retry } = useOrderDetail(orderId);

  const {
    timelineEvents,
    isLoading: isTrackLoading,
    isRefreshing,
    errorMessage: trackErrorMessage,
    refresh: refreshTrack,
  } = useOrderTrackStatus(orderId, {
    enabled: Boolean(order) && !isNotFound,
    pollWhileProcessing: true,
  });

  const handleActionSuccess = useCallback(() => {
    retry();
    refreshTrack();
  }, [retry, refreshTrack]);

  const handleCancelSuccess = useCallback(
    (result) => {
      setShowCancelDialog(false);
      setToastMessage(buildCancelOrderSuccessToast(result));
      handleActionSuccess();
    },
    [handleActionSuccess],
  );

  const handleConfirmSuccess = useCallback(
    (result) => {
      setShowConfirmDialog(false);
      setToastMessage(buildConfirmOrderReceivedSuccessToast(result));
      handleActionSuccess();
    },
    [handleActionSuccess],
  );

  const { isCancelling, errorMessage: cancelError, cancel, clearError: clearCancelError } =
    useCancelOrder({ onSuccess: handleCancelSuccess });

  const {
    isConfirming,
    errorMessage: confirmError,
    confirm,
    clearError: clearConfirmError,
  } = useConfirmOrderReceived({ onSuccess: handleConfirmSuccess });

  const showPayNow =
    order?.orderStatus === "AWAITING_PAYMENT" &&
    ONLINE_PAYMENT_METHODS.has(order?.paymentMethod) &&
    order?.payment?.paymentId &&
    !isOrderCancelledDueToPayment(order.orderStatus, order.orderPaymentStatus);
  const isVnpayPayNow = showPayNow && order?.paymentMethod === "VNPAY";

  const showPaymentFailureBanner = isOrderCancelledDueToPayment(
    order?.orderStatus,
    order?.orderPaymentStatus
  );

  const showCancel = canCancelOrder(order);
  const showPendingRefund = isPendingRefund(order);
  const showConfirm = canConfirmOrderReceived(order);
  const isMutating = isCancelling || isConfirming;

  const shippingAddress = order?.shipments?.[0]?.shippingAddress;

  const statusLabel = order ? ORDER_STATUS_LABELS[order.orderStatus] || order.orderStatus : "";
  const statusClass =
    ORDER_STATUS_BADGE_CLASS[order?.orderStatus] ||
    "bg-surface-container-high text-on-surface-variant";

  const handleRefresh = useCallback(() => {
    retry();
    refreshTrack();
  }, [retry, refreshTrack]);

  const handleCloseCancelDialog = useCallback(() => {
    if (isCancelling) return;
    setShowCancelDialog(false);
    clearCancelError();
  }, [clearCancelError, isCancelling]);

  const handleCloseConfirmDialog = useCallback(() => {
    if (isConfirming) return;
    setShowConfirmDialog(false);
    clearConfirmError();
  }, [clearConfirmError, isConfirming]);

  const handleSubmitCancel = useCallback(
    async ({ reason }) => {
      if (!order?.orderId) return;
      await cancel(order.orderId, { reason });
    },
    [cancel, order?.orderId],
  );

  const handleSubmitConfirm = useCallback(async () => {
    if (!order?.orderId) return;
    await confirm(order.orderId);
  }, [confirm, order?.orderId]);

  const orderLabel = order ? formatShortOrderId(order.orderId) : "";

  return (
    <CommerceShell>
      <div className="mx-auto w-full max-w-[1280px]">
        {isLoading ? <OrderDetailSkeleton /> : null}

        {isNotFound ? (
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center">
            <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
              receipt_long
            </span>
            <p className="text-sm text-on-surface-variant">
              {errorMessage || "Không tìm thấy đơn hàng."}
            </p>
            <Link
              to={APP_ROUTES.commerceOrders}
              className="mt-4 inline-block rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Quay lại danh sách đơn
            </Link>
          </div>
        ) : null}

        {!isLoading && isError ? (
          <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
            <p className="text-sm text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Thử lại
            </button>
          </div>
        ) : null}

        {!isLoading && !isNotFound && !isError && order ? (
          <>
            <header className="mb-6">
              <Link
                to={APP_ROUTES.commerceOrders}
                className="mb-3 inline-flex items-center text-label-md text-on-surface-variant transition-colors hover:text-primary"
              >
                <span className="material-symbols-outlined mr-1 text-[20px]" aria-hidden="true">
                  arrow_back
                </span>
                Quay lại đơn hàng
              </Link>

              <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                <div>
                  <div className="flex flex-wrap items-center gap-3">
                    <h1 className="text-headline-md font-bold text-on-surface">Chi tiết đơn hàng</h1>
                    <span className={`rounded-full px-2.5 py-0.5 text-label-sm ${statusClass}`}>
                      {statusLabel}
                    </span>
                  </div>
                  <p className="mt-1 text-body-sm text-on-surface-variant">
                    {formatShortOrderId(order.orderId)} · Đặt lúc {formatOrderDate(order.createdAt)}
                  </p>
                </div>

                <div className="flex flex-wrap gap-2">
                  <button
                    type="button"
                    onClick={handleRefresh}
                    disabled={isTrackLoading || isRefreshing || isMutating}
                    className="rounded-lg border border-outline-variant px-4 py-2 text-label-md text-on-surface hover:bg-surface-container-low disabled:opacity-60"
                  >
                    {isRefreshing ? "Đang làm mới..." : "Làm mới"}
                  </button>
                  {showCancel ? (
                    <button
                      type="button"
                      onClick={() => setShowCancelDialog(true)}
                      disabled={isMutating}
                      className="rounded-lg border border-error px-4 py-2 text-label-md font-medium text-error hover:bg-error-container/30 disabled:opacity-60"
                    >
                      Hủy đơn
                    </button>
                  ) : null}
                  {showConfirm ? (
                    <button
                      type="button"
                      onClick={() => setShowConfirmDialog(true)}
                      disabled={isMutating}
                      className="rounded-lg border border-primary px-4 py-2 text-label-md font-medium text-primary hover:bg-primary/5 disabled:opacity-60"
                    >
                      Đã nhận hàng
                    </button>
                  ) : null}
                  {showPayNow && isVnpayPayNow ? (
                    <RetryVnpayPaymentButton orderId={order.orderId} label="Thanh toán ngay" />
                  ) : null}
                  {showPayNow && !isVnpayPayNow ? (
                    <Link
                      to={`${APP_ROUTES.commerceCheckoutPaymentResult}?paymentId=${order.payment.paymentId}`}
                      className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary hover:bg-[#0050cb]"
                    >
                      Thanh toán ngay
                    </Link>
                  ) : null}
                </div>
              </div>

              {trackErrorMessage ? (
                <p className="mt-2 text-body-sm text-error">{trackErrorMessage}</p>
              ) : null}

              {showPendingRefund ? (
                <div className="mt-4 rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-body-sm text-amber-950">
                  <p>Đơn hàng đang chờ hoàn tiền. Admin sẽ xác nhận sau khi hoàn tiền qua VNPay.</p>
                  {order.activeRefundRequest?.reason ? (
                    <p className="mt-2">
                      <span className="font-medium">Lý do hủy:</span> {order.activeRefundRequest.reason}
                    </p>
                  ) : null}
                </div>
              ) : null}

              {!showPendingRefund && order.cancellationNote ? (
                <div className="mt-4 rounded-lg border border-outline-variant bg-surface-container-low px-4 py-3 text-body-sm text-on-surface">
                  <p className="font-medium text-on-surface">Lý do hủy đơn</p>
                  <p className="mt-1 text-on-surface-variant">{order.cancellationNote}</p>
                </div>
              ) : null}

              {showPaymentFailureBanner ? (
                <p className="mt-4 rounded-lg border border-error/20 bg-error-container/15 px-4 py-3 text-body-sm text-on-surface">
                  Đơn đã hủy do thanh toán không hoàn tất. Sản phẩm đã được trả lại kho — bạn có thể
                  đặt lại nếu còn hàng.
                </p>
              ) : null}
            </header>

            <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-12">
              <div className="flex flex-col gap-6 lg:col-span-8">
                <OrderDetailItemsSection
                  orderId={order.orderId}
                  items={order.items}
                  cancellationNote={order.cancellationNote}
                />
                <OrderDetailShipmentsSection orderId={order.orderId} shipments={order.shipments} />
              </div>

              <aside className="flex flex-col gap-6 lg:col-span-4 lg:sticky lg:top-20">
                <OrderDetailTimeline
                  events={timelineEvents}
                  isLoading={isTrackLoading && !timelineEvents.length}
                />
                <OrderDetailPaymentSummary order={order} shipments={order.shipments} />
                <OrderDetailShippingAddress address={shippingAddress} />
              </aside>
            </div>
          </>
        ) : null}
      </div>

      <CancelOrderConfirmDialog
        open={showCancelDialog}
        orderLabel={orderLabel}
        isSubmitting={isCancelling}
        submitError={cancelError}
        onClose={handleCloseCancelDialog}
        onConfirm={handleSubmitCancel}
      />

      <ConfirmOrderReceivedDialog
        open={showConfirmDialog}
        isSubmitting={isConfirming}
        submitError={confirmError}
        onClose={handleCloseConfirmDialog}
        onConfirm={handleSubmitConfirm}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </CommerceShell>
  );
}
