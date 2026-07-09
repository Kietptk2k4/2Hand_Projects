import { formatVndPrice } from "../../social/utils/formatPrice";
import { AdminMetricCard, AdminSurfaceCard } from "../../auth/admin/components/ui";

const BUCKETS = [
  {
    id: "in_transit",
    key: "inTransit",
    title: "Đang vận chuyển",
    description: "Đơn đang xử lý hoặc đang giao",
    icon: "local_shipping",
    accentClass: "text-admin-accent",
    surfaceClass: "bg-admin-accent-soft/60",
  },
  {
    id: "pending_confirm",
    key: "pendingConfirm",
    title: "Chờ xác nhận",
    description: "Đã giao, chờ khách xác nhận nhận hàng",
    icon: "inventory",
    accentClass: "text-admin-warning",
    surfaceClass: "bg-admin-warning-soft/70",
  },
  {
    id: "recognized",
    key: "recognized",
    title: "Đã ghi nhận",
    description: "Hoàn tất đơn và đã thu COD",
    icon: "paid",
    accentClass: "text-admin-success",
    surfaceClass: "bg-admin-success-soft/70",
  },
];

function BucketSkeleton() {
  return (
    <AdminSurfaceCard padding="md" className="animate-pulse">
      <div className="h-3 w-24 rounded bg-admin-surface-muted" />
      <div className="mt-4 h-8 w-32 rounded bg-admin-surface-muted" />
      <div className="mt-2 h-3 w-full rounded bg-admin-surface-muted" />
    </AdminSurfaceCard>
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
          <AdminMetricCard
            key={bucket.id}
            label={bucket.title}
            value={formatVndPrice(data.amount)}
            hint={bucket.description}
            footer={
              <div className="flex items-center justify-between gap-3">
                <p className="text-sm text-admin-text-secondary">{data.itemCount} dòng đơn</p>
                <span
                  className={[
                    "material-symbols-outlined flex h-9 w-9 items-center justify-center rounded-lg text-xl",
                    bucket.accentClass,
                    bucket.surfaceClass,
                  ].join(" ")}
                  aria-hidden="true"
                >
                  {bucket.icon}
                </span>
              </div>
            }
          />
        );
      })}
    </div>
  );
}
