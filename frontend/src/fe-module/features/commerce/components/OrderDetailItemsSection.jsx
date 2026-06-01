import { Link, useNavigate } from "react-router-dom";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { ITEM_STATUS_BADGE_CLASS, ITEM_STATUS_LABELS } from "../constants/orderDetailConstants";
import { APP_ROUTES } from "../../../shared/constants/routes";

function parseAttributes(attributesSnapshot) {
  if (!attributesSnapshot) return null;
  try {
    const parsed = JSON.parse(attributesSnapshot);
    if (!parsed || typeof parsed !== "object") return null;
    return Object.entries(parsed)
      .map(([key, value]) => `${key}: ${value}`)
      .join(" · ");
  } catch {
    return attributesSnapshot;
  }
}

export function OrderDetailItemsSection({ orderId, items }) {
  const navigate = useNavigate();

  if (!items?.length) return null;

  const handleWriteReview = (item) => {
    const path = `${APP_ROUTES.commerceReviewCreate}?orderItemId=${encodeURIComponent(item.orderItemId)}`;
    navigate(path, {
      state: {
        orderId,
        orderItem: item,
        productId: item.productId,
      },
    });
  };

  return (
    <section className="overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
      <div className="flex items-center justify-between border-b border-outline-variant bg-surface-bright px-4 py-3 md:px-6">
        <h2 className="text-headline-sm font-semibold text-on-surface">Sản phẩm đã đặt</h2>
        <span className="text-body-sm text-on-surface-variant">{items.length} sản phẩm</span>
      </div>

      <div className="flex flex-col gap-4 p-4 md:p-6">
        {items.map((item) => {
          const statusLabel = ITEM_STATUS_LABELS[item.status] || item.status;
          const statusClass =
            ITEM_STATUS_BADGE_CLASS[item.status] ||
            "bg-surface-container-high text-on-surface-variant";
          const attributesText = parseAttributes(item.attributesSnapshot);
          const canReview = item.status === "COMPLETED";
          const hasReview = Boolean(item.reviewId);

          return (
            <div
              key={item.orderItemId}
              className="flex flex-col gap-3 border-b border-outline-variant pb-4 last:border-0 last:pb-0 sm:flex-row sm:items-start"
            >
              <div className="h-24 w-24 shrink-0 overflow-hidden rounded-lg border border-outline-variant bg-surface-container-high">
                {item.imageSnapshot ? (
                  <img
                    src={item.imageSnapshot}
                    alt=""
                    className="h-full w-full object-cover"
                    loading="lazy"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center">
                    <span className="material-symbols-outlined text-2xl text-outline" aria-hidden="true">
                      inventory_2
                    </span>
                  </div>
                )}
              </div>

              <div className="min-w-0 flex-1">
                <h3 className="text-body-md font-medium text-on-surface">
                  {item.productNameSnapshot}
                </h3>
                {item.shopNameSnapshot ? (
                  <p className="mt-0.5 text-body-sm text-on-surface-variant">
                    {item.shopNameSnapshot}
                  </p>
                ) : null}
                {attributesText ? (
                  <p className="mt-1 text-body-sm text-on-surface-variant">{attributesText}</p>
                ) : null}
                <div className="mt-2 flex flex-wrap items-center gap-3 text-label-md text-on-surface">
                  <span>Số lượng: {item.quantity}</span>
                  <span className="font-semibold">{formatVndPrice(item.finalPrice)}</span>
                </div>

                {canReview && !hasReview ? (
                  <button
                    type="button"
                    onClick={() => handleWriteReview(item)}
                    className="mt-3 rounded-lg border border-primary px-4 py-2 text-label-md font-medium text-primary hover:bg-surface-container-low"
                  >
                    Viết đánh giá
                  </button>
                ) : null}

                {canReview && hasReview ? (
                  <Link
                    to={APP_ROUTES.commerceReviewEdit.replace(":reviewId", item.reviewId)}
                    className="mt-3 inline-block rounded-lg border border-primary px-4 py-2 text-label-md font-medium text-primary hover:bg-surface-container-low"
                  >
                    Sửa đánh giá
                  </Link>
                ) : null}
              </div>

              <span className={`self-start rounded-full px-2.5 py-0.5 text-label-sm ${statusClass}`}>
                {statusLabel}
              </span>
            </div>
          );
        })}
      </div>
    </section>
  );
}
