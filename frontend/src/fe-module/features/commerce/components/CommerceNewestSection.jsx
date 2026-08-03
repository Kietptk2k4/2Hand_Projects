import { formatVndPrice } from "../../social/utils/formatPrice";

const FALLBACK_IMAGES = [
  "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80",
];

export function CommerceNewestSection({
  products = [],
  isLoading = false,
  onOpenProduct,
  onViewAll,
}) {
  if (!isLoading && products.length === 0) {
    return null;
  }

  return (
    <section id="newest-section" className="mb-10">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="flex items-center gap-2 text-lg font-bold text-on-surface sm:text-xl">
            <span className="material-symbols-outlined text-primary">new_releases</span>
            HÀNG MỚI VỀ
          </h2>
          <p className="text-xs text-on-surface-variant">Sản phẩm vừa lên kệ từ shop uy tín</p>
        </div>
        <button
          type="button"
          onClick={onViewAll}
          className="flex items-center gap-1 text-xs font-bold text-primary transition-colors hover:text-[#0050cb] cursor-pointer"
        >
          <span>Xem tất cả</span>
          <span className="material-symbols-outlined text-sm">chevron_right</span>
        </button>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="h-56 animate-pulse rounded-xl bg-surface-container-high" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {products.map((product, idx) => {
            const priceVal = Number(product.effectivePrice ?? product.salePrice ?? product.price ?? 0);
            const imageSrc = product.thumbnailUrl || FALLBACK_IMAGES[idx % FALLBACK_IMAGES.length];

            return (
              <article
                key={product.productId}
                onClick={() => onOpenProduct?.(product.productId)}
                className="group cursor-pointer overflow-hidden rounded-xl border border-outline-variant/70 bg-surface-container-lowest shadow-xs transition-all hover:-translate-y-1 hover:border-primary/40 hover:shadow-md"
              >
                <div className="relative aspect-square w-full overflow-hidden bg-surface-container-low">
                  <img
                    src={imageSrc}
                    alt={product.title}
                    className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
                    loading="lazy"
                  />
                  <div className="absolute left-2 top-2 rounded-md bg-primary px-2 py-0.5 text-[10px] font-bold text-on-primary shadow-xs">
                    MỚI
                  </div>
                </div>
                <div className="p-3">
                  <h3 className="text-xs font-semibold text-on-surface line-clamp-2 group-hover:text-primary">
                    {product.title}
                  </h3>
                  <p className="mt-2 text-sm font-black text-primary">{formatVndPrice(priceVal)}</p>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}
