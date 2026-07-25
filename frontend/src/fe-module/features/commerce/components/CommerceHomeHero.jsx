import { useState, useEffect } from "react";

const HERO_BANNERS = [
  {
    id: 1,
    title: "SẮM ĐỒ 2HAND CHẤT",
    subtitle: "Giảm tới 50% cho hàng tuyển chọn tháng này",
    badge: "🔥 DEAL HOT TỰ CHỌN",
    bgGradient: "from-blue-600 via-indigo-600 to-purple-700",
    buttonText: "Săn Deal ngay",
    tag: "Chính Hãng & Đã Kiểm Định",
  },
  {
    id: 2,
    title: "VINTAGE & STREETWEAR",
    subtitle: "Độc lạ, cá tính từ các Shop xác minh trên 2Hands",
    badge: "⚡ NEW ARRIVALS",
    bgGradient: "from-amber-600 via-orange-600 to-rose-600",
    buttonText: "Khám phá BST",
    tag: "Hàng Độc Bản 1-0-2",
  },
  {
    id: 3,
    title: "MUA BÁN AN TOÀN 100%",
    subtitle: "Giao hàng tận nơi qua GHN & GHTK • Kiểm hàng trước khi thanh toán",
    badge: "🛡️ ĐẢM BẢO BỞI 2HANDS",
    bgGradient: "from-emerald-600 via-teal-600 to-cyan-700",
    buttonText: "Xem chính sách",
    tag: "Đổi trả 7 ngày",
  },
];

const QUICK_SHORTCUTS = [
  { id: "flash-sale", label: "Flash Sale", icon: "bolt", color: "bg-amber-500 text-white" },
  { id: "2hand-select", label: "2Hand Select", icon: "verified", color: "bg-blue-600 text-white" },
  { id: "freeship", label: "Freeship Xtra", icon: "local_shipping", color: "bg-emerald-500 text-white" },
  { id: "guarantee", label: "Mua An Toàn", icon: "verified_user", color: "bg-teal-600 text-white" },
  { id: "fashion-men", label: "Đồ Nam", icon: "apparel", color: "bg-indigo-500 text-white" },
  { id: "fashion-women", label: "Đồ Nữ", icon: "woman", color: "bg-pink-500 text-white" },
  { id: "shoes", label: "Giày Sneaker", icon: "steps", color: "bg-purple-500 text-white" },
  { id: "tech", label: "Đồ Điện Tử", icon: "devices", color: "bg-cyan-600 text-white" },
];

