export function SellerProductSummaryCards({ summary }) {
  if (!summary) return null;

  const cards = [
    { label: "Tổng sản phẩm", value: summary.total, icon: "inventory_2" },
    { label: "Đang bán", value: summary.active, icon: "storefront" },
    { label: "Hết hàng", value: summary.outOfStock, icon: "error_outline" },
    { label: "Sắp hết hàng", value: summary.lowStock, icon: "warning" },
  ];

  return (
    <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {cards.map((card) => (
        <div
          key={card.label}
          className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm"
        >
          <div className="flex items-center justify-between gap-2">
            <p className="text-body-sm text-on-surface-variant">{card.label}</p>
            <span className="material-symbols-outlined text-primary" aria-hidden="true">
              {card.icon}
            </span>
          </div>
          <p className="mt-2 text-headline-md font-bold text-on-surface">{card.value}</p>
        </div>
      ))}
    </div>
  );
}
