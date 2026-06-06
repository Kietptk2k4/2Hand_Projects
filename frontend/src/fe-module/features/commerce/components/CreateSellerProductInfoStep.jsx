import { PRODUCT_CONDITIONS, PRODUCT_TYPE_OPTIONS, TITLE_MAX } from "../constants/sellerProductConstants";
import { ShopImageUploadField } from "./ShopImageUploadField";
import { SHOP_MEDIA_KIND } from "../constants/shopMediaConstants";

const inputClass =
  "w-full rounded-lg border border-outline bg-surface-container-lowest px-4 py-2.5 text-body-md text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary";

const errorClass = "mt-1 text-sm text-error";

export function CreateSellerProductInfoStep({
  form,
  fieldErrors,
  categories,
  disabled,
  onFieldChange,
  onCancel,
  onNext,
}) {
  return (
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
      <h2 className="mb-6 text-headline-sm font-semibold text-on-surface">Thông tin sản phẩm</h2>

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
          {fieldErrors.title ? <p className={errorClass}>{fieldErrors.title}</p> : null}
        </div>

        <div>
          <label htmlFor="product-description" className="mb-1 block text-label-md font-medium text-on-surface">
            Mô tả <span className="text-error">*</span>
          </label>
          <textarea
            id="product-description"
            rows={4}
            className={`${inputClass} resize-none`}
            value={form.description}
            disabled={disabled}
            onChange={(e) => onFieldChange("description", e.target.value)}
          />
          {fieldErrors.description ? <p className={errorClass}>{fieldErrors.description}</p> : null}
        </div>

        <div>
          <label htmlFor="weight-gram" className="mb-1 block text-label-md font-medium text-on-surface">
            Khối lượng (gram) <span className="text-error">*</span>
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

        <ShopImageUploadField
          label="Ảnh đại diện sản phẩm"
          hint="Nhấn để chọn ảnh (khuyến nghị 1:1)"
          icon="image"
          aspectHint="avatar"
          uploadMediaKind={SHOP_MEDIA_KIND.PRODUCT_THUMBNAIL}
          value={form.thumbnailUrl}
          disabled={disabled}
          onChange={(url) => onFieldChange("thumbnailUrl", url)}
        />
      </div>

      <div className="mt-8 flex justify-end gap-3 border-t border-outline-variant pt-6">
        <button
          type="button"
          onClick={onCancel}
          disabled={disabled}
          className="rounded-lg px-6 py-2.5 text-label-md text-on-surface-variant hover:bg-surface-container-low"
        >
          Hủy
        </button>
        <button
          type="button"
          onClick={onNext}
          disabled={disabled}
          className="rounded-lg bg-primary px-6 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb]"
        >
          Tiếp theo
        </button>
      </div>
    </div>
  );
}
