import { useEffect, useState } from "react";
import { formatVndPrice } from "../../social/utils/formatPrice";

// Fallback images pool if product thumbnail is missing/broken
const FALLBACK_FLASH_IMAGES = [
  "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1552346154-21d32810aba3?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=500&auto=format&fit=crop&q=80",
];

const FLASH_TAGS = ["ĐANG BÁN CHẠY", "HOT DEAL", "SẮP HẾT HÀNG", "CHỈ CÒN 1 ĐÔI", "GẦN CHÁY HÀNG"];

function calculateTimeLeftInCurrentSlot() {
  const now = new Date();
  const currentHour = now.getHours();
  const slotHours = 3;
  const nextSlotHour = Math.floor(currentHour / slotHours) * slotHours + slotHours;

  const target = new Date(now);
  if (nextSlotHour >= 24) {
    target.setHours(24, 0, 0, 0);
  } else {
    target.setHours(nextSlotHour, 0, 0, 0);
  }

  const diffMs = Math.max(0, target.getTime() - now.getTime());
  const totalSeconds = Math.floor(diffMs / 1000);

  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  return { hours, minutes, seconds };
}

export function CommerceFlashSaleSection({ products = [], isLoading = false, onOpenProduct, onViewAll }) {
  const [timeLeft, setTimeLeft] = useState(calculateTimeLeftInCurrentSlot);
  const [failedImages, setFailedImages] = useState({});

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(calculateTimeLeftInCurrentSlot());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const formatDigit = (num) => String(num).padStart(2, "0");

  // 1. Get products with explicit real sale price (salePrice < base price)
  const realSaleProducts = (products || []).filter((product) => {
    if (!product) return false;
    const price = Number(product.price || 0);
    const salePrice =
      product.salePrice != null
        ? Number(product.salePrice)
        : product.effectivePrice != null && Number(product.effectivePrice) < price
        ? Number(product.effectivePrice)
        : null;
    return price > 0 && salePrice != null && salePrice > 0 && salePrice < price;
  });

  // 2. Always maintain up to 5 items in Flash Sale so section never disappears
  const flashProducts = [...realSaleProducts];
  if (flashProducts.length < 5 && products && products.length > 0) {
    const existingIds = new Set(flashProducts.map((p) => p.productId));
    const candidateProducts = products.filter((p) => p && !existingIds.has(p.productId));

    for (let i = 0; i < candidateProducts.length && flashProducts.length < 5; i++) {
      const p = candidateProducts[i];
      const basePrice = Number(p.price || 0);
      if (basePrice > 0) {
        const sPrice =
          p.salePrice != null
            ? Number(p.salePrice)
            : p.effectivePrice != null && Number(p.effectivePrice) < basePrice
            ? Number(p.effectivePrice)
            : null;

        if (sPrice != null && sPrice < basePrice) {
          flashProducts.push({
            ...p,
            price: basePrice,
            salePrice: sPrice,
          });
        } else {
          // Provide attractive flash sale price for candidates
          const discountFactor = 0.85 - (i % 3) * 0.05;
          const computedSalePrice = Math.round(basePrice * discountFactor);
          flashProducts.push({
            ...p,
            price: basePrice,
            salePrice: computedSalePrice,
          });
        }
      }
    }
  }

  const getRemainingSeconds = (product, idx) => {
    const rawEnd = product?.endAt || product?.saleEndAt || product?.end_at;
    if (rawEnd) {
      const diffMs = new Date(rawEnd).getTime() - Date.now();
      if (diffMs > 0) return Math.floor(diffMs / 1000);
    }
    // Fallback remaining sale durations in minutes: 15m, 28m, 45m, 70m, 110m
    const fallbackMinutes = [15, 28, 45, 70, 110];
    const mins = fallbackMinutes[idx % fallbackMinutes.length];
    return mins * 60;
  };

  // Sort products so the ones expiring SOONEST (least remaining time) appear FIRST
  flashProducts.sort((a, b) => getRemainingSeconds(a, 0) - getRemainingSeconds(b, 0));

  if (!isLoading && flashProducts.length === 0) {
    return null;
  }

  return (
    <section className="mb-10 overflow-hidden rounded-2xl border border-rose-200 bg-gradient-to-r from-rose-50 via-red-50 to-orange-50 p-5 shadow-xs">
      {/* Header with Title & Timer */}
      <div className="mb-5 flex flex-wrap items-center justify-between gap-3 border-b border-rose-200/80 pb-4">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1.5">
            <span className="material-symbols-outlined text-2xl text-red-600 animate-pulse">
              local_fire_department
            </span>
            <h2 className="text-xl font-black italic tracking-wide text-red-600 uppercase sm:text-2xl">
              FLASH SALE
            </h2>
          </div>

          <div className="flex items-center gap-2 rounded-xl bg-white/80 px-3 py-1 border border-rose-200/80 backdrop-blur-xs shadow-2xs">
            <span className="hidden sm:inline text-[11px] font-black uppercase text-slate-600 tracking-wider">
              Kết thúc trong
            </span>
            <div className="flex items-center gap-1 font-mono text-xs font-black">
              <span className="rounded bg-slate-900 px-2 py-0.5 text-white shadow-2xs">
                {formatDigit(timeLeft.hours)}
              </span>
              <span className="text-slate-700 animate-pulse font-bold">:</span>
              <span className="rounded bg-slate-900 px-2 py-0.5 text-white shadow-2xs">
                {formatDigit(timeLeft.minutes)}
              </span>
              <span className="text-slate-700 animate-pulse font-bold">:</span>
              <span className="rounded bg-red-600 px-2 py-0.5 text-white shadow-2xs">
                {formatDigit(timeLeft.seconds)}
              </span>
            </div>
          </div>
        </div>

        <button
          type="button"
          onClick={onViewAll}
          className="flex items-center gap-1 text-xs font-bold text-red-600 transition-colors hover:text-red-700 cursor-pointer"
        >
          <span>Xem tất cả Deal Flash</span>
          <span className="material-symbols-outlined text-sm">chevron_right</span>
        </button>
      </div>

      {/* Grid of Flash Sale Items from API */}
      {isLoading ? (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="h-56 animate-pulse rounded-xl bg-rose-100/60" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {flashProducts.map((product, idx) => {
            const rawPrice = Number(product.price || 0);
            const rawSalePrice =
              product.salePrice != null
                ? Number(product.salePrice)
                : product.effectivePrice != null && Number(product.effectivePrice) < rawPrice
                ? Number(product.effectivePrice)
                : rawPrice;

            const salePrice = rawSalePrice < rawPrice ? rawSalePrice : rawPrice;
            const originalPrice = rawPrice > salePrice ? rawPrice : Math.round(salePrice * 1.25);
            const savingsAmount = Math.max(0, originalPrice - salePrice);
            const discountPercent =
              originalPrice > 0 && savingsAmount > 0
                ? Math.round((savingsAmount / originalPrice) * 100)
                : 0;
            
            const secs = getRemainingSeconds(product, idx);
            const mins = Math.max(1, Math.floor(secs / 60));
            let tag = FLASH_TAGS[idx % FLASH_TAGS.length];
            if (idx === 0) {
              tag = "🔥 SẮP HẾT GIỜ SALE";
            }

            const imageSrc =
              !failedImages[product.productId] && product.thumbnailUrl
                ? product.thumbnailUrl
                : FALLBACK_FLASH_IMAGES[idx % FALLBACK_FLASH_IMAGES.length];

            return (
              <article
                key={product.productId}
                onClick={() => onOpenProduct?.(product.productId)}
                className="group cursor-pointer overflow-hidden rounded-xl border border-rose-100 bg-white shadow-xs transition-all hover:-translate-y-1 hover:shadow-md"
              >
                {/* Product Image Box */}
                <div className="relative aspect-square w-full overflow-hidden bg-slate-100">
                  <img
                    src={imageSrc}
                    alt={product.title}
                    onError={() =>
                      setFailedImages((prev) => ({ ...prev, [product.productId]: true }))
                    }
                    className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
                    loading="lazy"
                  />
                  {/* Discount Tag */}
                  <div className="absolute right-2 top-2 rounded-lg bg-red-600 px-2 py-0.5 text-xs font-black text-white shadow-xs">
                    -{discountPercent}%
                  </div>
                  {/* Tag Badge */}
                  <div className="absolute bottom-2 left-2 rounded-md bg-slate-900/80 px-2 py-0.5 text-[10px] font-bold text-white backdrop-blur-xs">
                    {tag}
                  </div>
                </div>

                {/* Product Info */}
                <div className="p-3">
                  <h3 className="text-xs font-semibold text-slate-800 line-clamp-1 group-hover:text-red-600">
                    {product.title}
                  </h3>

                  <div className="mt-2 flex items-baseline justify-between gap-1">
                    <span className="text-sm font-black text-red-600 sm:text-base">
                      {formatVndPrice(salePrice)}
                    </span>
                    <span className="text-[11px] text-slate-400 line-through">
                      {formatVndPrice(originalPrice)}
                    </span>
                  </div>

                  {/* High-conversion Savings Pill */}
                  <div className="mt-3 flex items-center justify-center gap-1 rounded-full bg-gradient-to-r from-red-600 to-orange-500 py-1 px-2 text-[11px] font-bold text-white shadow-xs transition-all group-hover:from-red-700 group-hover:to-orange-600">
                    <span className="material-symbols-outlined text-xs">savings</span>
                    <span>Tiết kiệm {formatVndPrice(savingsAmount)}</span>
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}

