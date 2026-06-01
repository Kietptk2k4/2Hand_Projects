import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";
import { buildDetailFromSummary } from "./commerceOrderDetailData";
import { mockCommerceProducts } from "./commerceProductListData";
import { hasReviewForOrderItem } from "./commerceProductReviewIndex";

const VALID_ORDER_STATUSES = [
  "CREATED",
  "AWAITING_PAYMENT",
  "PROCESSING",
  "COMPLETED",
  "CANCELLED",
];

const ordersByUserId = new Map();

function daysAgoIso(days) {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date.toISOString();
}

function orderHasPendingReview(summary) {
  if (summary.order_status !== "COMPLETED") return false;
  const detail = buildDetailFromSummary(summary);
  return detail.items.some(
    (item) => item.status === "COMPLETED" && !hasReviewForOrderItem(item.order_item_id),
  );
}

function productPreview(index) {
  const product = mockCommerceProducts[index % mockCommerceProducts.length];
  return {
    preview_product_name: product.title,
    preview_image_url: product.thumbnail_url,
  };
}

function buildOrder({
  orderId,
  buyerId,
  orderStatus,
  orderPaymentStatus,
  paymentMethod,
  totalAmount,
  finalAmount,
  createdAt,
  itemCount = 1,
  productIndex = 0,
  payment = null,
  shipmentSummary = { shipment_count: 0, statuses: [] },
  completedAt = null,
}) {
  const preview = productPreview(productIndex);
  const now = new Date().toISOString();

  return {
    order_id: orderId,
    buyer_id: buyerId,
    order_status: orderStatus,
    order_payment_status: orderPaymentStatus,
    payment_method: paymentMethod,
    total_amount: totalAmount,
    final_amount: finalAmount,
    created_at: createdAt,
    updated_at: createdAt,
    completed_at: completedAt,
    item_count: itemCount,
    preview_product_name: preview.preview_product_name,
    preview_image_url: preview.preview_image_url,
    payment,
    shipment_summary: shipmentSummary,
    _sort_key: createdAt,
  };
}

function seedDemoOrders() {
  const userId = MOCK_CART_DEMO_USER_ID;
  const orders = [
    buildOrder({
      orderId: "o2000000-0000-4000-8000-000000000001",
      buyerId: userId,
      orderStatus: "AWAITING_PAYMENT",
      orderPaymentStatus: "PENDING",
      paymentMethod: "PAYOS",
      totalAmount: 4530000,
      finalAmount: 4560000,
      createdAt: daysAgoIso(1),
      itemCount: 2,
      productIndex: 0,
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000001",
        status: "PENDING",
        payment_method: "PAYOS",
        amount: 4560000,
        currency: "VND",
      },
      shipmentSummary: { shipment_count: 0, statuses: [] },
    }),
    buildOrder({
      orderId: "o2000000-0000-4000-8000-000000000002",
      buyerId: userId,
      orderStatus: "AWAITING_PAYMENT",
      orderPaymentStatus: "PENDING",
      paymentMethod: "PAYOS",
      totalAmount: 935000,
      finalAmount: 965000,
      createdAt: daysAgoIso(2),
      productIndex: 1,
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000002",
        status: "PENDING",
        payment_method: "PAYOS",
        amount: 965000,
        currency: "VND",
      },
    }),
    buildOrder({
      orderId: "o2000000-0000-4000-8000-000000000003",
      buyerId: userId,
      orderStatus: "PROCESSING",
      orderPaymentStatus: "PENDING",
      paymentMethod: "COD",
      totalAmount: 4500000,
      finalAmount: 4530000,
      createdAt: daysAgoIso(4),
      itemCount: 1,
      productIndex: 0,
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000003",
        status: "PENDING",
        payment_method: "COD",
        amount: 4530000,
        currency: "VND",
      },
      shipmentSummary: { shipment_count: 1, statuses: ["SHIPPED"] },
    }),
    buildOrder({
      orderId: "o2000000-0000-4000-8000-000000000004",
      buyerId: userId,
      orderStatus: "PROCESSING",
      orderPaymentStatus: "PAID",
      paymentMethod: "PAYOS",
      totalAmount: 3200000,
      finalAmount: 3230000,
      createdAt: daysAgoIso(5),
      itemCount: 3,
      productIndex: 2,
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000004",
        status: "PAID",
        payment_method: "PAYOS",
        amount: 3230000,
        currency: "VND",
      },
      shipmentSummary: { shipment_count: 1, statuses: ["SHIPPED"] },
    }),
    buildOrder({
      orderId: "o2000000-0000-4000-8000-000000000005",
      buyerId: userId,
      orderStatus: "COMPLETED",
      orderPaymentStatus: "PAID",
      paymentMethod: "PAYOS",
      totalAmount: 1100000,
      finalAmount: 1130000,
      createdAt: daysAgoIso(12),
      productIndex: 1,
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000005",
        status: "PAID",
        payment_method: "PAYOS",
        amount: 1130000,
        currency: "VND",
      },
      shipmentSummary: { shipment_count: 1, statuses: ["DELIVERED"] },
      completedAt: daysAgoIso(10),
    }),
    buildOrder({
      orderId: "o2000000-0000-4000-8000-000000000006",
      buyerId: userId,
      orderStatus: "COMPLETED",
      orderPaymentStatus: "PAID",
      paymentMethod: "COD",
      totalAmount: 850000,
      finalAmount: 880000,
      createdAt: daysAgoIso(20),
      productIndex: 3,
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000006",
        status: "PAID",
        payment_method: "COD",
        amount: 880000,
        currency: "VND",
      },
      shipmentSummary: { shipment_count: 1, statuses: ["DELIVERED"] },
      completedAt: daysAgoIso(18),
    }),
    buildOrder({
      orderId: "o2000000-0000-4000-8000-000000000007",
      buyerId: userId,
      orderStatus: "CANCELLED",
      orderPaymentStatus: "FAILED",
      paymentMethod: "PAYOS",
      totalAmount: 1500000,
      finalAmount: 1530000,
      createdAt: daysAgoIso(8),
      productIndex: 4,
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000007",
        status: "FAILED",
        payment_method: "PAYOS",
        amount: 1530000,
        currency: "VND",
      },
    }),
    buildOrder({
      orderId: "o2000000-0000-4000-8000-000000000008",
      buyerId: userId,
      orderStatus: "CREATED",
      orderPaymentStatus: "PENDING",
      paymentMethod: "PAYOS",
      totalAmount: 620000,
      finalAmount: 650000,
      createdAt: daysAgoIso(0),
      productIndex: 5,
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000008",
        status: "PENDING",
        payment_method: "PAYOS",
        amount: 650000,
        currency: "VND",
      },
    }),
  ];

  ordersByUserId.set(userId, orders);
}