export function CommerceHomeHero({
  onSearchSubmit,
  onCategoryClick,
  onCreateShopClick,
  onExploreShippingClick,
  onBannerCtaClick,
  onShortcutClick,
  navItems = [],
  isLoadingNav = false,
}) {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [query, setQuery] = useState("");

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % HERO_BANNERS.length);
    }, 5000);
    return () => clearInterval(timer);
  }, []);

  const handleSearchSubmit = (event) => {
    event.preventDefault();
    if (query.trim()) {
      onSearchSubmit?.(query);
    }
  };

  const activeBanner = HERO_BANNERS[currentSlide];

  return (
    <section className="mb-8 space-y-6">
      {/* Top Banner Grid */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        {/* Main Banner Carousel (2 Columns on Large Screens) */}
        <div className="relative overflow-hidden rounded-2xl bg-surface-container-high shadow-md lg:col-span-2">
          <div
            className={`relative flex min-h-[220px] flex-col justify-between bg-gradient-to-r ${activeBanner.bgGradient} p-6 text-white transition-all duration-700 sm:p-8`}
          >
            {/* Background Pattern / Gloss */}
            <div className="pointer-events-none absolute -right-10 -top-10 h-64 w-64 rounded-full bg-white/10 blur-2xl" />
            <div className="pointer-events-none absolute -bottom-10 -left-10 h-48 w-48 rounded-full bg-black/10 blur-xl" />

            <div>
              <div className="flex items-center gap-2">
                <span className="rounded-full bg-white/20 px-3 py-1 text-xs font-bold backdrop-blur-md">
                  {activeBanner.badge}
                </span>
                <span className="text-xs font-medium text-white/80">{activeBanner.tag}</span>
              </div>
              <h2 className="mt-3 text-2xl font-black tracking-tight sm:text-3xl lg:text-4xl">
                {activeBanner.title}
              </h2>
              <p className="mt-2 max-w-lg text-sm text-white/90 sm:text-base">
                {activeBanner.subtitle}
              </p>
            </div>

            <div className="mt-6 flex items-center justify-between">
              <button
                type="button"
                onClick={() => onBannerCtaClick?.(activeBanner.id)}
                className="inline-flex items-center gap-2 rounded-xl bg-white px-5 py-2.5 text-sm font-bold text-gray-900 shadow-md transition-transform hover:scale-105 active:scale-95 cursor-pointer"
              >
                <span>{activeBanner.buttonText}</span>
                <span className="material-symbols-outlined text-base">arrow_forward</span>
              </button>

              {/* Slide Indicators */}
              <div className="flex items-center gap-1.5">
                {HERO_BANNERS.map((banner, index) => (
                  <button
                    key={banner.id}
                    type="button"
                    onClick={() => setCurrentSlide(index)}
                    className={`h-2 rounded-full transition-all ${
                      index === currentSlide ? "w-6 bg-white" : "w-2 bg-white/40"
                    }`}
                    aria-label={`Chuyển banner ${index + 1}`}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Right Side Promo Cards */}
        <div className="flex flex-col gap-4">
          {/* Functional Create Shop Card */}
          <div
            onClick={onCreateShopClick}
            className="group relative flex-1 cursor-pointer overflow-hidden rounded-2xl bg-gradient-to-br from-slate-900 to-blue-950 p-5 text-white shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md active:scale-[0.99]"
          >
            <div className="relative z-10 flex h-full flex-col justify-between">
              <div>
                <span className="inline-block rounded-md bg-amber-500/20 px-2.5 py-0.5 text-xs font-semibold text-amber-400">
                  Mở Shop Miễn Phí
                </span>
                <h3 className="mt-2 text-lg font-bold text-white">Bạn có đồ cũ muốn bán?</h3>
                <p className="mt-1 text-xs text-slate-300">
                  Đăng sản phẩm trong 1 phút & tiếp cận hàng nghìn người mua.
                </p>
              </div>
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  onCreateShopClick?.();
                }}
                className="mt-4 flex items-center gap-1 text-left text-xs font-bold text-amber-400 transition-colors group-hover:underline cursor-pointer"
              >
                <span>Tạo cửa hàng ngay</span>
                <span className="material-symbols-outlined text-sm">chevron_right</span>
              </button>
            </div>
            <span
              className="material-symbols-outlined absolute -bottom-2 -right-2 text-6xl text-white/5"
              aria-hidden="true"
            >
              storefront
            </span>
          </div>

          {/* Functional Shipping Exploration Card */}
          <div
            onClick={onExploreShippingClick}
            className="group relative flex-1 cursor-pointer overflow-hidden rounded-2xl bg-gradient-to-br from-emerald-600 to-teal-700 p-5 text-white shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md active:scale-[0.99]"
          >
            <div className="relative z-10 flex h-full flex-col justify-between">
              <div>
                <span className="inline-block rounded-md bg-white/20 px-2.5 py-0.5 text-xs font-semibold text-white">
                  Vận Chuyển Toàn Quốc
                </span>
                <h3 className="mt-2 text-lg font-bold text-white">GIAO HÀNG GHN & GHTK</h3>
                <p className="mt-1 text-xs text-white/90">Theo dõi hành trình đơn hàng trực tiếp 24/7.</p>
              </div>
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  onExploreShippingClick?.();
                }}
                className="mt-4 flex items-center gap-1 text-left text-xs font-bold text-white transition-colors group-hover:underline cursor-pointer"
              >
                <span>Khám phá ngay</span>
                <span className="material-symbols-outlined text-sm">local_shipping</span>
              </button>
            </div>
            <span
              className="material-symbols-outlined absolute -bottom-2 -right-2 text-6xl text-white/10"
              aria-hidden="true"
            >
              local_shipping
            </span>
          </div>
        </div>
      </div>

      {/* Quick Search & Filter Bar */}
      <div className="rounded-2xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm">
        <form onSubmit={handleSearchSubmit} className="flex flex-col gap-3 sm:flex-row">
          <div className="relative flex-1">
            <span
              className="material-symbols-outlined absolute left-3.5 top-1/2 -translate-y-1/2 text-outline"
              aria-hidden="true"
            >
              search
            </span>
            <input
              type="search"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Tìm theo tên sản phẩm, thương hiệu, quần kaki, áo khoác 2hand..."
              className="w-full rounded-xl border border-outline-variant bg-surface py-2.5 pl-11 pr-4 text-sm text-on-surface outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
            />
          </div>
          <button
            type="submit"
            className="flex items-center justify-center gap-2 rounded-xl bg-primary px-6 py-2.5 text-sm font-bold text-on-primary shadow-sm transition hover:bg-[#0050cb] cursor-pointer"
          >
            <span className="material-symbols-outlined text-lg">search</span>
            <span>Tìm kiếm</span>
          </button>
        </form>

        {/* Dynamic Category Chips */}
        <div className="mt-3 flex items-center gap-2 overflow-x-auto pb-1 text-xs text-on-surface-variant no-scrollbar">
          <span className="font-semibold text-on-surface shrink-0">Từ khóa hot:</span>
          {isLoadingNav ? (
            <span className="text-outline">Đang tải...</span>
          ) : (
            (navItems.length > 0
              ? navItems
              : [
                  { categoryId: "1", label: "Quần kaki 2hand" },
                  { categoryId: "2", label: "Áo khoác Vintage" },
                  { categoryId: "3", label: "Sneaker Nike/Adidas" },
                  { categoryId: "4", label: "Đồng hồ cũ" },
                  { categoryId: "5", label: "Túi xách da" },
                ]
            ).map((item) => (
              <button
                key={item.categoryId || item.label}
                type="button"
                onClick={() => onCategoryClick?.(item)}
                className="shrink-0 rounded-lg border border-outline-variant bg-surface px-2.5 py-1 transition-colors hover:border-primary hover:text-primary cursor-pointer"
              >
                {item.label}
              </button>
            ))
          )}
        </div>
      </div>

      {/* Quick Category Shortcuts Grid */}
      <div className="grid grid-cols-4 gap-3 sm:grid-cols-4 md:grid-cols-8">
        {QUICK_SHORTCUTS.map((item) => (
          <button
            key={item.id}
            type="button"
            onClick={() => onShortcutClick?.(item.id)}
            className="group flex flex-col items-center justify-center rounded-xl border border-outline-variant/60 bg-surface-container-lowest p-3 text-center transition-all hover:-translate-y-0.5 hover:border-primary/40 hover:shadow-sm cursor-pointer"
          >
            <div
              className={`flex h-11 w-11 items-center justify-center rounded-2xl ${item.color} shadow-sm transition-transform group-hover:scale-110`}
            >
              <span className="material-symbols-outlined text-xl">{item.icon}</span>
            </div>
            <span className="mt-2 text-xs font-semibold text-on-surface line-clamp-1 group-hover:text-primary">
              {item.label}
            </span>
          </button>
        ))}
      </div>
    </section>
  );
}
