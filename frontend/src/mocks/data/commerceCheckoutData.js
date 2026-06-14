import { CHECKOUT_COD_ONLY_ENABLED } from "../../fe-module/features/commerce/constants/checkoutConstants";
import { findAddressForUser } from "./commerceAddressData";
import { registerOrderFromCheckout } from "./commerceOrderListData";
import { registerPaymentFromCheckout } from "./commercePaymentData";
import {
  getOrCreateCartForUser,
  findCartItem,
  isCartItemCheckoutEligible,
} from "./commerceCartData";

const SHIPPING_FEE_PER_SELLER = {
  STANDARD: 30000,
  EXPRESS: 52500,
  SAME_DAY: 80000,
};

const idempotencyStore = new Map();

function addDaysIso(days) {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date.toISOString().slice(0, 10);
}

function normalizeShipmentType(type) {
  if (!type || type === "STANDARD") return "STANDARD";
  if (["EXPRESS", "SAME_DAY"].includes(type)) return type;
  return null;
}

function resolveCartItems(userId, cartItemIds) {
  const cart = getOrCreateCartForUser(userId);
  const uniqueIds = [...new Set(cartItemIds || [])];

  const items = [];
  for (const cartItemId of uniqueIds) {
    const item = findCartItem(cart, cartItemId);
    if (!item) {
      return { error: "COMMERCE-404-CART-ITEM", status: 404 };
    }
    if (!isCartItemCheckoutEligible(item)) {
      return { error: "COMMERCE-409-NOT-PURCHASABLE", status: 409 };
    }
    items.push(item);
  }

  if (items.length === 0) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  return { items, cart };
}

function groupBySeller(items) {
  const map = new Map();
  for (const item of items) {
    const key = item.seller_id;
    if (!map.has(key)) {
      map.set(key, { seller_id: item.seller_id, shop_id: item.shop_id, items: [] });
    }
    map.get(key).items.push(item);
  }
  return [...map.values()];
}

function calculateShippingForGroups(groups, shipmentType) {
  const feePerSeller = SHIPPING_FEE_PER_SELLER[shipmentType] || SHIPPING_FEE_PER_SELLER.STANDARD;
  const etaDays = shipmentType === "SAME_DAY" ? 0 : shipmentType === "EXPRESS" ? 1 : 3;

  return groups.map((group) => ({
    seller_id: group.seller_id,
    shop_id: group.shop_id,
    shipping_fee: feePerSeller,
    shipping_fee_origin: feePerSeller,
    estimated_delivery_date: addDaysIso(etaDays),
    shipment_type: shipmentType,
  }));
}

export function buildCheckoutQuote(userId, { cartItemIds, addressId, shipmentType }) {
  const shipment = normalizeShipmentType(shipmentType);
  if (!shipment) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  if (!findAddressForUser(userId, addressId)) {
    return { error: "COMMERCE-404-ADDRESS", status: 404 };
  }

  const resolved = resolveCartItems(userId, cartItemIds);
  if (resolved.error) return resolved;

  const sellerGroups = groupBySeller(resolved.items);
  const shippingGroups = calculateShippingForGroups(sellerGroups, shipment);
  const totalShippingFee = shippingGroups.reduce((sum, g) => sum + g.shipping_fee, 0);

  const quoteItems = resolved.items.map((item) => {
    const itemTotal = item.effective_price * item.quantity;
    const group = sellerGroups.find((g) => g.seller_id === item.seller_id);
    const groupItemTotal = group.items.reduce(
      (sum, entry) => sum + entry.effective_price * entry.quantity,
      0
    );
    const groupShipping =
      shippingGroups.find((g) => g.seller_id === item.seller_id)?.shipping_fee || 0;
    const shippingFeeAllocated =
      groupItemTotal > 0 ? Math.round((itemTotal / groupItemTotal) * groupShipping) : 0;

    return {
      cart_item_id: item.cart_item_id,
      unit_price: item.effective_price,
      quantity: item.quantity,
      item_total: itemTotal,
      shipping_fee_allocated: shippingFeeAllocated,
    };
  });

  const totalAmount = quoteItems.reduce((sum, item) => sum + item.item_total, 0);

  return {
    data: {
      items: quoteItems,
      total_amount: totalAmount,
      shipping_fee: totalShippingFee,
      final_amount: totalAmount + totalShippingFee,
      seller_shipping_groups: shippingGroups.map((g) => ({
        seller_id: g.seller_id,
        shop_id: g.shop_id,
        shipping_fee: g.shipping_fee,
        shipment_type: g.shipment_type,
      })),
    },
  };
}