seedDemoOrders();

export function registerOrderFromCheckout(userId, checkoutResult, previewMeta = {}) {
  if (!userId || !checkoutResult?.order_id) return null;

  const list = ordersByUserId.get(userId) || [];
  const isCod = checkoutResult.payment_method === "COD";
  const now = new Date().toISOString();

  const order = buildOrder({
    orderId: checkoutResult.order_id,
    buyerId: userId,
    orderStatus: checkoutResult.order_status,
    orderPaymentStatus: checkoutResult.payment_status,
    paymentMethod: checkoutResult.payment_method,
    totalAmount: previewMeta.total_amount ?? checkoutResult.final_amount,
    finalAmount: checkoutResult.final_amount,
    createdAt: now,
    itemCount: previewMeta.item_count ?? 1,
    productIndex: 0,
    payment: checkoutResult.payment_id
      ? {
          payment_id: checkoutResult.payment_id,
          status: checkoutResult.payment_status,
          payment_method: checkoutResult.payment_method,
          amount: checkoutResult.final_amount,
          currency: "VND",
        }
      : null,
    shipmentSummary: isCod
      ? { shipment_count: 1, statuses: ["SHIPPED"] }
      : { shipment_count: 0, statuses: [] },
  });

  if (previewMeta.preview_product_name) {
    order.preview_product_name = previewMeta.preview_product_name;
  }
  if (previewMeta.preview_image_url) {
    order.preview_image_url = previewMeta.preview_image_url;
  }

  const existingIndex = list.findIndex((item) => item.order_id === order.order_id);
  if (existingIndex >= 0) {
    list[existingIndex] = order;
  } else {
    list.unshift(order);
  }

  ordersByUserId.set(userId, list);
  return order;
}

export function validateOrderListQuery({ page, limit, status }) {
  const pageNum = Number(page);
  const limitNum = Number(limit);

  if (!Number.isInteger(pageNum) || pageNum < 1) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (!Number.isInteger(limitNum) || limitNum < 1 || limitNum > 50) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (status && !VALID_ORDER_STATUSES.includes(status)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  return { page: pageNum, limit: limitNum, status: status || null };
}

export function findOrderSummaryForUser(userId, orderId) {
  const list = ordersByUserId.get(userId) || [];
  return list.find((order) => order.order_id === orderId) || null;
}

export function getOrdersForUser(userId, { page, limit, status }) {
  let list = [...(ordersByUserId.get(userId) || [])];

  if (status) {
    list = list.filter((order) => order.order_status === status);
  }

  list.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));

  const totalItems = list.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / limit) || 1);
  const start = (page - 1) * limit;
  const slice = list.slice(start, start + limit).map((order) => {
    const { buyer_id: _buyerId, _sort_key: _sortKey, ...rest } = order;
    return {
      ...rest,
      pending_review: orderHasPendingReview(order),
    };
  });

  return {
    orders: slice,
    pagination: {
      page,
      limit,
      total_items: totalItems,
      total_pages: totalPages,
      has_next: page < totalPages,
    },
  };
}
