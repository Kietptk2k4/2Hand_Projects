import { useState } from "react";
import { ATTRIBUTE_NAME_MAX, ATTRIBUTE_VALUE_MAX } from "../constants/sellerProductConstants";

const inputClass =
  "w-full rounded-lg border border-outline bg-surface-container-lowest px-3 py-2 text-body-md text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary";

export function ProductAttributesEditor({ attributes, disabled, onChange, fieldErrors }) {
  const [draftName, setDraftName] = useState("");
  const [draftValue, setDraftValue] = useState("");
  const [localError, setLocalError] = useState("");

  const addAttribute = () => {
    const name = draftName.trim();
    const value = draftValue.trim();
    if (!name || !value) {
      setLocalError("Vui lòng nhập tên và giá trị thuộc tính.");
      return;
    }
    if (name.length > ATTRIBUTE_NAME_MAX || value.length > ATTRIBUTE_VALUE_MAX) {
      setLocalError(`Tên tối đa ${ATTRIBUTE_NAME_MAX}, giá trị tối đa ${ATTRIBUTE_VALUE_MAX} ký tự.`);
      return;
    }
    if (attributes.some((a) => a.name.trim().toLowerCase() === name.toLowerCase())) {
      setLocalError("Tên thuộc tính đã tồn tại.");
      return;
    }
    onChange?.([...attributes, { name, value }]);
    setDraftName("");
    setDraftValue("");
    setLocalError("");
  };

  const removeAt = (index) => {
    onChange?.(attributes.filter((_, i) => i !== index));
  };

  return (
    <div>
      {attributes.length > 0 ? (
        <ul className="mb-4 divide-y divide-outline-variant rounded-lg border border-outline-variant">
          {attributes.map((attr, index) => (
            <li
              key={`${attr.name}-${index}`}
              className="flex items-center justify-between gap-3 px-4 py-3"
            >
              <div className="min-w-0">
                <p className="text-label-md font-medium text-on-surface">{attr.name}</p>
                <p className="truncate text-body-sm text-on-surface-variant">{attr.value}</p>
              </div>
              <button
                type="button"
                disabled={disabled}
                onClick={() => removeAt(index)}
                className="shrink-0 rounded p-1 text-on-surface-variant hover:bg-surface-container-low"
                aria-label="Xóa thuộc tính"
              >
                <span className="material-symbols-outlined text-[20px]">delete</span>
              </button>
            </li>
          ))}
        </ul>
      ) : (
        <p className="mb-4 text-body-sm text-on-surface-variant">Chưa có thuộc tính nào.</p>
      )}

      <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
        <input
          type="text"
          placeholder="Tên thuộc tính (vd: Màu sắc)"
          className={inputClass}
          value={draftName}
          maxLength={ATTRIBUTE_NAME_MAX}
          disabled={disabled}
          onChange={(e) => setDraftName(e.target.value)}
        />
        <input
          type="text"
          placeholder="Giá trị (vd: Đỏ)"
          className={inputClass}
          value={draftValue}
          maxLength={ATTRIBUTE_VALUE_MAX}
          disabled={disabled}
          onChange={(e) => setDraftValue(e.target.value)}
        />
      </div>

      {localError ? <p className="mt-2 text-sm text-error">{localError}</p> : null}
      {fieldErrors?.attributes ? (
        <p className="mt-2 text-sm text-error">{fieldErrors.attributes}</p>
      ) : null}

      <button
        type="button"
        disabled={disabled}
        onClick={addAttribute}
        className="mt-3 inline-flex items-center gap-1 rounded-lg border border-primary px-4 py-2 text-label-md font-medium text-primary hover:bg-surface-container-low"
      >
        <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
          add
        </span>
        Thêm thuộc tính
      </button>

      <div className="mt-4 rounded-lg bg-primary-container/30 p-4 text-body-sm text-on-surface">
        <span className="material-symbols-outlined mr-1 align-middle text-[18px]" aria-hidden="true">
          info
        </span>
        Thuộc tính được snapshot khi checkout — đơn hàng đã tạo không bị thay đổi khi bạn sửa sau này.
      </div>
    </div>
  );
}
