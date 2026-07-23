import { AdminSurfaceCard } from "../../components/ui";

const KPI_META = [
  {
    key: "total",
    label: "Tổng thương hiệu",
    hint: "Trong catalog",
    icon: "storefront",
    accentClass: "text-admin-text-secondary",
    surfaceClass: "bg-admin-surface-muted",
  },
  {
    key: "active",
    label: "Đang hoạt động",
    hint: "Hiển thị trên storefront",
    icon: "check_circle",
    accentClass: "text-[#5b7c6a]",
    surfaceClass: "bg-[#5b7c6a]/10",
  },
  {
    key: "inactive",
    label: "Đã vô hiệu",
    hint: "Ẩn khỏi người mua",
    icon: "block",
    accentClass: "text-[#b45353]",
    surfaceClass: "bg-[#b45353]/10",
  },
  {
    key: "products",
    label: "Sản phẩm gán",
    hint: "Tổng product_count",
    icon: "inventory_2",
    accentClass: "text-[#8b7355]",
    surfaceClass: "bg-[#8b7355]/10",
  },
];

function KpiCard({ meta, value, isLoading, isActive, onClick }) {
  if (isLoading) {
    return (
      <AdminSurfaceCard padding="md" className="min-w-0 animate-pulse">
        <div className="h-3 w-24 rounded bg-admin-surface-muted" />
        <div className="mt-4 h-8 w-20 rounded bg-admin-surface-muted" />
      </AdminSurfaceCard>
    );
  }

  return (
    <AdminSurfaceCard
      padding="md"
      className={[
        "min-w-0 transition-colors",
        onClick ? "hover:border-admin-accent-border hover:bg-admin-surface-muted/40" : "",
        isActive ? "ring-2 ring-admin-accent-soft" : "",
      ].join(" ")}
    >
      <button
        type="button"
        className="w-full text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
        onClick={onClick}
        disabled={!onClick}
        aria-pressed={isActive}
      >
        <div className="flex items-start justify-between gap-2">
          <div>
            <p className="text-[11px] font-medium uppercase tracking-[0.08em] text-admin-text-muted">
              {meta.label}
            </p>
            <p className="mt-1 text-xs text-admin-text-secondary">{meta.hint}</p>
          </div>
          <span
            className={[
              "material-symbols-outlined flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-xl",
              meta.accentClass,
              meta.surfaceClass,
            ].join(" ")}
            aria-hidden="true"
          >
            {meta.icon}
          </span>
        </div>
        <p className="mt-4 text-2xl font-semibold tabular-nums tracking-tight text-admin-text sm:text-[1.65rem]">
          {value}
        </p>
      </button>
    </AdminSurfaceCard>
  );
}

export function BrandHeroStrip({ metrics, activeStatusFilter, isLoading, onStatusClick }) {
  const values = {
    total: metrics?.total ?? 0,
    active: metrics?.activeCount ?? 0,
    inactive: metrics?.inactiveCount ?? 0,
    products: metrics?.totalProducts ?? 0,
  };

  const activeKeys = {
    "": "total",
    active: "active",
    inactive: "inactive",
  };

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
      {KPI_META.map((meta) => {
        const clickable = meta.key !== "products";
        const isActive = activeKeys[activeStatusFilter] === meta.key;

        return (
          <KpiCard
            key={meta.key}
            meta={meta}
            value={values[meta.key]}
            isLoading={isLoading}
            isActive={isActive}
            onClick={clickable ? () => onStatusClick?.(meta.key) : undefined}
          />
        );
      })}
    </div>
  );
}
