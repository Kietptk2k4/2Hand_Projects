import { useCallback, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { ReviewAuthorLink } from "../components/ReviewAuthorLink";
import { CommerceShell } from "../components/CommerceShell";
import { OrderDetailShippingAddress } from "../components/OrderDetailShippingAddress";
import { SellerOrderItemStatusBadge } from "../components/SellerOrderItemStatusBadge";
import { SellerOrderProcessConfirmDialog } from "../components/SellerOrderProcessConfirmDialog";
import { SellerOrderShipmentCell } from "../components/SellerOrderShipmentCell";
import {
  buildProcessSuccessToast,
  formatOrderPaymentSubline,
  PAYMENT_METHOD_LABELS,
} from "../constants/sellerOrderConstants";
import { useProcessSellerOrderItems } from "../hooks/useProcessSellerOrderItems";
import { useSellerOrderDetail } from "../hooks/useSellerOrderDetail";
import { formatOrderDate, formatShortOrderId } from "../utils/formatOrderDate";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { buildCommerceProductDetailPath } from "../utils/commerceRoutes";

export function CommerceSellerOrderDetailPage() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");
  const [confirmItems, setConfirmItems] = useState(null);

  const { detail, isLoading, isNotFound, isError, errorMessage, reload } = useSellerOrderDetail(orderId);

  const handleProcessSuccess = useCallback(
    (result) => {
      setConfirmItems(null);
      reload();
      setToastMessage(
        buildProcessSuccessToast({
          newlyProcessedCount: result.newlyProcessedCount,
          alreadyProcessingCount: result.alreadyProcessingCount,
        }),
      );
    },
    [reload],
  );

  const { isProcessing, processError, process, clearError } = useProcessSellerOrderItems({
    onSuccess: handleProcessSuccess,
  });

  const openConfirmForItems = useCallback(
    (items) => {
      const pending = items.filter((item) => item.itemStatus === "PENDING");
      if (pending.length === 0) return;
      clearError();
      setConfirmItems(pending);
    },
    [clearError],
  );

  const handleConfirmProcess = useCallback(async () => {
    if (!confirmItems?.length) return;
    const ids = confirmItems.map((item) => item.orderItemId);
    await process(ids);
  }, [confirmItems, process]);

  const handleCancelConfirm = useCallback(() => {
    if (isProcessing) return;
    setConfirmItems(null);
    clearError();
  }, [clearError, isProcessing]);

  const shortId = detail ? formatShortOrderId(detail.orderId) : orderId;
  const paymentSubline = detail
    ? formatOrderPaymentSubline(detail.orderPaymentStatus, detail.orderPaymentMethod)
    : "";

  return (
    <CommerceShell>
      <div className="mx-auto w-full max-w-[960px]">
        <div className="mb-6">
          <Link
            to={APP_ROUTES.commerceSellerOrders}
            className="mb-3 inline-flex items-center gap-1 text-label-md text-primary hover:underline"
          >
            <span className="material-symbols-outlined text-[18px]">arrow_back</span>
            Quản lý đơn hàng
          </Link>
        </div>

        {isLoading ? (
          <div className="space-y-4">
            <div className="h-24 animate-pulse rounded-xl bg-surface-container-low" />
            <div className="h-48 animate-pulse rounded-xl bg-surface-container-low" />
          </div>
        ) : null}

        {isNotFound || isError ? (
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center">
            <p className="text-body-md text-on-surface-variant">
              {errorMessage || "Không tải được chi tiết đơn hàng."}
            </p>
            <div className="mt-4 flex flex-wrap justify-center gap-3">
              <button
                type="button"
                onClick={reload}
                className="rounded-lg bg-primary px-4 py-2 text-label-md text-on-primary"
              >
                Thử lại
              </button>
              <Link
                to={APP_ROUTES.commerceSellerOrders}
                className="rounded-lg border border-primary px-4 py-2 text-label-md text-primary"
              >
                Về danh sách
              </Link>
            </div>
          </div>
        ) : null}

        {!isLoading && detail ? (
          <>
            <header className="mb-6 rounded-xl border border-outline-variant bg-surface-container-lowest p-5 shadow-sm">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                  <p className="text-label-sm text-on-surface-variant">Mã đơn</p>
                  <h1 className="font-mono text-headline-md font-bold text-on-surface">{shortId}</h1>
                  <p className="mt-1 text-body-sm text-on-surface-variant">
                    Đặt lúc {formatOrderDate(detail.orderCreatedAt)}
                  </p>
                  <div className="mt-4">
                    <p className="mb-2 text-label-sm text-on-surface-variant">Người mua</p>
                    <ReviewAuthorLink
                      buyerId={detail.buyerId}
                      displayName={detail.buyerDisplayName}
                      avatarUrl={detail.buyerAvatarUrl}
                    />
                  </div>
                </div>
                <div className="text-right text-body-sm text-on-surface-variant">
                  <p>{paymentSubline}</p>
                  <p className="mt-1">
                    {PAYMENT_METHOD_LABELS[detail.orderPaymentMethod] || detail.orderPaymentMethod}
                  </p>
                </div>
              </div>

              <div className="mt-4 grid gap-3 border-t border-outline-variant pt-4 sm:grid-cols-3">
                <div>
                  <p className="text-label-sm text-on-surface-variant">Tạm tính (shop)</p>
                  <p className="text-headline-sm font-semibold text-on-surface">
                    {formatVndPrice(detail.sellerItemsSubtotal)}
                  </p>
                </div>
                <div>
                  <p className="text-label-sm text-on-surface-variant">Phí ship phân bổ</p>
                  <p className="text-headline-sm font-semibold text-on-surface">
                    {formatVndPrice(detail.sellerShippingTotal)}
                  </p>
                </div>
                <div>
                  <p className="text-label-sm text-on-surface-variant">Tổng phần shop</p>
                  <p className="text-headline-sm font-semibold text-primary">
                    {formatVndPrice(
                      Number(detail.sellerItemsSubtotal || 0) + Number(detail.sellerShippingTotal || 0),
                    )}
                  </p>
                </div>
              </div>
            </header>

            {detail.shippingAddress ? (
              <div className="mb-6">
                <OrderDetailShippingAddress address={detail.shippingAddress} />
              </div>
            ) : null}

            <section className="rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
              <div className="border-b border-outline-variant px-5 py-4">
                <h2 className="text-headline-sm font-semibold text-on-surface">
                  Sản phẩm của shop ({detail.items.length})
                </h2>
              </div>

              <div className="divide-y divide-outline-variant/60">
                {detail.items.map((item) => {
                  const canPrepare = item.itemStatus === "PENDING";
                  const canCreateShipment =
                    item.itemStatus === "PROCESSING" &&
                    !item.shipmentSummary?.shipmentId &&
                    detail.orderStatus === "PROCESSING" &&
                    (detail.orderPaymentMethod === "COD" || detail.orderPaymentStatus === "PAID");
                  const createShipmentHref = canCreateShipment
                    ? `${APP_ROUTES.commerceSellerShipments}?create=1&orderId=${encodeURIComponent(item.orderId)}&orderItemIds=${encodeURIComponent(item.orderItemId)}`
                    : null;
                  const shipmentDetailPath = item.shipmentSummary?.shipmentId
                    ? APP_ROUTES.commerceSellerShipmentDetail.replace(
                        ":shipmentId",
                        item.shipmentSummary.shipmentId,
                      )
                    : null;

                  const productDetailPath = item.productId
                    ? buildCommerceProductDetailPath(item.productId)
                    : null;

                  return (
                    <article key={item.orderItemId} className="flex flex-col gap-4 p-5 sm:flex-row sm:items-start">
                      <div className="flex min-w-0 flex-1 items-start gap-3">
                        {productDetailPath ? (
                          <Link
                            to={productDetailPath}
                            className="h-14 w-14 shrink-0 overflow-hidden rounded-lg border border-outline-variant hover:opacity-90"
                          >
                            {item.imageSnapshot ? (
                              <img
                                src={item.imageSnapshot}
                                alt=""
                                className="h-full w-full object-cover"
                              />
                            ) : (
                              <div className="flex h-full w-full items-center justify-center bg-surface-container-high">
                                <span className="material-symbols-outlined text-on-surface-variant">image</span>
                              </div>
                            )}
                          </Link>
                        ) : item.imageSnapshot ? (
                          <img
                            src={item.imageSnapshot}
                            alt=""
                            className="h-14 w-14 shrink-0 rounded-lg border border-outline-variant object-cover"
                          />
                        ) : (
                          <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-lg bg-surface-container-high">
                            <span className="material-symbols-outlined text-on-surface-variant">image</span>
                          </div>
                        )}
                        <div className="min-w-0 flex-1">
                          {productDetailPath ? (
                            <Link
                              to={productDetailPath}
                              className="text-body-md font-medium text-on-surface hover:text-primary hover:underline"
                            >
                              {item.productNameSnapshot}
                            </Link>
                          ) : (
                            <p className="text-body-md font-medium text-on-surface">{item.productNameSnapshot}</p>
                          )}
                          <p className="mt-1 text-body-sm text-on-surface-variant">
                            SL: {item.quantity} · {formatVndPrice(item.finalPrice)}
                          </p>
                          <div className="mt-2">
                            <SellerOrderItemStatusBadge status={item.itemStatus} />
                          </div>
                        </div>
                      </div>

                      <div className="flex shrink-0 flex-col items-start gap-2 sm:items-end">
                        <SellerOrderShipmentCell
                          shipmentSummary={item.shipmentSummary}
                          detailHref={shipmentDetailPath}
                        />
                        {canPrepare ? (
                          <button
                            type="button"
                            disabled={isProcessing}
                            onClick={() => openConfirmForItems([item])}
                            className="rounded-lg border border-primary px-3 py-1.5 text-label-sm font-medium text-primary hover:bg-surface-container-low disabled:opacity-50"
                          >
                            Chuẩn bị hàng
                          </button>
                        ) : canCreateShipment ? (
                          <button
                            type="button"
                            onClick={() => navigate(createShipmentHref)}
                            className="rounded-lg border border-secondary px-3 py-1.5 text-label-sm font-medium text-secondary hover:bg-secondary/5"
                          >
                            Tạo vận đơn
                          </button>
                        ) : shipmentDetailPath ? (
                          <button
                            type="button"
                            onClick={() => navigate(shipmentDetailPath)}
                            className="text-label-sm font-medium text-primary hover:underline"
                          >
                            Xem vận đơn
                          </button>
                        ) : null}
                      </div>
                    </article>
                  );
                })}
              </div>
            </section>
          </>
        ) : null}
      </div>

      <SellerOrderProcessConfirmDialog
        open={Boolean(confirmItems?.length)}
        items={confirmItems || []}
        isProcessing={isProcessing}
        errorMessage={processError}
        onCancel={handleCancelConfirm}
        onConfirm={handleConfirmProcess}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </CommerceShell>
  );
}
