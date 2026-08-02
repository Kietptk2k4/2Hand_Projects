import { PRODUCT_CONDITIONS, PRODUCT_TYPE_OPTIONS, TITLE_MAX } from "../constants/sellerProductConstants";

const inputClass =
  "w-full rounded-2xl border border-outline-variant bg-surface-container-lowest px-4 py-3 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm";

const errorClass = "mt-1 text-xs font-bold text-error";

export function SellerProductInfoStep({
  form,
  fieldErrors,
  categories,
  brands,
  disabled,
  onFieldChange,
}) {
  return (
    <div className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-6 shadow-xs sm:p-8">
      <h2 className="mb-6 text-base font-black text-on-surface sm:text-lg flex items-center gap-2 border-b border-outline-variant/60 pb-3.5">
        <span className="material-symbols-outlined text-primary text-xl" aria-hidden="true">
          info
        </span>
        THÔNG TIN CƠ BẢN
      </h2>

      <div className="space-y-5">
        <div>
          <label htmlFor="product-type" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
            Loại sản phẩm
          </label>
          <select
            id="product-type"
            className={inputClass}
            value={form.productType}
            disabled={disabled}
            onChange={(e) => onFieldChange("productType", e.target.value)}
          >
            {PRODUCT_TYPE_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="category-id" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
            Danh mục sản phẩm <span className="text-rose-600">*</span>
          </label>
          <select
            id="category-id"
            className={inputClass}
            value={form.categoryId}
            disabled={disabled}
            onChange={(e) => onFieldChange("categoryId", e.target.value)}
          >
            <option value="">Chọn danh mục</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.name}
              </option>
            ))}
          </select>
          {fieldErrors.categoryId ? <p className={errorClass}>{fieldErrors.categoryId}</p> : null}
        </div>

        <div>
          <label htmlFor="brand-id" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
            Thương hiệu <span className="text-rose-600">*</span>
          </label>
          <select
            id="brand-id"
            className={inputClass}
            value={form.brandId}
            disabled={disabled}
            onChange={(e) => onFieldChange("brandId", e.target.value)}
          >
            {brands.map((brand) => (
              <option key={brand.id} value={brand.id}>
                {brand.name}
              </option>
            ))}
          </select>
          {fieldErrors.brandId ? <p className={errorClass}>{fieldErrors.brandId}</p> : null}
        </div>

        <div>
          <label htmlFor="condition" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
            Tình trạng sản phẩm 2Hand <span className="text-rose-600">*</span>
          </label>
          <select
            id="condition"
            className={inputClass}
            value={form.condition}
            disabled={disabled}
            onChange={(e) => onFieldChange("condition", e.target.value)}
          >
            {PRODUCT_CONDITIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="product-title" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
            Tên sản phẩm <span className="text-rose-600">*</span>
          </label>
          <input
            id="product-title"
            type="text"
            maxLength={TITLE_MAX}
            className={inputClass}
            placeholder="Nhập tên sản phẩm (Ví dụ: Áo khoác Vintage Denim Levis Size L...)"
            value={form.title}
            disabled={disabled}
            onChange={(e) => onFieldChange("title", e.target.value)}
          />
          <p className="mt-1 text-right text-xs font-semibold text-on-surface-variant">
            {form.title.length}/{TITLE_MAX}
          </p>
          {fieldErrors.title ? <p className={errorClass}>{fieldErrors.title}</p> : null}
        </div>

        <div>
          <label
            htmlFor="product-description"
            className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm"
          >
            Mô tả sản phẩm <span className="text-rose-600">*</span>
          </label>
          <div className="mb-2 flex gap-1 rounded-t-2xl border border-b-0 border-outline-variant bg-surface-container-low px-3 py-1.5">
            {["format_bold", "format_italic", "format_list_bulleted", "link"].map((icon) => (
              <button
                key={icon}
                type="button"
                disabled
                className="rounded-lg p-1 text-on-surface-variant opacity-50"
                aria-hidden="true"
              >
                <span className="material-symbols-outlined text-base">{icon}</span>
              </button>
            ))}
          </div>
          <textarea
            id="product-description"
            rows={5}
            className={`${inputClass} rounded-t-none`}
            placeholder="Mô tả chi tiết chất liệu, kích thước, độ mới và xuất xứ của sản phẩm..."
            value={form.description}
            disabled={disabled}
            onChange={(e) => onFieldChange("description", e.target.value)}
          />
          {fieldErrors.description ? <p className={errorClass}>{fieldErrors.description}</p> : null}
        </div>

        <div>
          <label htmlFor="weight-gram" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
            Cân nặng đóng gói (gram) <span className="text-rose-600">*</span>
          </label>
          <input
            id="weight-gram"
            type="number"
            min={1}
            className={inputClass}
            placeholder="Ví dụ: 350"
            value={form.weightGram}
            disabled={disabled}
            onChange={(e) => onFieldChange("weightGram", e.target.value)}
          />
          {fieldErrors.weightGram ? <p className={errorClass}>{fieldErrors.weightGram}</p> : null}
        </div>
      </div>
    </div>
  );
}
