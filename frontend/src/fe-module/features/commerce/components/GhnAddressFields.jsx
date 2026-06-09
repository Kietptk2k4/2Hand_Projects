import { useGhnAddressOptions } from "../hooks/useGhnAddressOptions";

const defaultInputClass =
  "w-full rounded-lg border border-outline-variant bg-surface px-3 py-2 text-sm text-on-surface focus:border-primary focus:outline-none";

export function GhnAddressFields({
  idPrefix = "ghn-address",
  values,
  fieldErrors = {},
  onFieldChange,
  disabled = false,
  inputClass = defaultInputClass,
  selectClass = defaultInputClass,
  enabled = true,
}) {
  const {
    provinces,
    districts,
    wards,
    isLoadingProvinces,
    isLoadingDistricts,
    isLoadingWards,
    loadError,
    retry,
  } = useGhnAddressOptions({
    provinceCode: values?.provinceCode,
    districtCode: values?.districtCode,
    enabled,
  });

  const provinceId = `${idPrefix}-province`;
  const districtId = `${idPrefix}-district`;
  const wardId = `${idPrefix}-ward`;

  return (
    <>
      {loadError ? (
        <div className="rounded-lg border border-error/30 bg-error-container/40 p-3 text-sm text-on-error-container md:col-span-2">
          <p>{loadError}</p>
          <button
            type="button"
            onClick={retry}
            disabled={disabled}
            className="mt-2 text-sm font-medium text-primary hover:underline"
          >
            Thử lại
          </button>
        </div>
      ) : null}

      <div>
        <label htmlFor={provinceId} className="mb-1 block text-sm font-medium text-on-surface">
          Tỉnh/Thành phố
        </label>
        <select
          id={provinceId}
          value={values?.provinceCode || ""}
          onChange={(event) => onFieldChange("provinceCode", event.target.value)}
          disabled={disabled || isLoadingProvinces}
          className={selectClass}
        >
          <option value="">{isLoadingProvinces ? "Đang tải..." : "Chọn tỉnh/thành phố"}</option>
          {provinces.map((item) => (
            <option key={item.value} value={item.value}>
              {item.label}
            </option>
          ))}
        </select>
        {fieldErrors.provinceCode ? (
          <p className="mt-1 text-xs text-error">{fieldErrors.provinceCode}</p>
        ) : null}
      </div>

      <div>
        <label htmlFor={districtId} className="mb-1 block text-sm font-medium text-on-surface">
          Quận/Huyện
        </label>
        <select
          id={districtId}
          value={values?.districtCode || ""}
          onChange={(event) => onFieldChange("districtCode", event.target.value)}
          disabled={disabled || !values?.provinceCode || isLoadingDistricts}
          className={selectClass}
        >
          <option value="">
            {!values?.provinceCode
              ? "Chọn tỉnh/thành trước"
              : isLoadingDistricts
                ? "Đang tải..."
                : "Chọn quận/huyện"}
          </option>
          {districts.map((item) => (
            <option key={item.value} value={item.value}>
              {item.label}
            </option>
          ))}
        </select>
        {fieldErrors.districtCode ? (
          <p className="mt-1 text-xs text-error">{fieldErrors.districtCode}</p>
        ) : null}
      </div>

      <div>
        <label htmlFor={wardId} className="mb-1 block text-sm font-medium text-on-surface">
          Phường/Xã
        </label>
        <select
          id={wardId}
          value={values?.wardCode || ""}
          onChange={(event) => onFieldChange("wardCode", event.target.value)}
          disabled={disabled || !values?.districtCode || isLoadingWards}
          className={selectClass}
        >
          <option value="">
            {!values?.districtCode
              ? "Chọn quận/huyện trước"
              : isLoadingWards
                ? "Đang tải..."
                : "Chọn phường/xã"}
          </option>
          {wards.map((item) => (
            <option key={item.value} value={item.value}>
              {item.label}
            </option>
          ))}
        </select>
        {fieldErrors.wardCode ? (
          <p className="mt-1 text-xs text-error">{fieldErrors.wardCode}</p>
        ) : null}
      </div>
    </>
  );
}