import { useState } from "react";
import { CATEGORY_NAV_ITEMS } from "../constants/productListConstants";

export function CommerceHomeHero({ onSearchSubmit, onCategoryClick }) {
  const [query, setQuery] = useState("");

  const handleSubmit = (event) => {
    event.preventDefault();
    onSearchSubmit?.(query);
  };

  return (
    <section className="mx-auto mb-10 max-w-4xl text-center">
      <h1 className="text-headline-xl-mobile font-bold text-on-surface md:text-headline-xl">
        Khám phá sản phẩm chuyên nghiệp
      </h1>
      <p className="mt-4 text-body-lg text-on-surface-variant">
        Tìm công cụ và vật tư chất lượng từ các shop đã xác minh trên 2Hands.
      </p>

      <form
        onSubmit={handleSubmit}
        className="relative mx-auto mt-6 max-w-2xl rounded-lg shadow-sm transition-shadow hover:shadow-md"
      >
        <span
          className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline"
          aria-hidden="true"
        >
          search
        </span>
        <input
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Tìm sản phẩm, thương hiệu hoặc danh mục..."
          className="w-full rounded-lg border border-outline-variant bg-surface py-4 pl-12 pr-28 text-body-md outline-none focus:border-primary focus:ring-2 focus:ring-primary-container"
        />
        <button
          type="submit"
          className="absolute right-2 top-1/2 -translate-y-1/2 rounded-md bg-primary px-4 py-2 text-label-md text-on-primary transition-colors hover:bg-[#0050cb]"
        >
          Tìm kiếm
        </button>
      </form>

      <div className="mt-6 flex flex-wrap justify-center gap-3">
        {CATEGORY_NAV_ITEMS.map((item) => (
          <button
            key={item.categoryId}
            type="button"
            onClick={() => onCategoryClick?.(item)}
            className="rounded-full border border-outline-variant bg-surface-container-lowest px-4 py-2 text-label-md text-on-surface transition-colors hover:bg-surface-container-low"
          >
            {item.label}
          </button>
        ))}
      </div>
    </section>
  );
}
