const inputClass =
  "w-full rounded-lg border border-outline bg-surface-container-lowest px-4 py-2.5 text-body-md text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary";

const errorClass = "mt-1 text-sm text-error";

export function CreateSellerProductPricingStep({
  form,
  fieldErrors,
  disabled,
  onFieldChange,
  onBack,
  onSaveDraft,
  onPublish,
}) {
  return (
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
      <h2 className="mb-6 text-headline-sm font-semibold text-on-surface">Giá & tồn kho</h2>
      <p className="mb-6 text-body-sm text-on-surface-variant">
        Thiết lập giá và tồn kho để có thể đăng bán sản phẩm ngay sau khi tạo.
      </p>

      <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
        <div>
          <label htmlFor="price" className="mb-1 block text-label-md font-medium text-on-surface">
            Giá bán (₫) <span className="text-error">*</span>
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
          {fieldErrors.stockQuantity ? <p className={errorClass}>{fieldErrors.stockQuantity}</p> : null}
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

      <div className="mt-8 flex flex-wrap justify-between gap-3 border-t border-outline-variant pt-6">
        <button
          type="button"
          onClick={onBack}
          disabled={disabled}
          className="rounded-lg px-6 py-2.5 text-label-md text-on-surface-variant hover:bg-surface-container-low"
        >
          Quay lại
        </button>
        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            onClick={onSaveDraft}
            disabled={disabled}
            className="rounded-lg border border-primary px-6 py-2.5 text-label-md font-medium text-primary hover:bg-surface-container-low"
          >
            Lưu bản nháp
          </button>
          <button
            type="button"
            onClick={onPublish}
            disabled={disabled}
            className="rounded-lg bg-primary px-6 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-50"
          >
            {disabled ? "Đang lưu..." : "Đăng bán ngay"}
          </button>
        </div>
      </div>
    </div>
  );
}
