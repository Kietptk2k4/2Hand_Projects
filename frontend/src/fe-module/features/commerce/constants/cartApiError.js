const SELF_PURCHASE_MESSAGE = "Bạn không thể mua sản phẩm của chính mình.";

const ADD_TO_CART_ERROR_MESSAGES = {
  "COMMERCE-400-VALIDATION": "Dữ liệu không hợp lệ. Vui lòng kiểm tra số lượng.",
  "COMMERCE-401": "Phiên đăng nhập đã hết hạn.",
  "COMMERCE-404-PRODUCT": "Không tìm thấy sản phẩm.",
  "COMMERCE-409-NOT-PURCHASABLE": "Sản phẩm hiện không thể mua.",
  "COMMERCE-409-PRICE": "Sản phẩm chưa có giá bán hợp lệ.",
  "COMMERCE-409-STOCK": "Sản phẩm đã hết hàng.",
  "COMMERCE-409-SELF-PURCHASE": SELF_PURCHASE_MESSAGE,
};

const CHECKOUT_ERROR_MESSAGES = {
  "COMMERCE-409-SELF-PURCHASE": SELF_PURCHASE_MESSAGE,
};

export function mapAddToCartApiError(error) {
  const code = String(error?.code ?? "");
  return ADD_TO_CART_ERROR_MESSAGES[code] || error?.message || "Không thêm được sản phẩm vào giỏ.";
}

export function mapCheckoutApiError(error, fallback) {
  const code = String(error?.code ?? "");
  return CHECKOUT_ERROR_MESSAGES[code] || error?.message || fallback;
}
