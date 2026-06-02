import { getCartValidateReasonLabel } from "../constants/cartConstants";

function reasonToUnavailableReason(reason) {
  if (!reason) return null;
  if (reason === "SHOP_ON_VACATION") return "SHOP_ON_VACATION";
  if (reason === "OUT_OF_STOCK" || reason === "PRODUCT_NOT_FOUND") {
    return reason === "PRODUCT_NOT_FOUND" ? "PRODUCT_NOT_FOUND" : "OUT_OF_STOCK";
  }
  if (
    reason === "PRODUCT_NOT_ACTIVE" ||
    reason === "SHOP_NOT_ACTIVE" ||
    reason === "CATEGORY_INACTIVE" ||
    reason === "ACTIVE_PRICE_MISSING" ||
    reason === "INVALID_PRODUCT"
  ) {
    return "INVALID_PRODUCT";
  }
  return reason;
}

/** @deprecated Use applyValidationToCart */
export function mergeCartWithValidation(cart, validation) {
  return applyValidationToCart(cart, validation);
}

export function applyValidationToCart(cart, validation) {
  if (!cart || !validation) return cart;

  const invalidById = new Map(
    validation.invalidItems.map((entry) => [entry.cartItemId, entry])
  );

  const items = cart.items.map((item) => {
    const invalid = invalidById.get(item.cartItemId);
    if (!invalid) return item;

    const unavailableReason = reasonToUnavailableReason(invalid.reason);
    const isVacation = invalid.reason === "SHOP_ON_VACATION";

    return {
      ...item,
      status: invalid.currentStatus || item.status,
      unavailableReason,
      validateReason: invalid.reason,
      validateMessage: getCartValidateReasonLabel(invalid.reason),
      inStock: isVacation ? item.inStock : false,
    };
  });

  const eligible = items.filter(
    (item) => item.status === "ACTIVE" && item.inStock && !item.unavailableReason
  );
  const invalidCount = items.length - eligible.length;
  const subtotal = eligible.reduce(
    (sum, item) => sum + item.effectivePrice * item.quantity,
    0
  );

  const warnings = [];
  if (invalidCount > 0) {
    if (items.some((item) => item.unavailableReason === "OUT_OF_STOCK" || !item.inStock)) {
      warnings.push("Một số sản phẩm trong giỏ đã hết hàng.");
    }
    if (items.some((item) => item.unavailableReason === "SHOP_ON_VACATION")) {
      warnings.push("Một số shop đang nghỉ — không thể thanh toán các sản phẩm đó.");
    }
    if (
      items.some(
        (item) =>
          item.validateReason &&
          !["OUT_OF_STOCK", "SHOP_ON_VACATION"].includes(item.validateReason)
      )
    ) {
      warnings.push("Một số sản phẩm không còn hợp lệ để thanh toán.");
    }
  }

  return {
    ...cart,
    items,
    summary: {
      ...cart.summary,
      activeItemCount: eligible.length,
      invalidItemCount: invalidCount,
      subtotal,
      canCheckout: validation.canCheckout,
      warnings,
    },
  };
}
