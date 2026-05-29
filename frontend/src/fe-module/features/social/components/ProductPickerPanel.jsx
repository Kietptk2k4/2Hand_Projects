import { MOCK_PRODUCT_CATALOG } from "../constants/mockProductCatalog";
import { formatVndPrice } from "../utils/formatPrice";

export function ProductPickerPanel({ selectedIds = [], onSelect, onClose }) {
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
          <h3 className="text-lg font-semibold text-on-surface">Chọn dịch vụ / sản phẩm</h3>
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
        <ul className="max-h-[60vh] overflow-y-auto p-2">
          {MOCK_PRODUCT_CATALOG.map((product) => {
            const isSelected = selectedIds.includes(product.productId);
            return (
              <li key={product.productId}>
                <button
                  type="button"
                  disabled={isSelected}
                  onClick={() => onSelect(product)}
                  className="flex w-full items-center gap-3 rounded-lg p-3 text-left transition hover:bg-surface-container-low disabled:opacity-50"
                >
                  <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-surface-container-high">
                    <span className="material-symbols-outlined text-outline" aria-hidden="true">
                      sell
                    </span>
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-semibold text-on-surface">{product.name}</p>
                    <p className="text-xs text-on-surface-variant">{product.category}</p>
                  </div>
                  <span className="shrink-0 text-sm font-semibold text-on-surface">
                    {formatVndPrice(product.defaultPrice)}
                  </span>
                </button>
              </li>
            );
          })}
        </ul>
      </div>
    </div>
  );
}
