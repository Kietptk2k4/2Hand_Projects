const ADD_TO_CART_ERROR_MESSAGES = {
  'COMMERCE-400-VALIDATION': 'Dữ liệu không hợp lệ. Vui lòng kiểm tra số lượng.',
  'COMMERCE-401': 'Phiên đăng nhập đã hết hạn.',
  'COMMERCE-404-PRODUCT': 'Không tìm thấy sản phẩm.',
  'COMMERCE-409-NOT-PURCHASABLE': 'Sản phẩm hiện không thể mua.',
  'COMMERCE-409-PRICE': 'Sản phẩm chưa có giá bán hợp lệ.',
  'COMMERCE-409-STOCK': 'Sản phẩm đã hết hàng.',
};

export function mapAddToCartApiError(error) {
  const code = String(error?.code ?? '');
  return ADD_TO_CART_ERROR_MESSAGES[code] || error?.message || 'Không thêm được sản phẩm vào giỏ.';
}
