import { SHOP_NAME_MAX } from "../constants/createShopConstants";
import { ShopImageUploadField } from "./ShopImageUploadField";

const inputClass =
  "w-full rounded-lg border border-outline bg-surface-container-lowest px-3 py-2.5 text-body-md text-on-surface focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary";

const errorClass = "mt-1 text-sm text-error";

export function CreateShopBrandStep({
  form,
  fieldErrors,
  disabled,
  onFieldChange,
  onCancel,
  onNext,
}) {
  return (
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
      <h2 className="mb-6 border-b border-outline-variant pb-3 text-headline-md font-semibold text-on-surface">
        Bước 1: Thương hiệu
      </h2>

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        <div className="md:col-span-2">
          <label htmlFor="shop-name" className="mb-1 block text-label-md font-medium text-on-surface">
            Tên shop <span className="text-error">*</span>
          </label>
          <input
            id="shop-name"
            type="text"
            maxLength={SHOP_NAME_MAX}
            className={inputClass}
            placeholder="VD: Tu do thoi trang Lan"
            value={form.shopName}
            disabled={disabled}
            onChange={(event) => onFieldChange("shopName", event.target.value)}
          />
          {fieldErrors.shopName ? <p className={errorClass}>{fieldErrors.shopName}</p> : null}
        </div>

        <div className="md:col-span-2">
          <label
            htmlFor="shop-description"
            className="mb-1 block text-label-md font-medium text-on-surface"
          >
            Mô tả shop
          </label>
          <textarea
            id="shop-description"
            rows={4}
            className={`${inputClass} resize-none`}
            placeholder="Giới thiệu shop, sản phẩm và điểm nổi bật của bạn..."
            value={form.description}
            disabled={disabled}
            onChange={(event) => onFieldChange("description", event.target.value)}
          />
        </div>

        <ShopImageUploadField
          label="Ảnh đại diện shop"
          hint="Nhấn để chọn ảnh (tỉ lệ 1:1 khuyến nghị)"
          icon="account_circle"
          aspectHint="avatar"
          value={form.avatarUrl}
          disabled={disabled}
          onChange={(url) => onFieldChange("avatarUrl", url)}
        />

        <ShopImageUploadField
          label="Ảnh bìa shop"
          hint="Nhấn để chọn ảnh (tỉ lệ 16:9 khuyến nghị)"
          icon="wallpaper"
          aspectHint="cover"
          value={form.coverUrl}
          disabled={disabled}
          onChange={(url) => onFieldChange("coverUrl", url)}
        />
      </div>

      <div className="mt-8 flex justify-end gap-3 border-t border-outline-variant pt-6">
        <button
          type="button"
          disabled={disabled}
          onClick={onCancel}
          className="rounded-lg px-6 py-2.5 text-label-md text-on-surface-variant transition-colors hover:bg-surface-container-low"
        >
          Hủy
        </button>
        <button
          type="button"
          disabled={disabled}
          onClick={onNext}
          className="flex items-center gap-1 rounded-lg bg-primary px-6 py-2.5 text-label-md font-medium text-on-primary shadow-sm hover:bg-[#0050cb] focus:ring-2 focus:ring-primary focus:ring-offset-2"
        >
          Tiếp theo
          <span className="material-symbols-outlined text-lg" aria-hidden="true">
            arrow_forward
          </span>
        </button>
      </div>
    </div>
  );
}
