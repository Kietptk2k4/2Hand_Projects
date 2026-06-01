import { PRODUCT_CONDITIONS, PRODUCT_TYPE_OPTIONS, TITLE_MAX } from "../constants/sellerProductConstants";

const inputClass =
  "w-full rounded-lg border border-outline bg-surface-container-lowest px-4 py-2.5 text-body-md text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary";

const errorClass = "mt-1 text-sm text-error";

export function SellerProductInfoStep({
  form,
  fieldErrors,
  categories,
  disabled,
  onFieldChange,
}) {
  return (
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
      <h2 className="mb-6 text-headline-sm font-semibold text-on-surface">Thông tin cơ bản</h2>

      <div className="space-y-5">
        <div>
          <label htmlFor="product-type" className="mb-1 block text-label-md font-medium text-on-surface">
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
          <label htmlFor="category-id" className="mb-1 block text-label-md font-medium text-on-surface">
            Danh mục <span className="text-error">*</span>
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
          <label htmlFor="condition" className="mb-1 block text-label-md font-medium text-on-surface">
            Tình trạng <span className="text-error">*</span>
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
          <label htmlFor="product-title" className="mb-1 block text-label-md font-medium text-on-surface">
            Tên sản phẩm <span className="text-error">*</span>
          </label>
          <input
            id="product-title"
            type="text"
            maxLength={TITLE_MAX}
            className={inputClass}
            value={form.title}
            disabled={disabled}
            onChange={(e) => onFieldChange("title", e.target.value)}
          />
          <p className="mt-1 text-right text-body-sm text-on-surface-variant">
            {form.title.length}/{TITLE_MAX}
          </p>
          {fieldErrors.title ? <p className={errorClass}>{fieldErrors.title}</p> : null}
        </div>

        <div>
          <label
            htmlFor="product-description"
            className="mb-1 block text-label-md font-medium text-on-surface"
          >
            Mô tả <span className="text-error">*</span>
          </label>
          <div className="mb-2 flex gap-1 rounded-t-lg border border-b-0 border-outline-variant bg-surface-container-low px-2 py-1">
            {["format_bold", "format_italic", "format_list_bulleted", "link"].map((icon) => (
              <button
                key={icon}
                type="button"
                disabled
                className="rounded p-1 text-on-surface-variant opacity-50"
                aria-hidden="true"
              >
                <span className="material-symbols-outlined text-[18px]">{icon}</span>
              </button>
            ))}
          </div>
          <textarea
            id="product-description"
            rows={5}
            className={`${inputClass} rounded-t-none`}
            value={form.description}
            disabled={disabled}
            onChange={(e) => onFieldChange("description", e.target.value)}
          />
          {fieldErrors.description ? <p className={errorClass}>{fieldErrors.description}</p> : null}
        </div>

        <div>
          <label htmlFor="weight-gram" className="mb-1 block text-label-md font-medium text-on-surface">
            Cân nặng (gram) <span className="text-error">*</span>
          </label>
          <input
            id="weight-gram"
            type="number"
            min={1}
            className={inputClass}
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
