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

/** Tổng số lượng SP (sum quantity) — dùng cho badge sidebar. */
export function getCartBadgeCountFromMapped(cart) {
  if (!cart?.items?.length) return 0;
  return cart.items.reduce((sum, item) => sum + (Number(item.quantity) || 0), 0);
}

export function getCartBadgeCountFromRaw(raw) {
  const items = raw?.items || [];
  return items.reduce((sum, item) => sum + (Number(item.quantity) || 0), 0);
}
