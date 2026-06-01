import { getUnavailableReasonLabel } from "../constants/cartConstants";

export function isCartItemInvalid(item) {
  return !item?.inStock || Boolean(item?.unavailableReason);
}

export function getLineTotal(item) {
  return (item?.effectivePrice || 0) * (item?.quantity || 0);
}

export function getUnavailableLabel(item) {
  return getUnavailableReasonLabel(item?.unavailableReason);
}

export function getCartItemCountLabel(cart) {
  if (!cart) return "";
  const total = cart.items.length;
  if (total === 0) return "Giỏ hàng trống";
  return `${total} sản phẩm trong giỏ hàng`;
}
