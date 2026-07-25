import { useMemo } from "react";

// Image mapping helper based on category name/slug keywords
function getCategoryImage(categoryName = "", categorySlug = "", index = 0) {
  const name = (categoryName + " " + categorySlug).toLowerCase();
  
  if (name.includes("nu") || name.includes("women") || name.includes("vay")) {
    return "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=500&auto=format&fit=crop&q=80";
  }
  if (name.includes("nam") || name.includes("men")) {
    return "https://images.unsplash.com/photo-1617137968427-85924c800a22?w=500&auto=format&fit=crop&q=80";
  }
  if (name.includes("giay") || name.includes("shoes") || name.includes("sneaker")) {
    return "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=500&auto=format&fit=crop&q=80";
  }
  if (name.includes("tui") || name.includes("phu kien") || name.includes("bags") || name.includes("accessories")) {
    return "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=500&auto=format&fit=crop&q=80";
  }
  if (name.includes("unisex") || name.includes("streetwear")) {
    return "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=500&auto=format&fit=crop&q=80";
  }
  if (name.includes("vintage") || name.includes("designer")) {
    return "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&auto=format&fit=crop&q=80";
  }
  if (name.includes("quan") || name.includes("pants") || name.includes("jeans") || name.includes("kaki")) {
    return "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&auto=format&fit=crop&q=80";
  }
  if (name.includes("ao") || name.includes("tops") || name.includes("shirt")) {
    return "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80";
  }

  // Fallback pool by index
  const pool = [
    "https://images.unsplash.com/photo-1445205170230-053b83016050?w=500&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=500&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=500&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=500&auto=format&fit=crop&q=80",
  ];
  return pool[index % pool.length];
}

export function CommerceCategoryGridSection({ categories = [], isLoading = false, onCategoryClick }) {
  // Filter and display top active categories from API
  const displayCategories = useMemo(() => {
    if (!categories || categories.length === 0) return [];
    
    // Sort or select categories with products or level 1 & 2
    return categories
      .filter((cat) => cat.categoryId && cat.categoryName)
      .slice(0, 8);
  }, [categories]);

  if (!isLoading && displayCategories.length === 0) {
    return null;
  }

  return (
    <section className="mb-10">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-on-surface sm:text-xl">DANH MỤC NỔI BẬT</h2>
          <p className="text-xs text-on-surface-variant">
            Khám phá thế giới đồ 2Hand phân loại chuyên nghiệp
          </p>
        </div>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="h-28 animate-pulse rounded-xl bg-surface-container-high" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
          {displayCategories.map((cat, index) => {
            const countText = cat.productCount !== undefined ? `${cat.productCount} sản phẩm` : "Xem ngay";
            const imageUrl = getCategoryImage(cat.categoryName, cat.categorySlug, index);

            return (
              <div
                key={cat.categoryId}
                onClick={() => onCategoryClick?.(cat)}
                className="group relative cursor-pointer overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-xs transition-all hover:-translate-y-1 hover:border-primary/40 hover:shadow-md"
              >
                <div className="relative aspect-[4/3] w-full overflow-hidden bg-surface-container-low">
                  <img
                    src={imageUrl}
                    alt={cat.categoryName}
                    className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-110"
                    loading="lazy"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/75 via-black/25 to-transparent" />
                </div>

                <div className="absolute bottom-0 left-0 right-0 p-2.5 text-white">
                  <h3 className="text-xs font-bold text-white line-clamp-1 group-hover:text-amber-300">
                    {cat.categoryName}
                  </h3>
                  <p className="text-[10px] text-white/80 font-medium">{countText}</p>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
}
