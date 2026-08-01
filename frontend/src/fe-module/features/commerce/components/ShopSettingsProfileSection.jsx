import { SHOP_NAME_MAX } from "../constants/shopSettingsConstants";
import { ShopImageUploadField } from "./ShopImageUploadField";

const inputClass =
  "w-full rounded-2xl border border-outline-variant bg-surface-container-lowest px-4 py-3 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm";

const errorClass = "mt-1 text-xs font-bold text-error";

export function ShopSettingsProfileSection({
  form,
  fieldErrors,
  shop,
  disabled,
  onFieldChange,
}) {
  return (
    <section className="rounded-2xl border border-outline-variant bg-surface-container-lowest p-6 shadow-xs sm:p-8">
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3 border-b border-outline-variant/60 pb-4">
        <h2 className="text-lg font-black text-on-surface flex items-center gap-2">
          <span className="material-symbols-outlined text-primary text-2xl">storefront</span>
          HỒ SƠ CỬA HÀNG
        </h2>
        {shop?.status ? (
          <span className="rounded-xl bg-emerald-50 px-3 py-1 text-xs font-bold text-emerald-700 border border-emerald-200">
            🟢 {shop.status}
            {shop.ratingCount > 0
              ? ` · ${shop.ratingAvg} ⭐ (${shop.ratingCount} đánh giá)`
              : null}
          </span>
        ) : null}
      </div>

      <div className="space-y-6">
        <div>
          <label htmlFor="settings-shop-name" className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm">
            Tên cửa hàng <span className="text-rose-600">*</span>
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
            className="mb-1.5 block text-xs font-bold text-on-surface sm:text-sm"
          >
            Mô tả cửa hàng
          </label>
          <textarea
            id="settings-shop-description"
            rows={4}
            className={`${inputClass} resize-none`}
            placeholder="Giới thiệu cửa hàng, các dòng sản phẩm secondhand chính và cam kết với khách hàng..."
            value={form.description ?? ""}
            disabled={disabled}
            onChange={(event) => onFieldChange("description", event.target.value)}
          />
        </div>

        <div className="grid grid-cols-1 gap-6 border-t border-outline-variant/60 pt-6 sm:grid-cols-2">
          <ShopImageUploadField
            label="Ảnh đại diện Avatar (1:1)"
            hint="Nhấn để chọn ảnh đại diện"
            icon="account_circle"
            aspectHint="avatar"
            value={form.avatarUrl ?? ""}
            disabled={disabled}
            onChange={(url) => onFieldChange("avatarUrl", url)}
          />

          <ShopImageUploadField
            label="Ảnh bìa Cover (16:9)"
            hint="Nhấn để chọn ảnh bìa"
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
