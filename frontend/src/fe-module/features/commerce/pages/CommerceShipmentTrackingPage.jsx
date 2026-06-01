import { useCallback } from "react";
import { Link, useParams } from "react-router-dom";
import { CommerceShell } from "../components/CommerceShell";
import { OrderDetailShippingAddress } from "../components/OrderDetailShippingAddress";
import { ShipmentTrackingHero } from "../components/ShipmentTrackingHero";
import { ShipmentTrackingItemsSection } from "../components/ShipmentTrackingItemsSection";
import { ShipmentTrackingSkeleton } from "../components/ShipmentTrackingSkeleton";
import { ShipmentTrackingSummary } from "../components/ShipmentTrackingSummary";
import { ShipmentTrackingTimeline } from "../components/ShipmentTrackingTimeline";
import { useShipmentTrackingPage } from "../hooks/useShipmentTrackingPage";
import { formatShortOrderId } from "../utils/formatOrderDate";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CommerceShipmentTrackingPage() {
  const { orderId, shipmentId } = useParams();

  const {
    detail,
    tracking,
    timelineEvents,
    shipmentDelivered,
    orderCompleted,
    isLoading,
    isRefreshing,
    errorMessage,
    isNotFound,
    isError,
    refresh,
    retry,
  } = useShipmentTrackingPage(orderId, shipmentId);

  const orderDetailPath = APP_ROUTES.commerceOrderDetail.replace(":orderId", orderId || "");
  const carrier = detail?.carrier || tracking?.carrier;
  const subtitleParts = [carrier, orderId ? formatShortOrderId(orderId) : null].filter(Boolean);

  const handleRefresh = useCallback(() => {
    refresh();
  }, [refresh]);

  const showDisclaimer = shipmentDelivered && !orderCompleted;

  return (
    <CommerceShell>
      <div className="mx-auto w-full max-w-[1280px]">
        {isLoading ? <ShipmentTrackingSkeleton /> : null}

        {isNotFound ? (
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center">
            <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
              local_shipping
            </span>
            <p className="text-sm text-on-surface-variant">
              {errorMessage || "Không tìm thấy thông tin vận chuyển."}
            </p>
            {orderId ? (
              <Link
                to={orderDetailPath}
                className="mt-4 inline-block rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
              >
                Quay lại chi tiết đơn
              </Link>
            ) : null}
          </div>
        ) : null}

        {!isLoading && isError && !isNotFound ? (
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

        {!isLoading && !isNotFound && !isError && detail ? (
          <>
            <header className="mb-6">
              <nav className="mb-3 flex flex-wrap items-center gap-1 text-label-sm text-on-surface-variant">
                <Link to={APP_ROUTES.commerceOrders} className="hover:text-primary">
                  Đơn hàng
                </Link>
                <span aria-hidden="true">/</span>
                <Link to={orderDetailPath} className="hover:text-primary">
                  Chi tiết đơn
                </Link>
                <span aria-hidden="true">/</span>
                <span className="text-on-surface">Theo dõi vận chuyển</span>
              </nav>

              <Link
                to={orderDetailPath}
                className="mb-3 inline-flex items-center text-label-md text-on-surface-variant transition-colors hover:text-primary"
              >
                <span className="material-symbols-outlined mr-1 text-[20px]" aria-hidden="true">
                  arrow_back
                </span>
                Quay lại chi tiết đơn
              </Link>

              <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                <div>
                  <h1 className="text-headline-md font-bold text-on-surface">Theo dõi vận chuyển</h1>
                  {subtitleParts.length ? (
                    <p className="mt-1 text-body-sm text-on-surface-variant">
                      {subtitleParts.join(" · ")}
                    </p>
                  ) : null}
                </div>

                <div className="flex flex-wrap gap-2">
                  <button
                    type="button"
                    onClick={handleRefresh}
                    disabled={isRefreshing}
                    className="rounded-lg border border-outline-variant px-4 py-2 text-label-md text-on-surface hover:bg-surface-container-low disabled:opacity-60"
                  >
                    {isRefreshing ? "Đang làm mới..." : "Làm mới"}
                  </button>
                  <Link
                    to={orderDetailPath}
                    className="rounded-lg border border-primary px-4 py-2 text-label-md font-medium text-primary hover:bg-surface-container-low"
                  >
                    Xem chi tiết đơn hàng
                  </Link>
                </div>
              </div>
            </header>

            {showDisclaimer ? (
              <div
                className="mb-6 rounded-lg border border-primary/30 bg-primary/5 px-4 py-3 text-body-sm text-on-surface"
                role="status"
              >
                Hàng đã được giao. Vui lòng xác nhận đã nhận hàng tại trang chi tiết đơn.
              </div>
            ) : null}

            <div className="mb-6">
              <ShipmentTrackingHero detail={detail} tracking={tracking} />
            </div>

            <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-12">
              <div className="flex flex-col gap-6 lg:col-span-8">
                <ShipmentTrackingTimeline
                  events={timelineEvents}
                  isLoading={isLoading && !timelineEvents.length}
                />
                <ShipmentTrackingItemsSection items={detail.orderItems} />
              </div>

              <aside className="flex flex-col gap-6 lg:col-span-4 lg:sticky lg:top-20">
                <OrderDetailShippingAddress address={detail.shippingAddress} />
                <ShipmentTrackingSummary detail={detail} />
              </aside>
            </div>
          </>
        ) : null}
      </div>
    </CommerceShell>
  );
}
