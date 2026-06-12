export const ORDER_ACTION_ERROR_MESSAGES = {
  "COMMERCE-401": "Phiên đăng nhập không hợp lệ.",
  "COMMERCE-404-ORDER": "Không tìm thấy đơn hàng.",
  "COMMERCE-409-ORDER-NOT-CANCELLABLE":
    "Đơn hàng không thể hủy ở trạng thái hiện tại (đã thanh toán hoặc đang giao hàng).",
  "COMMERCE-409-ORDER-ITEMS": "Chưa có sản phẩm nào ở trạng thái đã giao để xác nhận.",
  "COMMERCE-409-PAYMENT-STATE": "Thanh toán chưa hoàn tất. Vui lòng thanh toán trước khi xác nhận nhận hàng.",
};

export function mapOrderActionApiError(error) {
  const code = String(error?.code ?? "");
  return (
    ORDER_ACTION_ERROR_MESSAGES[code] ||
    error?.message ||
    "Có lỗi xảy ra. Vui lòng thử lại."
  );
}

export function buildCancelOrderSuccessToast(result) {
  if (result?.alreadyCancelled) {
    return "Đơn hàng đã được hủy trước đó.";
  }
  return "Đã hủy đơn hàng thành công.";
}

export function buildConfirmOrderReceivedSuccessToast(result) {
  if (result?.alreadyConfirmed) {
    return "Đơn hàng đã được xác nhận nhận hàng trước đó.";
  }

  const parts = ["Đã xác nhận nhận hàng thành công."];
  if (result?.itemsCompleted > 0) {
    parts.push(`Hoàn tất ${result.itemsCompleted} mục hàng.`);
  }
  if (result?.paymentMarkedPaid) {
    parts.push("Thanh toán COD đã được ghi nhận.");
  }
  return parts.join(" ");
}