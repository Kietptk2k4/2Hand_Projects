import { SHOP_NAME_MAX } from "../constants/shopSettingsConstants";
import { ShopImageUploadField } from "./ShopImageUploadField";

const inputClass =
  "w-full rounded-lg border border-outline bg-surface-container-lowest px-4 py-2 text-body-md text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary";

const errorClass = "mt-1 text-sm text-error";

export function ShopSettingsProfileSection({
  form,
  fieldErrors,
  shop,
  disabled,
  onFieldChange,
}) {
  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-headline-sm font-semibold text-on-surface">Hồ sơ cửa hàng</h2>
        {shop?.status ? (
          <span className="rounded-full bg-surface-container-high px-2.5 py-0.5 text-label-sm text-on-surface-variant">
            {shop.status}
            {shop.ratingCount > 0
              ? ` · ${shop.ratingAvg} (${shop.ratingCount} đánh giá)`
              : null}
          </span>
        ) : null}
      </div>

      <div className="space-y-6">
        <div>
          <label htmlFor="settings-shop-name" className="mb-1 block text-label-md font-medium text-on-surface">
            Tên cửa hàng <span className="text-error">*</span>
          </label>
          <input
            id="settings-shop-name"
            type="text"
            maxLength={SHOP_NAME_MAX}
            className={inputClass}
            value={form.shopName ?? ""}
            disabled={disabled}
            onChange={(event) => onFieldChange("shopName", event.target.value)}
          />
          {fieldErrors.shopName ? <p className={errorClass}>{fieldErrors.shopName}</p> : null}
        </div>

        <div>
          <label
            htmlFor="settings-shop-description"
            className="mb-1 block text-label-md font-medium text-on-surface"
          >
            Mô tả
          </label>
          <textarea
            id="settings-shop-description"
            rows={4}
            className={`${inputClass} resize-none`}
            placeholder="Giới thiệu cửa hàng, sản phẩm và điểm nổi bật..."
            value={form.description ?? ""}
            disabled={disabled}
            onChange={(event) => onFieldChange("description", event.target.value)}
          />
        </div>

        <div className="grid grid-cols-1 gap-6 border-t border-outline-variant pt-6 sm:grid-cols-2">
          <ShopImageUploadField
            label="Ảnh đại diện (1:1)"
            hint="Nhấn để chọn ảnh"
            icon="account_circle"
            aspectHint="avatar"
            value={form.avatarUrl ?? ""}
            disabled={disabled}
            onChange={(url) => onFieldChange("avatarUrl", url)}
          />

          <ShopImageUploadField
            label="Ảnh bìa (16:9)"
            hint="Nhấn để chọn ảnh"
            icon="wallpaper"
            aspectHint="cover"
            value={form.coverUrl ?? ""}
            disabled={disabled}
            onChange={(url) => onFieldChange("coverUrl", url)}
          />
        </div>
      </div>
    </section>
  );
}
