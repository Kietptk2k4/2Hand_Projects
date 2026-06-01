import { formatVndPrice } from "../../social/utils/formatPrice";
import { formatOrderDate } from "../utils/formatOrderDate";

export function ProductReviewSummaryAside({ imageUrl, productName, shopName, price, completedAt }) {
  return (
    <aside className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm md:p-6 lg:sticky lg:top-20">
      <h2 className="mb-4 text-headline-sm font-semibold text-on-surface">Sản phẩm đã mua</h2>

      <div className="mb-4 aspect-[4/3] overflow-hidden rounded-lg border border-outline-variant bg-surface-container-high">
        {imageUrl ? (
          <img src={imageUrl} alt="" className="h-full w-full object-cover" loading="lazy" />
        ) : (
          <div className="flex h-full min-h-[160px] items-center justify-center">
            <span className="material-symbols-outlined text-4xl text-outline" aria-hidden="true">
              inventory_2
            </span>
          </div>
        )}
      </div>

      <h3 className="text-headline-sm font-semibold text-on-surface">{productName}</h3>
      {shopName ? (
        <p className="mt-1 text-body-sm text-on-surface-variant">{shopName}</p>
      ) : null}

      <dl className="mt-4 space-y-2 border-t border-outline-variant pt-4 text-body-sm">
        {price != null ? (
          <div className="flex justify-between gap-2">
            <dt className="text-on-surface-variant">Giá đã mua</dt>
            <dd className="font-semibold text-on-surface">{formatVndPrice(price)}</dd>
          </div>
        ) : null}
        {completedAt ? (
          <div className="flex items-center gap-1 text-on-surface-variant">
            <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
              calendar_today
            </span>
            <span>Hoàn thành {formatOrderDate(completedAt)}</span>
          </div>
        ) : null}
      </dl>
    </aside>
  );
}
