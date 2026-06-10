export const SHIPMENT_LIST_SORT_OPTIONS = [
  { value: "updated_at", label: "Cập nhật gần nhất" },
  { value: "created_at", label: "Tạo gần nhất" },
  { value: "shipped_at", label: "Gửi hàng gần nhất" },
];

export const SHIPMENT_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "PENDING", label: "PENDING" },
  { value: "PICKING_UP", label: "PICKING_UP" },
  { value: "READY_TO_SHIP", label: "READY_TO_SHIP" },
  { value: "SHIPPED", label: "SHIPPED" },
  { value: "DELIVERED", label: "DELIVERED" },
  { value: "FAILED", label: "FAILED" },
  { value: "CANCELLED", label: "CANCELLED" },
  { value: "RETURNED", label: "RETURNED" },
];

export const SHIPMENT_LIST_CARRIER_OPTIONS = [
  { value: "", label: "Tất cả carrier" },
  { value: "GHN", label: "GHN" },
  { value: "MANUAL", label: "MANUAL" },
  { value: "SELF_DELIVERY", label: "SELF_DELIVERY" },
];

export const SHIPMENT_LIST_PAGE_SIZE = 20;
