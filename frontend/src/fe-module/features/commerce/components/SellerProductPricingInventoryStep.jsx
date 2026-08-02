const inputClass =
  "w-full rounded-2xl border border-outline-variant bg-surface-container-lowest px-4 py-3 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm";

const errorClass = "mt-1 text-xs font-bold text-error";

export function SellerProductPricingInventoryStep({ form, fieldErrors, disabled, onFieldChange }) {
  return (
    <div className="space-y-8">
      <section className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-6 shadow-xs sm:p-8">
        <h2 className="mb-1 text-base font-black text-on-surface sm:text-lg flex items-center gap-2 border-b border-outline-variant/60 pb-3">
          <span className="material-symbols-outlined text-primary text-xl" aria-hidden="true">
            payments
          </span>
          GIÁ BÁN SẢN PHẨM
        </h2>
        <p className="mb-6 text-xs font-semibold text-on-surface-variant pt-2">
          Mỗi lần đổi giá bán sẽ tạo một bản ghi lịch sử giá mới theo chính sách sàn.
        </p>

        <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
          <div>
            <label htmlFor="price" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
              Giá niêm yết (₫) <span className="text-rose-600">*</span>
            </label>
            <input
              id="price"
              type="number"
              min={0}
              className={inputClass}
              placeholder="Ví dụ: 250000"
              value={form.price}
              disabled={disabled}
              onChange={(e) => onFieldChange("price", e.target.value)}
            />
            {fieldErrors.price ? <p className={errorClass}>{fieldErrors.price}</p> : null}
          </div>

          <div>
            <label htmlFor="sale-price" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
              Giá khuyến mãi (₫) (Tùy chọn)
            </label>
            <input
              id="sale-price"
              type="number"
              min={0}
              className={inputClass}
              placeholder="Ví dụ: 199000"
              value={form.salePrice}
              disabled={disabled}
              onChange={(e) => onFieldChange("salePrice", e.target.value)}
            />
            {fieldErrors.salePrice ? <p className={errorClass}>{fieldErrors.salePrice}</p> : null}
          </div>

          <div>
            <label htmlFor="sale-start" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
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
            {fieldErrors.saleStartAt ? <p className={errorClass}>{fieldErrors.saleStartAt}</p> : null}
          </div>

          <div>
            <label htmlFor="sale-end" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
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
            {fieldErrors.saleEndAt ? <p className={errorClass}>{fieldErrors.saleEndAt}</p> : null}
          </div>
        </div>
      </section>

      <section className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-6 shadow-xs sm:p-8">
        <h2 className="mb-1 text-base font-black text-on-surface sm:text-lg flex items-center gap-2 border-b border-outline-variant/60 pb-3">
          <span className="material-symbols-outlined text-primary text-xl" aria-hidden="true">
            inventory_2
          </span>
          QUẢN LÝ TỒN KHO
        </h2>
        <p className="mb-6 text-xs font-semibold text-on-surface-variant pt-2">
          Đồ Second-Hand: Mỗi sản phẩm là duy nhất — Tồn kho tiêu chuẩn 1 hoặc 0.
        </p>

        <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
          <div>
            <label htmlFor="stock" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
              Số lượng tồn kho <span className="text-rose-600">*</span>
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
            <label htmlFor="low-stock" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
              Ngưỡng cảnh báo sắp hết hàng
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
