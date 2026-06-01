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
    "Khám phá sản phẩm chất lượng từ các shop đã xác minh trên 2Hands.";

  return (
    <header className="mb-8 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div>
        <h1 className="text-headline-xl-mobile font-bold text-on-surface md:text-headline-xl">
          {categoryName}
        </h1>
        <p className="mt-2 max-w-2xl text-body-md text-on-surface-variant">{description}</p>
        {totalItems != null ? (
          <p className="mt-2 text-label-sm text-on-surface-variant">{totalItems} sản phẩm</p>
        ) : null}
        <label className="mt-4 flex cursor-pointer items-center gap-2 lg:hidden">
          <input
            type="checkbox"
            checked={includeChildren}
            onChange={(event) => onIncludeChildrenChange(event.target.checked)}
            className="h-4 w-4 rounded border-outline text-primary focus:ring-primary"
          />
          <span className="text-body-sm text-on-surface-variant">Bao gồm danh mục con</span>
        </label>
      </div>
      <ProductListSortSelect value={sort} onChange={onSortChange} disabled={sortDisabled} />
    </header>
  );
}
