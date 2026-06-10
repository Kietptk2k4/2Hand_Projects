export const ORDER_SUPPORT_EMPTY_ORDER_MESSAGE =
  "Nhập UUID đơn hàng ở thanh tra cứu phía trên để xem chi tiết hỗ trợ.";

export const ORDER_SUPPORT_EMPTY_PAYMENT_MESSAGE =
  "Chọn một thanh toán trong danh sách hoặc nhập UUID ở thanh tra cứu phía trên.";

export const ORDER_SUPPORT_EMPTY_SHIPMENT_MESSAGE =
  "Nhập UUID vận đơn hoặc mở từ chi tiết đơn hàng để xem dữ liệu.";

export const ORDER_SUPPORT_ORDER_TITLE = "Chi tiết đơn hàng";
export const ORDER_SUPPORT_ORDER_SUBTITLE =
  "Xem trạng thái đơn, sản phẩm, thanh toán và vận chuyển (read-only).";

export const ORDER_SUPPORT_PAYMENT_TITLE = "Chi tiết thanh toán";
export const ORDER_SUPPORT_PAYMENT_SUBTITLE =
  "Đối soát PayOS/COD và lịch sử webhook liên quan thanh toán.";

export const ORDER_SUPPORT_SHIPMENT_TITLE = "Chi tiết vận chuyển";
export const ORDER_SUPPORT_SHIPMENT_SUBTITLE =
  "Theo dõi vận đơn, địa chỉ giao hàng (masked) và sự kiện carrier.";

export const ORDER_SUPPORT_WEBHOOK_TITLE = "Nhật ký Webhook";
export const ORDER_SUPPORT_WEBHOOK_SUBTITLE =
  "Tra cứu webhook PayOS/GHN để debug callback và idempotency.";

export const ORDER_SUPPORT_COMMERCE_UNAVAILABLE =
  "Commerce service không khả dụng. Vui lòng thử lại sau hoặc liên hệ kỹ thuật.";

export const ORDER_SUPPORT_MASKED_NOTICE =
  "Thông tin liên hệ giao hàng đã được che (PII masked) theo chính sách hỗ trợ.";

export const ORDER_SUPPORT_SHIPMENT_OVERRIDE_TITLE = "Ghi đè trạng thái vận đơn";
export const ORDER_SUPPORT_SHIPMENT_OVERRIDE_NOTICE =
  "Chỉ cập nhật dữ liệu Commerce (DB). Không gọi GHN API hay đồng bộ carrier.";
export const ORDER_SUPPORT_SHIPMENT_OVERRIDE_FORCE_HINT =
  "Bật khi cần sửa vận đơn đã ở trạng thái kết thúc (DELIVERED, CANCELLED, RETURNED, FAILED). Cần quyền SHIPMENT_SUPPORT_FORCE_WRITE.";
export const ORDER_SUPPORT_SHIPMENT_OVERRIDE_SUCCESS =
  "Đã ghi đè trạng thái vận đơn thành công.";
export const ORDER_SUPPORT_SHIPMENT_OVERRIDE_UNCHANGED =
  "Trạng thái vận đơn không thay đổi.";