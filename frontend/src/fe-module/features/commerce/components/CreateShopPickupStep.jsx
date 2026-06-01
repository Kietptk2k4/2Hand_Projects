import {
  MOCK_DISTRICTS_BY_PROVINCE,
  MOCK_PROVINCES,
  MOCK_WARDS_BY_DISTRICT,
} from "../constants/addressFormConstants";

const inputClass =
  "w-full rounded-lg border border-outline bg-surface-container-lowest px-3 py-2.5 text-body-md text-on-surface focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary";

const selectClass = inputClass;

const errorClass = "mt-1 text-sm text-error";

export function CreateShopPickupStep({
  form,
  fieldErrors,
  disabled,
  onFieldChange,
  onBack,
  onSubmit,
}) {
  const pickup = form.pickup;
  const districts = pickup.provinceCode
    ? MOCK_DISTRICTS_BY_PROVINCE[pickup.provinceCode] || []
    : [];
  const wards = pickup.districtCode ? MOCK_WARDS_BY_DISTRICT[pickup.districtCode] || [] : [];

  return (
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
      <h2 className="mb-6 border-b border-outline-variant pb-3 text-headline-md font-semibold text-on-surface">
        Bước 2: Địa chỉ lấy hàng
      </h2>

      <label className="mb-6 flex cursor-pointer items-start gap-3 rounded-lg border border-outline-variant/60 bg-surface-container-low p-4">
        <input
          type="checkbox"
          className="mt-1 h-4 w-4 rounded border-outline text-primary focus:ring-primary"
          checked={form.includePickup}
          disabled={disabled}
          onChange={(event) => onFieldChange("includePickup", event.target.checked)}
        />
        <span>
          <span className="block text-label-md font-medium text-on-surface">
            Thiết lập địa chỉ lấy hàng ngay
          </span>
          <span className="mt-0.5 block text-body-sm text-on-surface-variant">
            Bỏ chọn nếu bạn muốn cấu hình kho sau khi tạo shop.
          </span>
        </span>
      </label>

      {form.includePickup ? (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div className="md:col-span-2">
            <label htmlFor="pickup-name" className="mb-1 block text-label-md font-medium text-on-surface">
              Tên điểm lấy hàng / kho <span className="text-error">*</span>
            </label>
            <input
              id="pickup-name"
              type="text"
              className={inputClass}
              value={pickup.pickupName}
              disabled={disabled}
              onChange={(event) => onFieldChange("pickup.pickupName", event.target.value)}
            />
            {fieldErrors["pickup.pickupName"] ? (
              <p className={errorClass}>{fieldErrors["pickup.pickupName"]}</p>
            ) : null}
          </div>

          <div className="md:col-span-2">
            <label htmlFor="pickup-phone" className="mb-1 block text-label-md font-medium text-on-surface">
              Số điện thoại <span className="text-error">*</span>
            </label>
            <input
              id="pickup-phone"
              type="tel"
              className={inputClass}
              placeholder="0901234567"
              value={pickup.phone}
              disabled={disabled}
              onChange={(event) => onFieldChange("pickup.phone", event.target.value)}
            />
            {fieldErrors["pickup.phone"] ? (
              <p className={errorClass}>{fieldErrors["pickup.phone"]}</p>
            ) : null}
          </div>

          <div>
            <label htmlFor="pickup-province" className="mb-1 block text-label-md font-medium text-on-surface">
              Tỉnh/TP <span className="text-error">*</span>
            </label>
            <select
              id="pickup-province"
              className={selectClass}
              value={pickup.provinceCode}
              disabled={disabled}
              onChange={(event) => onFieldChange("pickup.provinceCode", event.target.value)}
            >
              <option value="">Chọn tỉnh/thành phố</option>
              {MOCK_PROVINCES.map((item) => (
                <option key={item.value} value={item.value}>
                  {item.label}
                </option>
              ))}
            </select>
            {fieldErrors["pickup.provinceCode"] ? (
              <p className={errorClass}>{fieldErrors["pickup.provinceCode"]}</p>
            ) : null}
          </div>

          <div>
            <label htmlFor="pickup-district" className="mb-1 block text-label-md font-medium text-on-surface">
              Quận/Huyện <span className="text-error">*</span>
            </label>
            <select
              id="pickup-district"
              className={selectClass}
              value={pickup.districtCode}
              disabled={disabled || !pickup.provinceCode}
              onChange={(event) => onFieldChange("pickup.districtCode", event.target.value)}
            >
              <option value="">Chọn quận/huyện</option>
              {districts.map((item) => (
                <option key={item.value} value={item.value}>
                  {item.label}
                </option>
              ))}
            </select>
            {fieldErrors["pickup.districtCode"] ? (
              <p className={errorClass}>{fieldErrors["pickup.districtCode"]}</p>
            ) : null}
          </div>

          <div>
            <label htmlFor="pickup-ward" className="mb-1 block text-label-md font-medium text-on-surface">
              Phường/Xã <span className="text-error">*</span>
            </label>
            <select
              id="pickup-ward"
              className={selectClass}
              value={pickup.wardCode}
              disabled={disabled || !pickup.districtCode}
              onChange={(event) => onFieldChange("pickup.wardCode", event.target.value)}
            >
              <option value="">Chọn phường/xã</option>
              {wards.map((item) => (
                <option key={item.value} value={item.value}>
                  {item.label}
                </option>
              ))}
            </select>
            {fieldErrors["pickup.wardCode"] ? (
              <p className={errorClass}>{fieldErrors["pickup.wardCode"]}</p>
            ) : null}
          </div>

          <div className="md:col-span-2">
            <label
              htmlFor="pickup-address-detail"
              className="mb-1 block text-label-md font-medium text-on-surface"
            >
              Địa chỉ chi tiết <span className="text-error">*</span>
            </label>
            <input
              id="pickup-address-detail"
              type="text"
              className={inputClass}
              placeholder="Số nhà, tên đường..."
              value={pickup.addressDetail}
              disabled={disabled}
              onChange={(event) => onFieldChange("pickup.addressDetail", event.target.value)}
            />
            {fieldErrors["pickup.addressDetail"] ? (
              <p className={errorClass}>{fieldErrors["pickup.addressDetail"]}</p>
            ) : null}
          </div>
        </div>
      ) : (
        <p className="text-body-sm text-on-surface-variant">
          Bạn có thể thêm địa chỉ lấy hàng sau trong phần cài đặt seller (sắp ra mắt).
        </p>
      )}

      <div className="mt-8 flex flex-wrap justify-between gap-3 border-t border-outline-variant pt-6">
        <button
          type="button"
          disabled={disabled}
          onClick={onBack}
          className="flex items-center gap-1 rounded-lg px-6 py-2.5 text-label-md text-on-surface-variant transition-colors hover:bg-surface-container-low"
        >
          <span className="material-symbols-outlined text-lg" aria-hidden="true">
            arrow_back
          </span>
          Quay lại
        </button>
        <button
          type="button"
          disabled={disabled}
          onClick={onSubmit}
          className="flex items-center gap-1 rounded-lg bg-primary px-6 py-2.5 text-label-md font-medium text-on-primary shadow-sm hover:bg-[#0050cb] disabled:opacity-60"
        >
          {disabled ? "Đang tạo shop..." : "Tạo shop"}
          <span className="material-symbols-outlined text-lg" aria-hidden="true">
            check_circle
          </span>
        </button>
      </div>
    </div>
  );
}
