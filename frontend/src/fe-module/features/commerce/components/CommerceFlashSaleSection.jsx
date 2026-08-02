import { useEffect, useMemo, useState } from "react";
import { formatVndPrice } from "../../social/utils/formatPrice";

const FALLBACK_FLASH_IMAGES = [
  "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1552346154-21d32810aba3?w=500&auto=format&fit=crop&q=80",
];

function calculateTimeLeft(targetMs) {
  const diffMs = Math.max(0, targetMs - Date.now());
  const totalSeconds = Math.floor(diffMs / 1000);
  return {
    hours: Math.floor(totalSeconds / 3600),
    minutes: Math.floor((totalSeconds % 3600) / 60),
    seconds: totalSeconds % 60,
  };
}

function formatDigit(num) {
  return String(num).padStart(2, "0");
}

function getCountdownTarget(product, slotEnd) {
  const promoEndMs = product.promotionEndAt ? new Date(product.promotionEndAt).getTime() : null;
  const slotEndMs = slotEnd ? new Date(slotEnd).getTime() : null;

  if (promoEndMs && slotEndMs) {
    return Math.min(promoEndMs, slotEndMs);
  }
  return promoEndMs || slotEndMs || Date.now();
}

export function CommerceFlashSaleSection({
  products = [],
  isLoading = false,
  slotEnd = null,
  onOpenProduct,
  onViewAll,
}) {
  const [failedImages, setFailedImages] = useState({});
  const slotEndMs = useMemo(() => (slotEnd ? new Date(slotEnd).getTime() : null), [slotEnd]);
  const [timeLeft, setTimeLeft] = useState(() =>
    calculateTimeLeft(slotEndMs || Date.now() + 3 * 60 * 60 * 1000),
  );

  useEffect(() => {
    const target = slotEndMs || Date.now() + 3 * 60 * 60 * 1000;
    setTimeLeft(calculateTimeLeft(target));
    const timer = setInterval(() => {
      setTimeLeft(calculateTimeLeft(target));
    }, 1000);
    return () => clearInterval(timer);
  }, [slotEndMs]);

  if (!isLoading && products.length === 0) {
    return null;
  }

  return (
    <section className="mb-10 overflow-hidden rounded-2xl border border-rose-200 bg-gradient-to-r from-rose-50 via-red-50 to-orange-50 p-5 shadow-xs">
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

      {isLoading ? (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="h-56 animate-pulse rounded-xl bg-rose-100/60" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {products.map((product, idx) => {
            const listPrice = Number(product.price || 0);
            const salePrice = Number(product.salePrice ?? product.effectivePrice ?? 0);
            const savingsAmount = Math.max(0, listPrice - salePrice);
            const discountPercent =
              listPrice > 0 && savingsAmount > 0
                ? Math.round((savingsAmount / listPrice) * 100)
                : 0;
            const countdownTarget = getCountdownTarget(product, slotEnd);
            const remainingSeconds = Math.max(0, Math.floor((countdownTarget - Date.now()) / 1000));
            const remainingMinutes = Math.max(1, Math.ceil(remainingSeconds / 60));

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
                  {discountPercent > 0 ? (
                    <div className="absolute right-2 top-2 rounded-lg bg-red-600 px-2 py-0.5 text-xs font-black text-white shadow-xs">
                      -{discountPercent}%
                    </div>
                  ) : null}
                  <div className="absolute bottom-2 left-2 rounded-md bg-slate-900/80 px-2 py-0.5 text-[10px] font-bold text-white backdrop-blur-xs">
                    Còn {remainingMinutes} phút
                  </div>
                </div>

                <div className="p-3">
                  <h3 className="text-xs font-semibold text-slate-800 line-clamp-1 group-hover:text-red-600">
                    {product.title}
                  </h3>

                  <div className="mt-2 flex items-baseline justify-between gap-1">
                    <span className="text-sm font-black text-red-600 sm:text-base">
                      {formatVndPrice(salePrice)}
                    </span>
                    {listPrice > salePrice ? (
                      <span className="text-[11px] text-slate-400 line-through">
                        {formatVndPrice(listPrice)}
                      </span>
                    ) : null}
                  </div>

                  {savingsAmount > 0 ? (
                    <div className="mt-3 flex items-center justify-center gap-1 rounded-full bg-gradient-to-r from-red-600 to-orange-500 py-1 px-2 text-[11px] font-bold text-white shadow-xs transition-all group-hover:from-red-700 group-hover:to-orange-600">
                      <span className="material-symbols-outlined text-xs">savings</span>
                      <span>Tiết kiệm {formatVndPrice(savingsAmount)}</span>
                    </div>
                  ) : null}
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}
