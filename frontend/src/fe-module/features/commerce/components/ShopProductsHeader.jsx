import { ProductListSortSelect } from "./ProductListSortSelect";

export function ShopProductsHeader({ totalItems, sort, onSortChange, sortDisabled = false }) {
  return (
    <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <h2 className="text-headline-md font-semibold text-on-surface">
        Sản phẩm của shop
        {totalItems != null ? (
          <span className="ml-2 text-body-md font-normal text-on-surface-variant">
            ({totalItems})
          </span>
        ) : null}
      </h2>
      <ProductListSortSelect value={sort} onChange={onSortChange} disabled={sortDisabled} />
    </div>
  );
}
