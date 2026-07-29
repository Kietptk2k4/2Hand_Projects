import { formatVndPrice } from "../../social/utils/formatPrice";

const BUCKETS = [
  {
    id: "in_transit",
    key: "inTransit",
    title: "ĐANG VẬN CHUYỂN",
    description: "Đơn đang xử lý hoặc đang giao",
    icon: "local_shipping",
    badgeColor: "bg-blue-50 text-blue-600 border-blue-200",
  },
  {
    id: "pending_confirm",
    key: "pendingConfirm",
    title: "CHỜ XÁC NHẬN",
    description: "Đã giao, chờ khách xác nhận nhận hàng",
    icon: "inventory_2",
    badgeColor: "bg-amber-50 text-amber-600 border-amber-200",
  },
  {
    id: "recognized",
    key: "recognized",
    title: "ĐÃ GHI NHẬN",
    description: "Hoàn tất đơn và đã thu COD",
    icon: "paid",
    badgeColor: "bg-emerald-50 text-emerald-600 border-emerald-200",
  },
];

function BucketSkeleton() {
  return (
    <div className="animate-pulse rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-6 shadow-xs">
      <div className="h-3 w-24 rounded bg-surface-container-high" />
      <div className="mt-4 h-8 w-32 rounded bg-surface-container-high" />
      <div className="mt-2 h-3 w-full rounded bg-surface-container-high" />
    </div>
  );
}

export function SellerRevenueBucketCards({ summary, isLoading }) {
  if (isLoading) {
    return (
      <div className="grid max-w-full min-w-0 grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {BUCKETS.map((bucket) => (
          <BucketSkeleton key={bucket.id} />
        ))}
      </div>
    );
  }

  return (
    <div className="grid max-w-full min-w-0 grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {BUCKETS.map((bucket) => {
        const data = summary?.[bucket.key] ?? { amount: 0, itemCount: 0 };

        return (
          <div
            key={bucket.id}
            className="flex flex-col justify-between rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md"
          >
            <div>
              <div className="flex items-center justify-between gap-2">
                <span className="text-[11px] font-bold uppercase tracking-wider text-on-surface-variant">
                  {bucket.title}
                </span>
                <div className={`flex h-10 w-10 items-center justify-center rounded-xl border ${bucket.badgeColor}`}>
                  <span className="material-symbols-outlined text-lg" aria-hidden="true">
                    {bucket.icon}
                  </span>
                </div>
              </div>
              <p className="mt-2 text-2xl font-black text-on-surface">
                {formatVndPrice(data.amount)}
              </p>
              <p className="mt-1 text-xs text-on-surface-variant">{bucket.description}</p>
            </div>

            <div className="mt-4 border-t border-outline-variant/40 pt-3">
              <span className="inline-flex items-center gap-1 rounded-md bg-surface-container-high px-2.5 py-1 text-xs font-bold text-on-surface">
                {data.itemCount} dòng đơn
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
}
