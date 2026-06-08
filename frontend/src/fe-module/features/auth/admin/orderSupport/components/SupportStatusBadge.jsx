const STATUS_CLASS = {
  PAID: "bg-green-50 text-green-800",
  PENDING: "bg-amber-50 text-amber-900",
  FAILED: "bg-red-50 text-red-800",
  CANCELLED: "bg-gray-100 text-gray-700",
  EXPIRED: "bg-gray-100 text-gray-700",
  SHIPPED: "bg-blue-50 text-blue-800",
  DELIVERED: "bg-green-50 text-green-800",
  PROCESSING: "bg-indigo-50 text-indigo-800",
  COMPLETED: "bg-green-50 text-green-800",
  PROCESSED: "bg-green-50 text-green-800",
  INVALID_SIGNATURE: "bg-red-50 text-red-800",
  RECONCILED: "bg-green-50 text-green-800",
  OUTSTANDING: "bg-amber-50 text-amber-900",
  AWAITING_WEBHOOK: "bg-amber-50 text-amber-900",
};

export function SupportStatusBadge({ status, className = "" }) {
  if (!status) return null;
  const badgeClass = STATUS_CLASS[status] || "bg-surface-container-high text-on-surface";
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${badgeClass} ${className}`}
    >
      {status}
    </span>
  );
}
