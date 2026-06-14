import { useVnpayRetry } from "../hooks/useVnpayRetry";

export function RetryVnpayPaymentButton({
  orderId,
  label = "Thanh toán lại",
  className = "rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-60",
  onClick,
}) {
  const { retry, isRetrying, error } = useVnpayRetry(orderId);

  const handleClick = async (event) => {
    event?.stopPropagation?.();
    onClick?.(event);
    await retry();
  };

  return (
    <div className="inline-flex flex-col items-end gap-1">
      <button type="button" onClick={handleClick} disabled={isRetrying} className={className}>
        {isRetrying ? "Đang tạo liên kết..." : label}
      </button>
      {error ? <span className="text-xs text-error">{error}</span> : null}
    </div>
  );
}
