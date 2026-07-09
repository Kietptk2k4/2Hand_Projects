import { useEffect, useState } from "react";
import { isValidUuid } from "../utils/supportNavigation.js";
import { AdminSupportTargetBarView } from "./AdminSupportTargetBarView.jsx";

const TARGET_CONFIG = {
  "order-detail": {
    paramKey: "orderId",
    label: "Đơn hàng điều tra",
    placeholder: "Nhập UUID đơn hàng…",
    hint: "Dán UUID đơn hàng từ commerce_db hoặc ticket hỗ trợ.",
  },
  "payment-detail": {
    paramKey: "paymentId",
    label: "Thanh toán điều tra",
    placeholder: "Nhập UUID thanh toán…",
    hint: "Có thể mở từ tab Chi tiết đơn hàng hoặc nhập payment_id trực tiếp.",
  },
  "shipment-detail": {
    paramKey: "shipmentId",
    label: "Vận đơn điều tra",
    placeholder: "Nhập UUID vận đơn…",
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

  return (
    <AdminSupportTargetBarView
      label={config.label}
      placeholder={config.placeholder}
      hint={config.hint}
      paramKey={config.paramKey}
      inputValue={inputValue}
      validationError={validationError}
      currentId={targetIds[config.paramKey]}
      onInputChange={setInputValue}
      onSubmit={handleSubmit}
      onClear={handleClear}
    />
  );
}
