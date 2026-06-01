import { ProductListSortSelect } from "./ProductListSortSelect";

export function CommerceSearchResultsHeader({
  keyword,
  displayedCount,
  totalItems,
  sort,
  onSortChange,
  sortDisabled = false,
}) {
  if (!keyword) return null;

  return (
    <header className="mb-6 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div>
        <h1 className="text-headline-md font-semibold text-on-surface md:text-headline-lg">
          Kết quả tìm kiếm cho &quot;{keyword}&quot;
        </h1>
        {totalItems > 0 ? (
          <p className="mt-2 text-body-sm text-on-surface-variant">
            Hiển thị {displayedCount} / {totalItems} sản phẩm
          </p>
        ) : null}
      </div>
      <ProductListSortSelect value={sort} onChange={onSortChange} disabled={sortDisabled} />
    </header>
  );
}
