import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";
import { MOCK_DEMO_SELLER_SHOP_ID, getShopBySellerId } from "./commerceSellerShopData";

/** QA — demo seller (active@2hands.vn) order item IDs */
export const MOCK_SELLER_ORDER_ITEM_PENDING = "so2000000-0000-4000-8000-000000000001";
export const MOCK_SELLER_ORDER_ITEM_PROCESSING = "so2000000-0000-4000-8000-000000000002";
export const MOCK_SELLER_ORDER_ITEM_SHIPPED = "so2000000-0000-4000-8000-000000000003";
export const MOCK_SELLER_ORDER_ITEM_DELIVERED = "so2000000-0000-4000-8000-000000000004";
export const MOCK_SELLER_ORDER_ITEM_COMPLETED = "so2000000-0000-4000-8000-000000000005";
/** PENDING + COD + order PROCESSING — process success */
export const MOCK_SELLER_ORDER_ITEM_PENDING_COD = "so2000000-0000-4000-8000-000000000013";
/** PENDING + PayOS + payment PENDING — 409 PAYMENT-STATE */
export const MOCK_SELLER_ORDER_ITEM_PENDING_PAYOS_UNPAID = "so2000000-0000-4000-8000-000000000014";
/** PENDING + order AWAITING_PAYMENT — 409 ORDER-PROCESSING */
export const MOCK_SELLER_ORDER_ITEM_PENDING_ORDER_NOT_READY = "so2000000-0000-4000-8000-000000000015";

const VALID_ITEM_STATUSES = [
  "PENDING",
  "PROCESSING",
  "SHIPPED",
  "DELIVERED",
  "COMPLETED",
  "CANCELLED",
  "FAILED",
  "RETURNED",
];

const VALID_SHIPMENT_STATUSES = [
  "PENDING",
  "PICKING_UP",
  "READY_TO_SHIP",
  "SHIPPED",
  "DELIVERED",
  "FAILED",
  "CANCELLED",
  "RETURNED",
];

const sellerOrderItems = [];

function daysAgoIso(days) {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date.toISOString();
}

function buildSellerItem(overrides) {
  const created = overrides.item_created_at || daysAgoIso(1);
  return {
    seller_id: MOCK_CART_DEMO_USER_ID,
    shop_id: MOCK_DEMO_SELLER_SHOP_ID,
    quantity: 1,
    unit_price_snapshot: 890000,
    final_price: 890000,
    shipping_fee_allocated: 30000,
    image_snapshot: "https://picsum.photos/seed/seller-order-item/80/80",
    item_created_at: created,
    item_updated_at: overrides.item_updated_at || created,
    order_status: "PROCESSING",
    order_payment_status: "PAID",
    order_payment_method: "PAYOS",
    order_created_at: created,
    payment: {
      payment_id: "p2000000-0000-4000-8000-000000000099",
      status: "PAID",
      payment_method: "PAYOS",
      amount: 920000,
      currency: "VND",
    },
    shipment_summary: null,
    ...overrides,
  };
}

