import { useEffect, useState } from "react";
import { formatVndPrice } from "../utils/formatPrice";
import { useTaggableProducts } from "../hooks/useTaggableProducts";

function ProductThumbnail({ imageUrl, name }) {
  if (imageUrl) {
    return (
      <img
        src={imageUrl}
        alt=""
        className="h-12 w-12 shrink-0 rounded-lg object-cover"
        loading="lazy"
      />
    );
  }

  return (
    <div
      className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-surface-container-high"
      aria-hidden="true"
    >
      <span className="material-symbols-outlined text-outline">inventory_2</span>
    </div>
  );
}

export function ProductPickerPanel({ selectedIds = [], onSelect, onClose }) {
  const [search, setSearch] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(search.trim()), 300);
    return () => clearTimeout(timer);
  }, [search]);

  const { products, status, errorMessage } = useTaggableProducts(debouncedSearch);

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center bg-on-background/50 p-4 backdrop-blur-sm">
      <div
        className="max-h-[80vh] w-full max-w-md overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-lg"
        role="dialog"
        aria-modal="true"
        aria-label="Chọn sản phẩm"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="flex items-center justify-between border-b border-outline-variant px-4 py-3">
          <h3 className="text-lg font-semibold text-on-surface">Chọn sản phẩm từ closet</h3>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full p-2 hover:bg-surface-container-low"
            aria-label="Đóng"
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              close
            </span>
          </button>
        </div>

        <div className="border-b border-outline-variant px-4 py-3">
          <input
            type="search"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Tìm theo tên sản phẩm..."
            className="w-full rounded-lg border border-outline-variant bg-surface-container-low px-3 py-2 text-sm text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
          />
          <p className="mt-2 text-xs text-on-surface-variant">
            Chỉ hiển thị sản phẩm trong shop của bạn (đang bán, nháp, tạm dừng).
          </p>
        </div>

        {status === "loading" ? (
          <p className="p-6 text-center text-sm text-on-surface-variant">Đang tải sản phẩm...</p>
        ) : null}

        {status === "error" ? (
          <p className="p-6 text-center text-sm text-error">{errorMessage}</p>
        ) : null}

        {status === "ready" && products.length === 0 ? (
          <p className="p-6 text-center text-sm text-on-surface-variant">
            Chưa có sản phẩm để gắn. Hãy tạo listing trong mục Quản lý sản phẩm trước.
          </p>
        ) : null}

        {status === "ready" && products.length > 0 ? (
          <ul className="max-h-[50vh] overflow-y-auto p-2">
            {products.map((product) => {
              const isSelected = selectedIds.includes(product.productId);
              return (
                <li key={product.productId}>
                  <button
                    type="button"
                    disabled={isSelected}
                    onClick={() => onSelect(product)}
                    className="flex w-full items-center gap-3 rounded-lg p-3 text-left transition hover:bg-surface-container-low disabled:opacity-50"
                  >
                    <ProductThumbnail imageUrl={product.imageUrl} name={product.name} />
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-semibold text-on-surface">{product.name}</p>
                      <p className="text-xs text-on-surface-variant">
                        {[product.category, product.status].filter(Boolean).join(" · ")}
                      </p>
                    </div>
                    <span className="shrink-0 text-sm font-semibold text-on-surface">
                      {formatVndPrice(product.defaultPrice)}
                    </span>
                  </button>
                </li>
              );
            })}
          </ul>
        ) : null}
      </div>
    </div>
  );
}
