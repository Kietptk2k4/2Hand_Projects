import { findListProductById } from "./commerceProductDetailData";
import { MOCK_SHOP_AQUAFLOW_ID } from "./commerceShopData";

export const MOCK_CART_DEMO_USER_ID = "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1";

const cartsByUserId = new Map();

function createCartItemFromProduct(product, overrides = {}) {
  const listProduct = findListProductById(product.product_id) || product;
  return {
    cart_item_id: overrides.cart_item_id,
    product_id: listProduct.product_id,
    seller_id: overrides.seller_id || `e1000000-0000-4000-8000-${listProduct.shop_id.slice(-12)}`,
    shop_id: listProduct.shop_id,
    product_name: listProduct.title,
    image_url: listProduct.thumbnail_url,
    quantity: overrides.quantity ?? 1,
    status: "ACTIVE",
    effective_price: listProduct.effective_price,
    in_stock: overrides.in_stock ?? listProduct.in_stock !== false,
    available_quantity: overrides.available_quantity ?? 10,
    unavailable_reason: overrides.unavailable_reason ?? null,
  };
}

function buildSummary(items) {
  const eligible = items.filter(
    (item) => item.in_stock && !item.unavailable_reason && item.status === "ACTIVE"
  );
  const invalid = items.filter(
    (item) => !item.in_stock || item.unavailable_reason || item.status !== "ACTIVE"
  );

  const subtotal = eligible.reduce(
    (sum, item) => sum + item.effective_price * item.quantity,
    0
  );

  const warnings = [];
  if (invalid.some((item) => item.unavailable_reason === "OUT_OF_STOCK" || !item.in_stock)) {
    warnings.push("Một số sản phẩm trong giỏ đã hết hàng.");
  }
  if (invalid.some((item) => item.unavailable_reason === "SHOP_ON_VACATION")) {
    warnings.push("Một số shop đang nghỉ — không thể thanh toán các sản phẩm đó.");
  }

  return {
    active_item_count: eligible.length,
    invalid_item_count: invalid.length,
    subtotal,
    can_checkout: eligible.length > 0 && invalid.length === 0,
    warnings,
  };
}

function toCartResponse(cart) {
  const summary = buildSummary(cart.items);
  return {
    cart_id: cart.cart_id,
    items: cart.items.map((item) => ({ ...item })),
    summary,
    created_at: cart.created_at,
    updated_at: cart.updated_at,
  };
}

function createEmptyCart(userId) {
  const now = new Date().toISOString();
  return {
    cart_id: `cart-${userId.slice(0, 8)}`,
    user_id: userId,
    items: [],
    created_at: now,
    updated_at: now,
  };
}

function seedDemoCart() {
  const drill = findListProductById("c1000000-0000-4000-8000-000000000001");
  const gloves = findListProductById("c1000000-0000-4000-8000-000000000007");
  const pvc = findListProductById("c1000000-0000-4000-8000-000000000008");

  const now = new Date().toISOString();
  const cart = {
    cart_id: "cart-demo-0001-0000-4000-8000-000000000001",
    user_id: MOCK_CART_DEMO_USER_ID,
    items: [
      createCartItemFromProduct(drill, {
        cart_item_id: "c1100000-0000-4000-8000-000000000001",
        quantity: 2,
        in_stock: true,
        available_quantity: 10,
        unavailable_reason: null,
      }),
      createCartItemFromProduct(gloves, {
        cart_item_id: "c1100000-0000-4000-8000-000000000002",
        quantity: 1,
        in_stock: false,
        available_quantity: 0,
        unavailable_reason: "OUT_OF_STOCK",
      }),
      createCartItemFromProduct(pvc, {
        cart_item_id: "c1100000-0000-4000-8000-000000000003",
        quantity: 1,
        in_stock: true,
        available_quantity: 5,
        unavailable_reason: "SHOP_ON_VACATION",
      }),
    ],
    created_at: now,
    updated_at: now,
  };

  cartsByUserId.set(MOCK_CART_DEMO_USER_ID, cart);
}

seedDemoCart();

export function getOrCreateCartForUser(userId) {
  if (!cartsByUserId.has(userId)) {
    cartsByUserId.set(userId, createEmptyCart(userId));
  }
  return cartsByUserId.get(userId);
}

export function getCartResponseForUser(userId) {
  const cart = getOrCreateCartForUser(userId);
  return toCartResponse(cart);
}

export function findCartItem(cart, cartItemId) {
  return cart.items.find((item) => item.cart_item_id === cartItemId);
}

export function isCartItemCheckoutEligible(item) {
  return item.in_stock && !item.unavailable_reason && item.status === "ACTIVE";
}

export function updateCartItemQuantity(cart, cartItemId, quantity) {
  const item = findCartItem(cart, cartItemId);
  if (!item) return { error: "COMMERCE-404-CART-ITEM", status: 404 };

  if (!isCartItemCheckoutEligible(item)) {
    return { error: "COMMERCE-409-NOT-PURCHASABLE", status: 409 };
  }

  if (!Number.isInteger(quantity) || quantity <= 0) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  if (quantity > item.available_quantity) {
    return { error: "COMMERCE-409-STOCK", status: 409 };
  }

  item.quantity = quantity;
  cart.updated_at = new Date().toISOString();
  return { cart: toCartResponse(cart), item };
}

export function removeCartItem(cart, cartItemId) {
  const index = cart.items.findIndex((item) => item.cart_item_id === cartItemId);
  if (index === -1) {
    return { error: "COMMERCE-404-CART-ITEM", status: 404 };
  }

  cart.items.splice(index, 1);
  cart.updated_at = new Date().toISOString();
  return { cart: toCartResponse(cart) };
}