function seedSellerOrderItems() {
  if (sellerOrderItems.length > 0) return;

  const seeds = [
    buildSellerItem({
      order_item_id: MOCK_SELLER_ORDER_ITEM_PENDING,
      order_id: "o2000000-0000-4000-8000-000000000101",
      product_id: "c2000000-0000-4000-8000-000000000102",
      product_name_snapshot: "Bộ tuốc lực điện demo",
      item_status: "PENDING",
      order_status: "PROCESSING",
      order_payment_status: "PAID",
      item_created_at: daysAgoIso(0),
      final_price: 1100000,
      unit_price_snapshot: 1100000,
    }),
    buildSellerItem({
      order_item_id: MOCK_SELLER_ORDER_ITEM_PROCESSING,
      order_id: "o2000000-0000-4000-8000-000000000102",
      product_id: "c2000000-0000-4000-8000-000000000101",
      product_name_snapshot: "Ao khoac denim demo — sap het hang",
      item_status: "PROCESSING",
      order_payment_method: "COD",
      order_payment_status: "PENDING",
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000102",
        status: "PENDING",
        payment_method: "COD",
        amount: 920000,
        currency: "VND",
      },
      item_created_at: daysAgoIso(1),
      final_price: 890000,
      shipment_summary: {
        shipment_id: "sh-seller-000102",
        status: "READY_TO_SHIP",
        carrier: "GHN",
        tracking_number: null,
        delivery_address_summary: "Quận 7, TP. Hồ Chí Minh",
      },
    }),
    buildSellerItem({
      order_item_id: MOCK_SELLER_ORDER_ITEM_SHIPPED,
      order_id: "o2000000-0000-4000-8000-000000000103",
      product_id: "c2000000-0000-4000-8000-000000000102",
      product_name_snapshot: "Bộ tuốc lực điện demo",
      item_status: "SHIPPED",
      item_created_at: daysAgoIso(3),
      final_price: 1100000,
      shipment_summary: {
        shipment_id: "sh-seller-000103",
        status: "SHIPPED",
        carrier: "GHN",
        tracking_number: "GHN1029384756",
        delivery_address_summary: "Quận 1, TP. Hồ Chí Minh",
      },
    }),
    buildSellerItem({
      order_item_id: MOCK_SELLER_ORDER_ITEM_DELIVERED,
      order_id: "o2000000-0000-4000-8000-000000000104",
      product_id: "c2000000-0000-4000-8000-000000000101",
      product_name_snapshot: "Ao khoac denim demo",
      item_status: "DELIVERED",
      item_created_at: daysAgoIso(5),
      final_price: 890000,
      shipment_summary: {
        shipment_id: "sh-seller-000104",
        status: "DELIVERED",
        carrier: "GHTK",
        tracking_number: "GHTK8877665544",
        delivery_address_summary: "Quận Bình Thạnh, TP. Hồ Chí Minh",
      },
    }),
    buildSellerItem({
      order_item_id: MOCK_SELLER_ORDER_ITEM_COMPLETED,
      order_id: "o2000000-0000-4000-8000-000000000105",
      product_id: "c2000000-0000-4000-8000-000000000102",
      product_name_snapshot: "Bộ tuốc lực điện demo",
      item_status: "COMPLETED",
      order_status: "COMPLETED",
      item_created_at: daysAgoIso(12),
      final_price: 1100000,
      shipment_summary: {
        shipment_id: "sh-seller-000105",
        status: "DELIVERED",
        carrier: "GHN",
        tracking_number: "GHN5544332211",
        delivery_address_summary: "Quận 3, TP. Hồ Chí Minh",
      },
    }),
    buildSellerItem({
      order_item_id: "so2000000-0000-4000-8000-000000000006",
      order_id: "o2000000-0000-4000-8000-000000000106",
      product_id: "c2000000-0000-4000-8000-000000000103",
      product_name_snapshot: "Găng tay chống cắt demo",
      item_status: "CANCELLED",
      order_status: "CANCELLED",
      order_payment_status: "FAILED",
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000106",
        status: "FAILED",
        payment_method: "PAYOS",
        amount: 95000,
        currency: "VND",
      },
      item_created_at: daysAgoIso(8),
      final_price: 95000,
      unit_price_snapshot: 95000,
    }),
    buildSellerItem({
      order_item_id: "so2000000-0000-4000-8000-000000000007",
      order_id: "o2000000-0000-4000-8000-000000000107",
      product_id: "c2000000-0000-4000-8000-000000000102",
      product_name_snapshot: "Bộ tuốc lực điện — đơn 2",
      item_status: "PENDING",
      quantity: 2,
      final_price: 2200000,
      unit_price_snapshot: 1100000,
      item_created_at: daysAgoIso(2),
    }),
    buildSellerItem({
      order_item_id: "so2000000-0000-4000-8000-000000000008",
      order_id: "o2000000-0000-4000-8000-000000000108",
      product_id: "c2000000-0000-4000-8000-000000000101",
      product_name_snapshot: "Ao khoac denim — chua tao van chuyen",
      item_status: "PROCESSING",
      item_created_at: daysAgoIso(4),
      final_price: 890000,
      shipment_summary: null,
    }),
    buildSellerItem({
      order_item_id: "so2000000-0000-4000-8000-000000000009",
      order_id: "o2000000-0000-4000-8000-000000000109",
      product_id: "c2000000-0000-4000-8000-000000000108",
      product_name_snapshot: "Cờ lê điện nháp",
      item_status: "PENDING",
      item_created_at: daysAgoIso(1),
      final_price: 320000,
      unit_price_snapshot: 320000,
      image_snapshot: "https://picsum.photos/seed/seller-demo-108/80/80",
    }),
    buildSellerItem({
      order_item_id: "so2000000-0000-4000-8000-000000000010",
      order_id: "o2000000-0000-4000-8000-000000000110",
      product_id: "c2000000-0000-4000-8000-000000000101",
      product_name_snapshot: "Ao khoac denim — dang lay hang",
      item_status: "PROCESSING",
      item_created_at: daysAgoIso(6),
      final_price: 890000,
      shipment_summary: {
        shipment_id: "sh-seller-000110",
        status: "PENDING",
        carrier: "MANUAL",
        tracking_number: "MAN-LOCAL-001",
        delivery_address_summary: "Quận 10, TP. Hồ Chí Minh",
      },
    }),
    buildSellerItem({
      order_item_id: "so2000000-0000-4000-8000-000000000011",
      order_id: "o2000000-0000-4000-8000-000000000111",
      product_id: "c2000000-0000-4000-8000-000000000102",
      product_name_snapshot: "Bộ tuốc lực — trả hàng",
      item_status: "RETURNED",
      item_created_at: daysAgoIso(15),
      final_price: 1100000,
      shipment_summary: {
        shipment_id: "sh-seller-000111",
        status: "RETURNED",
        carrier: "GHN",
        tracking_number: "GHN9988776655",
        delivery_address_summary: "Quận 7, TP. Hồ Chí Minh",
      },
    }),
    buildSellerItem({
      order_item_id: "so2000000-0000-4000-8000-000000000012",
      order_id: "o2000000-0000-4000-8000-000000000112",
      product_id: "c2000000-0000-4000-8000-000000000101",
      product_name_snapshot: "Ao khoac denim — that bai",
      item_status: "FAILED",
      order_payment_status: "FAILED",
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000112",
        status: "FAILED",
        payment_method: "PAYOS",
        amount: 890000,
        currency: "VND",
      },
      item_created_at: daysAgoIso(9),
      final_price: 890000,
    }),
    buildSellerItem({
      order_item_id: MOCK_SELLER_ORDER_ITEM_PENDING_COD,
      order_id: "o2000000-0000-4000-8000-000000000113",
      product_id: "c2000000-0000-4000-8000-000000000108",
      product_name_snapshot: "Cờ lê điện — COD chờ chuẩn bị",
      item_status: "PENDING",
      order_status: "PROCESSING",
      order_payment_method: "COD",
      order_payment_status: "PENDING",
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000113",
        status: "PENDING",
        payment_method: "COD",
        amount: 350000,
        currency: "VND",
      },
      item_created_at: daysAgoIso(0),
      final_price: 320000,
    }),
    buildSellerItem({
      order_item_id: MOCK_SELLER_ORDER_ITEM_PENDING_PAYOS_UNPAID,
      order_id: "o2000000-0000-4000-8000-000000000114",
      product_id: "c2000000-0000-4000-8000-000000000102",
      product_name_snapshot: "Bộ tuốc lực — PayOS chưa thanh toán",
      item_status: "PENDING",
      order_status: "PROCESSING",
      order_payment_method: "PAYOS",
      order_payment_status: "PENDING",
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000114",
        status: "PENDING",
        payment_method: "PAYOS",
        amount: 1100000,
        currency: "VND",
      },
      item_created_at: daysAgoIso(0),
      final_price: 1100000,
    }),
    buildSellerItem({
      order_item_id: MOCK_SELLER_ORDER_ITEM_PENDING_ORDER_NOT_READY,
      order_id: "o2000000-0000-4000-8000-000000000115",
      product_id: "c2000000-0000-4000-8000-000000000101",
      product_name_snapshot: "Ao khoac denim — don chua san sang xu ly",
      item_status: "PENDING",
      order_status: "AWAITING_PAYMENT",
      order_payment_method: "PAYOS",
      order_payment_status: "PENDING",
      payment: {
        payment_id: "p2000000-0000-4000-8000-000000000115",
        status: "PENDING",
        payment_method: "PAYOS",
        amount: 890000,
        currency: "VND",
      },
      item_created_at: daysAgoIso(0),
      final_price: 890000,
    }),
  ];

  sellerOrderItems.push(...seeds);
}

