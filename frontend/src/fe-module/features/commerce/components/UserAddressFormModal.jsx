import { useCallback, useEffect, useState } from "react";
import { EMPTY_ADDRESS_FORM, VN_PHONE_REGEX } from "../constants/addressFormConstants";
import { mapAddressApiError } from "../constants/addressConstants";
import { GhnAddressFields } from "./GhnAddressFields";
import { addressFormToInitialValues } from "../utils/addressMapper";

const inputClass =
  "w-full rounded-lg border border-outline-variant bg-surface px-3 py-2 text-sm text-on-surface focus:border-primary focus:outline-none";

export function UserAddressFormModal({
  mode = "create",
  open,
  initialValues,
  onClose,
  onSubmit,
  isSubmitting = false,
}) {
  const isEdit = mode === "edit";
  const [form, setForm] = useState(EMPTY_ADDRESS_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [apiError, setApiError] = useState("");

  useEffect(() => {
    if (!open) return;
    setForm(initialValues ? addressFormToInitialValues(initialValues) : EMPTY_ADDRESS_FORM);
    setFieldErrors({});
    setApiError("");
  }, [open, initialValues]);

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
        setApiError(
          mapAddressApiError(error) ||
            (isEdit ? "Không thể cập nhật địa chỉ." : "Không thể thêm địa chỉ."),
        );
      }
    },
    [form, isEdit, onClose, onSubmit, validate],
  );

  if (!open) return null;

  const titleId = isEdit ? "edit-address-title" : "create-address-title";
  const title = isEdit ? "Sửa địa chỉ" : "Thêm địa chỉ mới";
  const submitLabel = isEdit ? "Lưu thay đổi" : "Lưu địa chỉ";

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
        aria-labelledby={titleId}
        onClick={(event) => event.stopPropagation()}
      >
        <div className="mb-6 flex items-start justify-between gap-4">
          <h2 id={titleId} className="text-headline-sm font-semibold text-on-surface">
            {title}
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

          <GhnAddressFields
            idPrefix={isEdit ? "edit-address" : "create-address"}
            values={form}
            fieldErrors={fieldErrors}
            onFieldChange={updateField}
            disabled={isSubmitting}
            inputClass={inputClass}
            selectClass={inputClass}
            enabled={open}
          />

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
                submitLabel
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
