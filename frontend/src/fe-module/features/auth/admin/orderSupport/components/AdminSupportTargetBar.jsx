import { useEffect, useState } from "react";
import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";
import { isValidUuid } from "../utils/supportNavigation.js";

const TARGET_CONFIG = {
  "order-detail": {
    paramKey: "orderId",
    label: "Đơn hàng điều tra",
    placeholder: "Nhập UUID đơn hàng...",
    hint: "Dán UUID đơn hàng từ commerce_db hoặc ticket hỗ trợ.",
  },
  "payment-detail": {
    paramKey: "paymentId",
    label: "Thanh toán điều tra",
    placeholder: "Nhập UUID thanh toán...",
    hint: "Có thể mở từ tab Chi tiết đơn hàng hoặc nhập payment_id trực tiếp.",
  },
  "shipment-detail": {
    paramKey: "shipmentId",
    label: "Vận đơn điều tra",
    placeholder: "Nhập UUID vận đơn...",
    hint: "Có thể mở từ tab Chi tiết đơn hàng hoặc nhập shipment_id trực tiếp.",
  },
};

export function AdminSupportTargetBar({ activeTab, targetIds, onTargetChange }) {
  const config = TARGET_CONFIG[activeTab];
  const [inputValue, setInputValue] = useState("");
  const [validationError, setValidationError] = useState("");

  useEffect(() => {
    if (!config) return;
    const current = targetIds[config.paramKey] || "";
    setInputValue(current);
    setValidationError("");
  }, [activeTab, config, targetIds]);

  if (!config) {
    return null;
  }

  const handleSubmit = (event) => {
    event.preventDefault();
    const trimmed = inputValue.trim();
    if (!trimmed) {
      onTargetChange({ [config.paramKey]: "" });
      setValidationError("");
      return;
    }
    if (!isValidUuid(trimmed)) {
      setValidationError("UUID không hợp lệ.");
      return;
    }
    setValidationError("");
    onTargetChange({ [config.paramKey]: trimmed });
  };

  const handleClear = () => {
    setInputValue("");
    setValidationError("");
    onTargetChange({ [config.paramKey]: "" });
  };

  const currentId = targetIds[config.paramKey];

  return (
    <AccountCard className="mb-6">
      <form onSubmit={handleSubmit}>
        <label htmlFor="support-target-input" className="mb-1.5 block text-xs font-semibold text-on-surface">
          {config.label}
        </label>
        <div className="flex flex-col gap-2 sm:flex-row">
          <input
            id="support-target-input"
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder={config.placeholder}
            className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2.5 text-base outline-none focus:border-primary focus:ring-1 focus:ring-primary/30"
            autoComplete="off"
          />
          <div className="flex shrink-0 gap-2">
            <button
              type="submit"
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-white hover:opacity-90"
            >
              Tra cứu
            </button>
            {currentId ? (
              <button
                type="button"
                onClick={handleClear}
                className="rounded-lg border border-outline-variant px-4 py-2.5 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
              >
                Xóa
              </button>
            ) : null}
          </div>
        </div>
        {validationError ? (
          <p className="mt-2 text-xs text-error">{validationError}</p>
        ) : (
          <p className="mt-2 text-xs text-on-surface-variant">{config.hint}</p>
        )}
        {currentId ? (
          <p className="mt-2 break-all text-xs text-on-surface-variant">
            {config.paramKey}: {currentId}
          </p>
        ) : null}
      </form>
    </AccountCard>
  );
}
