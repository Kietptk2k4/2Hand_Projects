const inputClass =
  "w-full rounded-lg border border-outline bg-surface-container-lowest px-4 py-2.5 text-body-md text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary";

const errorClass = "mt-1 text-sm text-error";

export function SellerProductPricingInventoryStep({ form, fieldErrors, disabled, onFieldChange }) {
  return (
    <div className="space-y-8">
      <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
        <h2 className="mb-1 text-headline-sm font-semibold text-on-surface">Giá bán</h2>
        <p className="mb-6 text-body-sm text-on-surface-variant">
          Mỗi lần đổi giá sẽ tạo bản ghi giá mới (theo chính sách hệ thống).
        </p>

        <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
          <div>
            <label htmlFor="price" className="mb-1 block text-label-md font-medium text-on-surface">
              Giá niêm yết (₫) <span className="text-error">*</span>
            </label>
            <input
              id="price"
              type="number"
              min={0}
              className={inputClass}
              value={form.price}
              disabled={disabled}
              onChange={(e) => onFieldChange("price", e.target.value)}
            />
            {fieldErrors.price ? <p className={errorClass}>{fieldErrors.price}</p> : null}
          </div>

          <div>
            <label htmlFor="sale-price" className="mb-1 block text-label-md font-medium text-on-surface">
              Giá khuyến mãi (₫)
            </label>
            <input
              id="sale-price"
              type="number"
              min={0}
              className={inputClass}
              value={form.salePrice}
              disabled={disabled}
              onChange={(e) => onFieldChange("salePrice", e.target.value)}
            />
            {fieldErrors.salePrice ? <p className={errorClass}>{fieldErrors.salePrice}</p> : null}
          </div>

          <div>
            <label htmlFor="sale-start" className="mb-1 block text-label-md font-medium text-on-surface">
              Ngày bắt đầu khuyến mãi
            </label>
            <input
              id="sale-start"
              type="datetime-local"
              className={inputClass}
              value={form.saleStartAt}
              disabled={disabled}
              onChange={(e) => onFieldChange("saleStartAt", e.target.value)}
            />
          </div>

          <div>
            <label htmlFor="sale-end" className="mb-1 block text-label-md font-medium text-on-surface">
              Ngày kết thúc khuyến mãi
            </label>
            <input
              id="sale-end"
              type="datetime-local"
              className={inputClass}
              value={form.saleEndAt}
              disabled={disabled}
              onChange={(e) => onFieldChange("saleEndAt", e.target.value)}
            />
          </div>
        </div>
      </section>

      <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
        <h2 className="mb-1 text-headline-sm font-semibold text-on-surface">Tồn kho</h2>
        <p className="mb-6 text-body-sm text-on-surface-variant">
          Đồ second-hand: mỗi listing một món — tồn kho 0 hoặc 1; ngưỡng cảnh báo 0 hoặc 1.
        </p>

        <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
          <div>
            <label htmlFor="stock" className="mb-1 block text-label-md font-medium text-on-surface">
              Tồn kho <span className="text-error">*</span>
            </label>
            <input
              id="stock"
              type="number"
              min={0}
              max={1}
              className={inputClass}
              value={form.stockQuantity}
              disabled={disabled}
              onChange={(e) => onFieldChange("stockQuantity", e.target.value)}
            />
            {fieldErrors.stockQuantity ? (
              <p className={errorClass}>{fieldErrors.stockQuantity}</p>
            ) : null}
          </div>

          <div>
            <label htmlFor="low-stock" className="mb-1 block text-label-md font-medium text-on-surface">
              Ngưỡng sắp hết hàng
            </label>
            <input
              id="low-stock"
              type="number"
              min={0}
              max={1}
              className={inputClass}
              value={form.lowStockThreshold}
              disabled={disabled}
              onChange={(e) => onFieldChange("lowStockThreshold", e.target.value)}
            />
            {fieldErrors.lowStockThreshold ? (
              <p className={errorClass}>{fieldErrors.lowStockThreshold}</p>
            ) : null}
          </div>
        </div>
      </section>
    </div>
  );
}