seedSellerOrderItems();

export function validateSellerOrderListQuery({ page, limit, status, shipment_status }) {
  const pageNum = Number(page);
  const limitNum = Number(limit);

  if (!Number.isInteger(pageNum) || pageNum < 1) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (!Number.isInteger(limitNum) || limitNum < 1 || limitNum > 50) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (status && !VALID_ITEM_STATUSES.includes(status)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  if (shipment_status && !VALID_SHIPMENT_STATUSES.includes(shipment_status)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  return {
    page: pageNum,
    limit: limitNum,
    status: status || null,
    shipment_status: shipment_status || null,
  };
}

function toListRow(record) {
  return {
    order_item_id: record.order_item_id,
    order_id: record.order_id,
    product_id: record.product_id,
    quantity: record.quantity,
    unit_price_snapshot: record.unit_price_snapshot,
    final_price: record.final_price,
    shipping_fee_allocated: record.shipping_fee_allocated,
    product_name_snapshot: record.product_name_snapshot,
    image_snapshot: record.image_snapshot,
    item_status: record.item_status,
    item_created_at: record.item_created_at,
    item_updated_at: record.item_updated_at,
    order_status: record.order_status,
    order_payment_status: record.order_payment_status,
    order_payment_method: record.order_payment_method,
    order_created_at: record.order_created_at,
    payment: record.payment,
    shipment_summary: record.shipment_summary,
  };
}

export function findSellerOrderItem(userId, orderItemId) {
  const record = sellerOrderItems.find(
    (row) => row.order_item_id === orderItemId && row.seller_id === userId,
  );
  return record || null;
}

export function getAllSellerOrderItemRecords(userId) {
  return sellerOrderItems.filter((row) => row.seller_id === userId);
}

export function findSellerOrderItemsByIds(userId, orderItemIds) {
  return (orderItemIds || [])
    .map((id) => findSellerOrderItem(userId, id))
    .filter(Boolean);
}

export function attachShipmentToOrderItems(userId, orderItemIds, shipmentId, shipmentSummary) {
  const now = new Date().toISOString();
  for (const id of orderItemIds) {
    const record = findSellerOrderItem(userId, id);
    if (!record) continue;
    record.shipment_summary = {
      ...shipmentSummary,
      shipment_id: shipmentId,
    };
    record.item_updated_at = now;
  }
}

export function setSellerOrderItemsStatus(userId, orderItemIds, status) {
  const now = new Date().toISOString();
  for (const id of orderItemIds) {
    const record = findSellerOrderItem(userId, id);
    if (!record) continue;
    record.item_status = status;
    record.item_updated_at = now;
  }
}

export function countPendingSellerOrderItems(userId) {
  return sellerOrderItems.filter(
    (row) => row.seller_id === userId && row.item_status === "PENDING",
  ).length;
}

export function processSellerOrderItemsForUser(userId, orderItemIds) {
  const shop = getShopBySellerId(userId);
  if (!shop) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409, message: "Seller chua co shop." };
  }

  if (!Array.isArray(orderItemIds) || orderItemIds.length === 0) {
    return {
      error: "COMMERCE-400-VALIDATION",
      status: 400,
      message: "order_item_ids khong duoc trong.",
    };
  }

  const uniqueIds = [...new Set(orderItemIds.map((id) => String(id).trim()).filter(Boolean))];
  if (uniqueIds.length !== orderItemIds.length) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const records = [];
  for (const id of uniqueIds) {
    const record = findSellerOrderItem(userId, id);
    if (!record) {
      return { error: "COMMERCE-404-ORDER-ITEM", status: 404 };
    }
    records.push(record);
  }

  for (const record of records) {
    if (record.order_status !== "PROCESSING") {
      return { error: "COMMERCE-409-ORDER-PROCESSING", status: 409 };
    }

    const method = record.order_payment_method || record.payment?.payment_method;
    if (method === "PAYOS" && record.order_payment_status !== "PAID") {
      return { error: "COMMERCE-409-PAYMENT-STATE", status: 409 };
    }

    if (!["PENDING", "PROCESSING"].includes(record.item_status)) {
      return { error: "COMMERCE-409-ORDER-ITEM-PROCESS", status: 409 };
    }
  }

  const now = new Date().toISOString();
  let newlyProcessedCount = 0;
  let alreadyProcessingCount = 0;
  const items = [];

  for (const record of records) {
    const wasPending = record.item_status === "PENDING";
    if (wasPending) {
      record.item_status = "PROCESSING";
      record.item_updated_at = now;
      newlyProcessedCount += 1;
    } else {
      alreadyProcessingCount += 1;
    }

    items.push({
      order_item_id: record.order_item_id,
      order_id: record.order_id,
      status: record.item_status,
      product_name_snapshot: record.product_name_snapshot,
      quantity: record.quantity,
      newly_processed: wasPending,
    });
  }

  return {
    data: {
      items,
      newly_processed_count: newlyProcessedCount,
      already_processing_count: alreadyProcessingCount,
      processed_at: now,
    },
  };
}