export function buildShippingFee(userId, { cartItemIds, addressId, shipmentType }) {
  const shipment = normalizeShipmentType(shipmentType);
  if (!shipment) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  if (!findAddressForUser(userId, addressId)) {
    return { error: "COMMERCE-404-ADDRESS", status: 404 };
  }

  const resolved = resolveCartItems(userId, cartItemIds);
  if (resolved.error) return resolved;

  const sellerGroups = groupBySeller(resolved.items);
  const shippingGroups = calculateShippingForGroups(sellerGroups, shipment);

  return {
    data: {
      seller_groups: shippingGroups,
      total_shipping_fee: shippingGroups.reduce((sum, g) => sum + g.shipping_fee, 0),
    },
  };
}

export function processCheckout(userId, body) {
  const {
    cart_item_ids: cartItemIds,
    address_id: addressId,
    payment_method: paymentMethod,
    shipment_type: shipmentType,
    idempotency_key: idempotencyKey,
  } = body;

  if (!["COD", "PAYOS", "VNPAY"].includes(paymentMethod)) {
    return { error: "COMMERCE-400-PAYMENT-METHOD", status: 400 };
  }

  if (CHECKOUT_COD_ONLY_ENABLED && paymentMethod !== "COD") {
    return { error: "COMMERCE-400-PAYMENT-METHOD", status: 400 };
  }

  const idemKey = `${userId}:${idempotencyKey || ""}`;
  if (idempotencyKey && idempotencyStore.has(idemKey)) {
    return { data: idempotencyStore.get(idemKey), idempotent: true };
  }

  const quoteResult = buildCheckoutQuote(userId, {
    cartItemIds,
    addressId,
    shipmentType,
  });
  if (quoteResult.error) return quoteResult;

  const orderId = `o1000000-0000-4000-8000-${Date.now().toString().slice(-12)}`;
  const paymentId = `p1000000-0000-4000-8000-${Date.now().toString().slice(-11)}`;

  const isCod = paymentMethod === "COD";
  const isVnpay = paymentMethod === "VNPAY";
  const result = {
    order_id: orderId,
    payment_id: paymentId,
    payment_method: paymentMethod,
    payment_status: "PENDING",
    order_status: isCod ? "PROCESSING" : "AWAITING_PAYMENT",
    final_amount: quoteResult.data.final_amount,
    payos_checkout_url: null,
    redirect: isVnpay
      ? `https://mock.vnpay.local/checkout?order_id=${orderId}&payment_id=${paymentId}`
      : null,
  };

  if (idempotencyKey) {
    idempotencyStore.set(idemKey, result);
  }

  if (!isCod) {
    registerPaymentFromCheckout(userId, {
      payment_id: paymentId,
      order_id: orderId,
      payment_method: paymentMethod,
      final_amount: quoteResult.data.final_amount,
      order_status: result.order_status,
      payment_status: result.payment_status,
    });
  }

  const resolved = resolveCartItems(userId, cartItemIds);
  if (!resolved.error) {
    const previewItem = resolved.items[0];
    const itemCount = resolved.items.reduce((sum, item) => sum + item.quantity, 0);

    registerOrderFromCheckout(
      userId,
      {
        order_id: orderId,
        payment_id: paymentId,
        payment_method: paymentMethod,
        payment_status: result.payment_status,
        order_status: result.order_status,
        final_amount: result.final_amount,
      },
      {
        total_amount: quoteResult.data.total_amount,
        item_count: itemCount,
        preview_product_name: previewItem?.product_name,
        preview_image_url: previewItem?.image_url,
      }
    );
  }

  if (resolved.cart) {
    resolved.cart.items = resolved.cart.items.filter(
      (item) => !cartItemIds.includes(item.cart_item_id)
    );
    resolved.cart.updated_at = new Date().toISOString();
  }

  return { data: result };
}
