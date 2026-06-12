import { VN_PHONE_REGEX } from "../constants/addressFormConstants";

export function validateAddressForm(form) {
  const errors = {};
  if (!form.receiverName?.trim()) errors.receiverName = "Vui lòng nhập họ tên người nhận.";
  if (!form.phone?.trim()) {
    errors.phone = "Vui lòng nhập số điện thoại.";
  } else if (!VN_PHONE_REGEX.test(form.phone.trim().replace(/\s/g, ""))) {
    errors.phone = "Số điện thoại không hợp lệ (0 hoặc +84 và 9–10 chữ số).";
  }
  if (!form.provinceCode) errors.provinceCode = "Vui lòng chọn tỉnh/thành phố.";
  if (!form.districtCode) errors.districtCode = "Vui lòng chọn quận/huyện.";
  if (!form.wardCode) errors.wardCode = "Vui lòng chọn phường/xã.";
  if (!form.addressDetail?.trim()) errors.addressDetail = "Vui lòng nhập địa chỉ chi tiết.";
  return errors;
}