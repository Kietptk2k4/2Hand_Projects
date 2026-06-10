import { formatVndPrice } from "../../social/utils/formatPrice";

const BUCKETS = [
  {
    id: "in_transit",
    key: "inTransit",
    title: "Đang vận chuyển",
    description: "Đơn đang xử lý hoặc đang giao",
    icon: "local_shipping",
    accent: "border-primary/30 bg-primary-container/20",
  },
  {
    id: "pending_confirm",
    key: "pendingConfirm",
    title: "Chờ xác nhận",
    description: "Đã giao, chờ khách xác nhận nhận hàng",
    icon: "inventory",
    accent: "border-tertiary/30 bg-tertiary-container/20",
  },
  {
    id: "recognized",
    key: "recognized",
    title: "Đã ghi nhận",
    description: "Hoàn tất đơn và đã thu COD",
    icon: "paid",
    accent: "border-secondary/30 bg-secondary-container/20",
  },
];

export function SellerRevenueBucketCards({ summary, isLoading }) {
  if (isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-3">
        {[0, 1, 2].map((index) => (
          <div
            key={index}
            className="h-36 animate-pulse rounded-xl border border-outline-variant bg-surface-container-low"
          />
        ))}
      </div>
    );
  }

  return (
    <div className="grid gap-4 md:grid-cols-3">
      {BUCKETS.map((bucket) => {
        const data = summary?.[bucket.key] ?? { amount: 0, itemCount: 0 };
        return (
          <article
            key={bucket.id}
            className={[
              "rounded-xl border p-5 shadow-sm",
              bucket.accent,
            ].join(" ")}
          >
            <div className="mb-3 flex items-start justify-between gap-3">
              <div>
                <h3 className="text-title-md font-semibold text-on-surface">{bucket.title}</h3>
                <p className="mt-1 text-body-sm text-on-surface-variant">{bucket.description}</p>
              </div>
              <span
                className="material-symbols-outlined text-2xl text-on-surface-variant"
                aria-hidden="true"
              >
                {bucket.icon}
              </span>
            </div>
            <p className="text-headline-sm font-bold text-on-surface">{formatVndPrice(data.amount)}</p>
            <p className="mt-1 text-body-sm text-on-surface-variant">
              {data.itemCount} dòng đơn
            </p>
          </article>
        );
      })}
    </div>
  );
}