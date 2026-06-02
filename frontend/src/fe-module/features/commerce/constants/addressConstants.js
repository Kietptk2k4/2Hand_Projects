export const ADDRESS_ERROR_MESSAGES = {
  "COMMERCE-401": "Phiên đăng nhập đã hết hạn.",
  "COMMERCE-404-ADDRESS": "Không tìm thấy địa chỉ.",
  "COMMERCE-400-VALIDATION": "Vui lòng kiểm tra lại thông tin địa chỉ.",
  "COMMERCE-400-PHONE": "Số điện thoại không hợp lệ.",
  "COMMERCE-409-ADDRESS-DEFAULT": "Không thể cập nhật địa chỉ mặc định. Vui lòng thử lại.",
};

export const ADDRESS_TOAST_MESSAGES = {
  createSuccess: "Đã thêm địa chỉ giao hàng.",
  updateSuccess: "Đã cập nhật địa chỉ.",
  deleteSuccess: "Đã xóa địa chỉ.",
  setDefaultSuccess: "Đã đặt làm địa chỉ mặc định.",
};

export function mapAddressApiError(error) {
  const code = String(error?.code ?? "");
  return ADDRESS_ERROR_MESSAGES[code] || error?.message || "Có lỗi xảy ra. Vui lòng thử lại.";
}
