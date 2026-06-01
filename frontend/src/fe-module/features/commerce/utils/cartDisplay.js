import { getUnavailableReasonLabel } from "../constants/cartConstants";

export function isCartItemInvalid(item) {
  return !item?.inStock || Boolean(item?.unavailableReason);
}

export function isCartItemCheckoutEligible(item) {
  return item?.status === "ACTIVE" && item?.inStock && !item?.unavailableReason;
}

export function getEligibleCartItems(cart) {
  return (cart?.items || []).filter(isCartItemCheckoutEligible);
}

export function getEligibleCartItemIds(cart) {
  return getEligibleCartItems(cart).map((item) => item.cartItemId);
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
