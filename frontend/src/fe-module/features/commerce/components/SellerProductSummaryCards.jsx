export function SellerProductSummaryCards({ summary, onSelectStatus }) {
  if (!summary) return null;

  const cards = [
    {
      label: "Tổng sản phẩm",
      value: summary.total,
      icon: "inventory_2",
      badgeColor: "bg-blue-50 text-blue-600 border-blue-200",
      status: null,
    },
    {
      label: "Đang bán",
      value: summary.active,
      icon: "storefront",
      badgeColor: "bg-emerald-50 text-emerald-600 border-emerald-200",
      status: "ACTIVE",
    },
    {
      label: "Hết hàng",
      value: summary.outOfStock,
      icon: "error_outline",
      badgeColor: "bg-rose-50 text-rose-600 border-rose-200",
      status: "OUT_OF_STOCK",
    },
    {
      label: "Sắp hết hàng",
      value: summary.lowStock,
      icon: "warning",
      badgeColor: "bg-amber-50 text-amber-600 border-amber-200",
      status: "ACTIVE",
    },
  ];

  return (
    <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {cards.map((card) => (
        <button
          key={card.label}
          type="button"
          onClick={() => onSelectStatus?.(card.status)}
          className="flex w-full items-center justify-between rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md cursor-pointer text-left"
        >
          <div>
            <p className="text-xs font-bold text-on-surface-variant">{card.label}</p>
            <p className="mt-2 text-2xl font-black text-on-surface">{card.value}</p>
          </div>
          <div className={`flex h-12 w-12 items-center justify-center rounded-2xl border ${card.badgeColor}`}>
            <span className="material-symbols-outlined text-xl" aria-hidden="true">
              {card.icon}
            </span>
          </div>
        </button>
      ))}
    </div>
  );
}