/** Keep seller rows aligned when buyer payment fails / auto-cancel (no new FE API). */
export function syncSellerItemsForBuyerOrderStatus(
  orderId,
  { orderStatus, orderPaymentStatus, paymentStatus, itemStatus = "CANCELLED" },
) {
  const now = new Date().toISOString();
  for (const row of sellerOrderItems) {
    if (row.order_id !== orderId) continue;
    row.order_status = orderStatus;
    row.order_payment_status = orderPaymentStatus;
    row.item_status = itemStatus;
    row.item_updated_at = now;
    if (row.payment) {
      row.payment = { ...row.payment, status: paymentStatus };
    }
    if (row.shipment_summary) {
      row.shipment_summary = {
        ...row.shipment_summary,
        status: "CANCELLED",
      };
    }
  }
}

export function listSellerOrdersForUser(userId, { page, limit, status, shipment_status }) {
  const shop = getShopBySellerId(userId);
  if (!shop) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409, message: "Seller chua co shop." };
  }

  let items = sellerOrderItems.filter((row) => row.seller_id === userId);

  if (status) {
    items = items.filter((row) => row.item_status === status);
  }

  if (shipment_status) {
    items = items.filter((row) => row.shipment_summary?.status === shipment_status);
  }

  items.sort((a, b) => new Date(b.item_created_at) - new Date(a.item_created_at));

  const totalItems = items.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / limit) || 1);
  const start = (page - 1) * limit;
  const slice = items.slice(start, start + limit).map(toListRow);

  return {
    data: {
      items: slice,
      pagination: {
        page,
        limit,
        total_items: totalItems,
        total_pages: totalPages,
        has_next: page < totalPages,
      },
      summary: {
        pending_count: countPendingSellerOrderItems(userId),
      },
    },
  };
}
