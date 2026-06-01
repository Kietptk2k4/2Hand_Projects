const SIDEBAR_LINKS = [
  { icon: "home", label: "Trang chủ", active: true },
  { icon: "storefront", label: "Marketplace" },
  { icon: "receipt_long", label: "Đơn hàng" },
  { icon: "analytics", label: "Thống kê" },
  { icon: "settings", label: "Cài đặt" },
];

export function CommerceHomeSidebar({ onComingSoon }) {
  return (
    <aside className="hidden w-64 shrink-0 border-r border-outline-variant px-3 py-6 lg:flex lg:flex-col">
      <div className="mb-8 px-3">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center overflow-hidden rounded-lg bg-surface-container-high">
            <span className="material-symbols-outlined text-primary" aria-hidden="true">
              business_center
            </span>
          </div>
          <div>
            <h2 className="text-headline-sm font-bold text-primary">2Hands Commerce</h2>
            <p className="text-label-sm text-on-surface-variant">Khám phá sản phẩm</p>
          </div>
        </div>
      </div>

      <nav className="flex flex-1 flex-col gap-1">
        {SIDEBAR_LINKS.map((link) => (
          <button
            key={link.label}
            type="button"
            onClick={() => {
              if (!link.active) onComingSoon?.();
            }}
            className={[
              "flex items-center gap-3 rounded-lg px-4 py-3 text-left text-label-md transition-colors",
              link.active
                ? "bg-primary text-on-primary"
                : "text-on-surface-variant hover:bg-surface-container-high",
            ].join(" ")}
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              {link.icon}
            </span>
            {link.label}
          </button>
        ))}
      </nav>
    </aside>
  );
}
