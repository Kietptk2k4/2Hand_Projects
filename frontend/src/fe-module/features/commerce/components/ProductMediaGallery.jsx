import { useMemo, useState } from "react";
import { getConditionLabel } from "../constants/productDetailConstants";
import { isProductOnSale } from "../utils/productDetailDisplay";

export function ProductMediaGallery({ product }) {
  const galleryItems = useMemo(() => {
    if (product?.media?.length > 0) {
      return product.media.filter((item) => item.mediaType === "IMAGE" && item.mediaUrl);
    }
    return [];
  }, [product]);

  const [activeIndex, setActiveIndex] = useState(0);
  const activeItem = galleryItems[activeIndex] || galleryItems[0];
  const isOnSale = isProductOnSale(product);
  const conditionLabel = getConditionLabel(product?.condition);

  if (!product) return null;

  return (
    <section className="mb-8">
      <div className="relative aspect-[16/10] w-full overflow-hidden rounded-xl border border-outline-variant bg-surface-container-low shadow-sm md:aspect-[21/9]">
        {activeItem?.mediaUrl ? (
          <img
            src={activeItem.mediaUrl}
            alt={product.title}
            className="h-full w-full object-cover"
          />
        ) : (
          <div className="flex h-full items-center justify-center">
            <span className="material-symbols-outlined text-5xl text-outline" aria-hidden="true">
              image
            </span>
          </div>
        )}
        <div className="absolute left-4 top-4 flex flex-wrap gap-2">
          {conditionLabel ? (
            <span className="rounded-full bg-primary px-3 py-1 text-label-sm font-semibold text-on-primary shadow-md">
              {conditionLabel}
            </span>
          ) : null}
          {isOnSale ? (
            <span className="rounded-full bg-error px-3 py-1 text-label-sm font-semibold text-on-error shadow-md">
              Giảm giá
            </span>
          ) : null}
        </div>
      </div>

      {galleryItems.length > 1 ? (
        <div className="mt-4 flex gap-3 overflow-x-auto pb-2">
          {galleryItems.map((item, index) => (
            <button
              key={item.mediaId || index}
              type="button"
              onClick={() => setActiveIndex(index)}
              className={[
                "h-20 w-20 shrink-0 overflow-hidden rounded-lg border-2 transition-colors md:h-24 md:w-24",
                index === activeIndex ? "border-primary" : "border-outline-variant opacity-70 hover:opacity-100",
              ].join(" ")}
              aria-label={`Ảnh ${index + 1}`}
            >
              <img src={item.mediaUrl} alt="" className="h-full w-full object-cover" />
            </button>
          ))}
        </div>
      ) : null}
    </section>
  );
}
