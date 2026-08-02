import { CATEGORY_DESCRIPTIONS } from "../constants/categoryProductsConstants";
import { ProductListSortSelect } from "./ProductListSortSelect";

export function CategoryProductsHeader({
  categoryName,
  categorySlug,
  totalItems,
  sort,
  onSortChange,
  includeChildren,
  onIncludeChildrenChange,
  sortDisabled = false,
}) {
  const description =
    CATEGORY_DESCRIPTIONS[categorySlug] ||
    "Khám phá sản phẩm 2Hand tuyển chọn chất lượng từ các shop uy tín trên 2Hands.";

  return (
    <header className="mb-6 rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs sm:p-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-xl font-black uppercase tracking-tight text-on-surface sm:text-2xl lg:text-3xl">
              {categoryName}
            </h1>
            {totalItems != null ? (
              <span className="rounded-xl bg-primary/10 px-3 py-1 text-xs font-bold text-primary border border-primary/20">
                🏷️ {totalItems} sản phẩm
              </span>
            ) : null}
          </div>
          <p className="mt-2 max-w-2xl text-xs font-semibold text-on-surface-variant sm:text-sm">
            {description}
          </p>
          <label className="mt-3 flex cursor-pointer items-center gap-2 lg:hidden">
            <input
              type="checkbox"
              checked={includeChildren}
              onChange={(event) => onIncludeChildrenChange(event.target.checked)}
              className="h-4 w-4 rounded border-outline text-primary focus:ring-primary cursor-pointer"
            />
            <span className="text-xs font-bold text-on-surface-variant">Bao gồm danh mục con</span>
          </label>
        </div>

        {/* Sort Select Dropdown */}
        <div className="shrink-0 pt-2 md:pt-0">
          <ProductListSortSelect value={sort} onChange={onSortChange} disabled={sortDisabled} />
        </div>
      </div>
    </header>
  );
}
