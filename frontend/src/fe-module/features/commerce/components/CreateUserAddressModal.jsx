import { useCallback, useEffect, useState } from "react";
import {
  EMPTY_ADDRESS_FORM,
  MOCK_DISTRICTS_BY_PROVINCE,
  MOCK_PROVINCES,
  MOCK_WARDS_BY_DISTRICT,
  VN_PHONE_REGEX,
} from "../constants/addressFormConstants";

const inputClass =
  "w-full rounded-lg border border-outline-variant bg-surface px-3 py-2 text-sm text-on-surface focus:border-primary focus:outline-none";

export function CreateUserAddressModal({ open, onClose, onSubmit, isSubmitting = false }) {
  const [form, setForm] = useState(EMPTY_ADDRESS_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [apiError, setApiError] = useState("");

  useEffect(() => {
    if (!open) return;
    setForm(EMPTY_ADDRESS_FORM);
    setFieldErrors({});
    setApiError("");
  }, [open]);

  const districts = form.provinceCode
    ? MOCK_DISTRICTS_BY_PROVINCE[form.provinceCode] || []
    : [];
  const wards = form.districtCode ? MOCK_WARDS_BY_DISTRICT[form.districtCode] || [] : [];

  const updateField = useCallback((name, value) => {
    setForm((prev) => {
      const next = { ...prev, [name]: value };
      if (name === "provinceCode") {
        next.districtCode = "";
        next.wardCode = "";
      }
      if (name === "districtCode") {
        next.wardCode = "";
      }
      return next;
    });
    setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    setApiError("");
  }, []);

  const validate = useCallback(() => {
    const errors = {};
    if (!form.receiverName.trim()) errors.receiverName = "Vui lòng nhập họ tên người nhận.";
    if (!form.phone.trim()) {
      errors.phone = "Vui lòng nhập số điện thoại.";
    } else if (!VN_PHONE_REGEX.test(form.phone.trim().replace(/\s/g, ""))) {
      errors.phone = "Số điện thoại không hợp lệ (0 hoặc +84 và 9–10 chữ số).";
    }
    if (!form.provinceCode) errors.provinceCode = "Vui lòng chọn tỉnh/thành phố.";
    if (!form.districtCode) errors.districtCode = "Vui lòng chọn quận/huyện.";
    if (!form.wardCode) errors.wardCode = "Vui lòng chọn phường/xã.";
    if (!form.addressDetail.trim()) errors.addressDetail = "Vui lòng nhập địa chỉ chi tiết.";
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }, [form]);

  const handleSubmit = useCallback(
    async (event) => {
      event.preventDefault();
      if (!validate()) return;

      setApiError("");
      try {
        await onSubmit?.(form);
        onClose?.();
      } catch (error) {
        if (error?.code === "COMMERCE-400-PHONE") {
          setFieldErrors((prev) => ({
            ...prev,
            phone: error?.message || "Số điện thoại không hợp lệ.",
          }));
          return;
        }
        setApiError(error?.message || "Không thể thêm địa chỉ. Vui lòng thử lại.");
      }
    },
    [form, onClose, onSubmit, validate]
  );

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-on-surface/40 p-4 backdrop-blur-sm"
      role="presentation"
      onClick={onClose}
    >
      <div
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-lg"
        role="dialog"
        aria-modal="true"
        aria-labelledby="create-address-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="mb-6 flex items-start justify-between gap-4">
          <h2 id="create-address-title" className="text-headline-sm font-semibold text-on-surface">
            Thêm địa chỉ mới
          </h2>
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="rounded-full p-1 text-on-surface-variant hover:bg-surface-container-low"
            aria-label="Đóng"
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              close
            </span>
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="receiver-name" className="mb-1 block text-sm font-medium text-on-surface">
              Họ tên người nhận
            </label>
            <input
              id="receiver-name"
              type="text"
              value={form.receiverName}
              onChange={(e) => updateField("receiverName", e.target.value)}
              className={inputClass}
              maxLength={255}
            />
            {fieldErrors.receiverName ? (
              <p className="mt-1 text-xs text-error">{fieldErrors.receiverName}</p>
            ) : null}
          </div>

          <div>
            <label htmlFor="phone" className="mb-1 block text-sm font-medium text-on-surface">
              Số điện thoại
            </label>
            <input
              id="phone"
              type="tel"
              value={form.phone}
              onChange={(e) => updateField("phone", e.target.value)}
              className={inputClass}
              placeholder="0901234567"
            />
            {fieldErrors.phone ? (
              <p className="mt-1 text-xs text-error">{fieldErrors.phone}</p>
            ) : null}
          </div>

          <div>
            <label htmlFor="province" className="mb-1 block text-sm font-medium text-on-surface">
              Tỉnh/Thành phố
            </label>
            <select
              id="province"
              value={form.provinceCode}
              onChange={(e) => updateField("provinceCode", e.target.value)}
              className={inputClass}
            >
              <option value="">Chọn tỉnh/thành phố</option>
              {MOCK_PROVINCES.map((item) => (
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
            <label htmlFor="district" className="mb-1 block text-sm font-medium text-on-surface">
              Quận/Huyện
            </label>
            <select
              id="district"
              value={form.districtCode}
              onChange={(e) => updateField("districtCode", e.target.value)}
              disabled={!form.provinceCode}
              className={inputClass}
            >
              <option value="">Chọn quận/huyện</option>
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
            <label htmlFor="ward" className="mb-1 block text-sm font-medium text-on-surface">
              Phường/Xã
            </label>
            <select
              id="ward"
              value={form.wardCode}
              onChange={(e) => updateField("wardCode", e.target.value)}
              disabled={!form.districtCode}
              className={inputClass}
            >
              <option value="">Chọn phường/xã</option>
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

          <div>
            <label htmlFor="address-detail" className="mb-1 block text-sm font-medium text-on-surface">
              Địa chỉ chi tiết
            </label>
            <input
              id="address-detail"
              type="text"
              value={form.addressDetail}
              onChange={(e) => updateField("addressDetail", e.target.value)}
              className={inputClass}
              placeholder="Số nhà, tên đường..."
            />
            {fieldErrors.addressDetail ? (
              <p className="mt-1 text-xs text-error">{fieldErrors.addressDetail}</p>
            ) : null}
          </div>

          <label className="flex cursor-pointer items-center gap-2">
            <input
              type="checkbox"
              checked={form.isDefault}
              onChange={(e) => updateField("isDefault", e.target.checked)}
              className="h-4 w-4 rounded border-outline-variant text-primary focus:ring-primary"
            />
            <span className="text-sm text-on-surface">Đặt làm địa chỉ mặc định</span>
          </label>

          {apiError ? (
            <p className="rounded-lg border border-error/30 bg-error-container/40 p-3 text-sm text-on-error-container">
              {apiError}
            </p>
          ) : null}

          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="flex-1 rounded-lg border border-outline-variant py-2.5 text-sm font-medium text-on-surface hover:bg-surface-container-low"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex flex-1 items-center justify-center gap-2 rounded-lg bg-primary py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-50"
            >
              {isSubmitting ? (
                <span className="h-5 w-5 animate-spin rounded-full border-2 border-on-primary border-t-transparent" />
              ) : (
                "Lưu địa chỉ"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
