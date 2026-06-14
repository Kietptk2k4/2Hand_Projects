import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";
import { getAddressesForUser } from "./commerceAddressData";
import { getShopBySellerId } from "./commerceSellerShopData";
import {
  attachShipmentToOrderItems,
  findSellerOrderItem,
  findSellerOrderItemsByIds,
  getAllSellerOrderItemRecords,
  MOCK_SELLER_ORDER_BUYER_PROFILE,
  setSellerOrderItemsStatus,
} from "./commerceSellerOrderData";

export const MOCK_SELLER_SHIPMENT_GHN = "sh-seller-000102";
export const MOCK_SELLER_SHIPMENT_MANUAL = "sh-seller-000110";

const VALID_CARRIERS = ["GHN", "MANUAL", "SELF_DELIVERY"];
const VALID_TYPES = ["STANDARD", "EXPRESS", "SAME_DAY"];
const VALID_STATUSES = [
  "PENDING",
  "PICKING_UP",
  "READY_TO_SHIP",
  "SHIPPED",
  "DELIVERED",
  "FAILED",
  "CANCELLED",
  "RETURNED",
];

const MANUAL_TRANSITIONS = {
  PENDING: ["READY_TO_SHIP"],
  READY_TO_SHIP: ["SHIPPED"],
  SHIPPED: ["DELIVERED", "FAILED"],
};

const shipmentsById = new Map();
const trackingNumbersInUse = new Set();

function generateShipmentId() {
  const segment = () =>
    Math.floor(Math.random() * 0x10000)
      .toString(16)
      .padStart(4, "0");
  return `sh-seller-${segment()}${segment()}`;
}

function getBuyerShippingAddress() {
  const list = getAddressesForUser(MOCK_CART_DEMO_USER_ID);
  const addr = list.find((a) => a.is_default) || list[0];
  if (!addr) return null;

  return {
    receiver_name: addr.receiver_name,
    phone: addr.phone,
    province_code: addr.province_code,
    district_code: addr.district_code,
    ward_code: addr.ward_code,
    address_detail: addr.address_detail,
    full_address: `${addr.address_detail}, Phường, Quận, TP.`,
  };
}

function itemStatusForShipmentStatus(shipmentStatus) {
  if (shipmentStatus === "SHIPPED") return "SHIPPED";
  if (shipmentStatus === "DELIVERED") return "DELIVERED";
  return "PROCESSING";
}

function buildShipmentSummaryFromRecord(shipment) {
  return {
    shipment_id: shipment.shipment_id,
    status: shipment.status,
    carrier: shipment.carrier,
    tracking_number: shipment.tracking_number,
    delivery_address_summary: shipment.shipping_address?.full_address?.slice(0, 80) || "",
  };
}

function registerShipment(record) {
  shipmentsById.set(record.shipment_id, record);
  if (record.tracking_number) {
    trackingNumbersInUse.add(record.tracking_number);
  }
}

function seedShipmentsFromOrderItems() {
  if (shipmentsById.size > 0) return;

  const grouped = new Map();
  for (const item of getAllSellerOrderItemRecords(MOCK_CART_DEMO_USER_ID)) {
    const sid = item.shipment_summary?.shipment_id;
    if (!sid) continue;
    if (!grouped.has(sid)) grouped.set(sid, []);
    grouped.get(sid).push(item);
  }

  const defaultAddress = getBuyerShippingAddress();

  for (const [shipmentId, items] of grouped) {
    const first = items[0];
    const summary = first.shipment_summary;
    const created = first.item_created_at;

    registerShipment({
      shipment_id: shipmentId,
      order_id: first.order_id,
      seller_id: first.seller_id,
      shop_id: first.shop_id,
      carrier: summary.carrier || "GHN",
      shipment_type: "STANDARD",
      status: summary.status || "PENDING",
      ghn_order_code:
        summary.carrier === "GHN"
          ? `GHN-MOCK-${shipmentId.slice(-8).toUpperCase()}`
          : null,
      tracking_number:
        summary.tracking_number ||
        (summary.carrier === "GHN" ? `GHN${shipmentId.replace(/\D/g, "").slice(-10)}` : null),
      shipping_fee: 30000,
      cod_amount: first.order_payment_method === "COD" ? first.final_price : 0,
      weight_gram: 800,
      estimated_delivery_date: new Date(Date.now() + 3 * 86400000).toISOString().slice(0, 10),
      shipped_at: ["SHIPPED", "DELIVERED"].includes(summary.status) ? created : null,
      delivered_at: summary.status === "DELIVERED" ? created : null,
      created_at: created,
      updated_at: first.item_updated_at || created,
      shipping_address: defaultAddress,
      order_item_ids: items.map((i) => i.order_item_id),
    });
  }
}

seedShipmentsFromOrderItems();

function toListRow(record) {
  return {
    shipment_id: record.shipment_id,
    order_id: record.order_id,
    carrier: record.carrier,
    shipment_type: record.shipment_type,
    status: record.status,
    tracking_number: record.tracking_number,
    ghn_order_code: record.ghn_order_code,
    delivery_address_summary: record.shipping_address?.full_address || "",
    created_at: record.created_at,
    updated_at: record.updated_at,
    order_item_count: record.order_item_ids?.length ?? 0,
  };
}

function toDetailResponse(record) {
  const items = findSellerOrderItemsByIds(record.seller_id, record.order_item_ids);
  return {
    shipment_id: record.shipment_id,
    order_id: record.order_id,
    seller_id: record.seller_id,
    carrier: record.carrier,
    shipment_type: record.shipment_type,
    status: record.status,
    ghn_order_code: record.ghn_order_code,
    tracking_number: record.tracking_number,
    shipping_fee: record.shipping_fee,
    cod_amount: record.cod_amount,
    weight_gram: record.weight_gram,
    estimated_delivery_date: record.estimated_delivery_date,
    shipped_at: record.shipped_at,
    delivered_at: record.delivered_at,
    created_at: record.created_at,
    updated_at: record.updated_at,
    shipping_address: record.shipping_address,
    order_items: items.map((item) => ({
      order_item_id: item.order_item_id,
      product_name_snapshot: item.product_name_snapshot,
      quantity: item.quantity,
      status: item.item_status,
    })),
    ...MOCK_SELLER_ORDER_BUYER_PROFILE,
  };
}

export function validateSellerShipmentListQuery({ page, limit, status, q }) {
  const pageNum = Number(page);
  const limitNum = Number(limit);

  if (!Number.isInteger(pageNum) || pageNum < 1) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (!Number.isInteger(limitNum) || limitNum < 1 || limitNum > 50) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (status && !VALID_STATUSES.includes(status)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  return { page: pageNum, limit: limitNum, status: status || null, q: q || null };
}

export function countShipmentsByStatus(userId) {
  const counts = {};
  for (const status of ["PENDING", "READY_TO_SHIP", "SHIPPED", "DELIVERED", "FAILED"]) {
    counts[status] = 0;
  }

  for (const record of shipmentsById.values()) {
    if (record.seller_id !== userId) continue;
    if (counts[record.status] != null) counts[record.status] += 1;
  }

  return counts;
}

/** FE-only GET list — không có trong backend doc */
export function listSellerShipmentsForUser(userId, { page, limit, status, q }) {
  const shop = getShopBySellerId(userId);
  if (!shop) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409 };
  }

  let items = [...shipmentsById.values()].filter((row) => row.seller_id === userId);

  if (status) {
    items = items.filter((row) => row.status === status);
  }

  if (q) {
    const needle = String(q).trim().toLowerCase();
    if (needle) {
      items = items.filter(
        (row) =>
          row.shipment_id.toLowerCase().includes(needle) ||
          row.order_id.toLowerCase().includes(needle) ||
          (row.tracking_number && row.tracking_number.toLowerCase().includes(needle)) ||
          (row.ghn_order_code && row.ghn_order_code.toLowerCase().includes(needle)),
      );
    }
  }

  items.sort((a, b) => new Date(b.updated_at) - new Date(a.updated_at));

  const total = items.length;
  const totalPages = Math.max(1, Math.ceil(total / limit) || 1);
  const start = (page - 1) * limit;

  return {
    data: {
      items: items.slice(start, start + limit).map(toListRow),
      pagination: {
        page,
        limit,
        total_items: total,
        total_pages: totalPages,
        has_next: page < totalPages,
      },
      summary: {
        status_counts: countShipmentsByStatus(userId),
      },
    },
  };
}

export function getSellerShipmentForUser(userId, shipmentId) {
  const record = shipmentsById.get(shipmentId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-SHIPMENT", status: 404 };
  }
  return { data: toDetailResponse(record) };
}

export function createShipmentForSeller(userId, body) {
  const shop = getShopBySellerId(userId);
  if (!shop) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409 };
  }

  const orderId = body?.order_id;
  const orderItemIds = body?.order_item_ids;
  const carrier = body?.carrier;
  const shipmentType = body?.shipment_type;

  if (!orderId || !Array.isArray(orderItemIds) || orderItemIds.length === 0) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  if (!VALID_CARRIERS.includes(carrier)) {
    return { error: "COMMERCE-400-SHIPMENT-CARRIER", status: 400 };
  }

  if (!VALID_TYPES.includes(shipmentType)) {
    return { error: "COMMERCE-400-SHIPMENT-TYPE", status: 400 };
  }

  const items = findSellerOrderItemsByIds(userId, orderItemIds);
  if (items.length !== orderItemIds.length) {
    return { error: "COMMERCE-404-ORDER-ITEM", status: 404 };
  }

  if (items.some((item) => item.order_id !== orderId)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const first = items[0];
  if (first.order_status !== "PROCESSING") {
    return { error: "COMMERCE-409-ORDER-PROCESSING", status: 409 };
  }

  const method = first.order_payment_method;
  if (method === "PAYOS" && first.order_payment_status !== "PAID") {
    return { error: "COMMERCE-409-PAYMENT-STATE", status: 409 };
  }

  if (items.some((item) => item.item_status !== "PROCESSING")) {
    return { error: "COMMERCE-409-ORDER-ITEM-PROCESS", status: 409 };
  }

  if (items.some((item) => item.shipment_summary?.shipment_id)) {
    return { error: "COMMERCE-409-ORDER-ITEM-SHIPPED", status: 409 };
  }

  const shippingAddress = getBuyerShippingAddress();
  if (!shippingAddress) {
    return { error: "COMMERCE-404-BUYER-ADDRESS", status: 404 };
  }

  const now = new Date().toISOString();
  const shipmentId = generateShipmentId();
  const trackingNumber =
    body.tracking_number?.trim() ||
    (carrier === "GHN" ? `GHN${shipmentId.replace(/\D/g, "").slice(-10)}` : null);

  if (trackingNumber && trackingNumbersInUse.has(trackingNumber)) {
    return { error: "COMMERCE-409-TRACKING", status: 409 };
  }

  const record = {
    shipment_id: shipmentId,
    order_id: orderId,
    seller_id: userId,
    shop_id: shop.shop_id,
    carrier,
    shipment_type: shipmentType,
    status: "PENDING",
    ghn_order_code: carrier === "GHN" ? `GHN-MOCK-${shipmentId.slice(-8).toUpperCase()}` : null,
    tracking_number: trackingNumber,
    shipping_fee: 30000,
    cod_amount: method === "COD" ? items.reduce((s, i) => s + i.final_price, 0) : 0,
    weight_gram: Number(body.weight_gram) || items.length * 500,
    estimated_delivery_date: new Date(Date.now() + 3 * 86400000).toISOString().slice(0, 10),
    shipped_at: null,
    delivered_at: null,
    created_at: now,
    updated_at: now,
    shipping_address: shippingAddress,
    order_item_ids: [...orderItemIds],
  };

  registerShipment(record);

  const summary = buildShipmentSummaryFromRecord(record);
  attachShipmentToOrderItems(userId, orderItemIds, shipmentId, summary);

  return {
    data: {
      shipment_id: shipmentId,
      order_id: orderId,
      seller_id: userId,
      carrier,
      shipment_type: shipmentType,
      status: "PENDING",
      ghn_order_code: record.ghn_order_code,
      tracking_number: record.tracking_number,
      shipping_fee: record.shipping_fee,
      cod_amount: record.cod_amount,
      weight_gram: record.weight_gram,
      estimated_delivery_date: record.estimated_delivery_date,
      order_item_ids: orderItemIds,
      created_at: now,
    },
  };
}

function applyShipmentStatusToItems(userId, record, newStatus) {
  const itemStatus = itemStatusForShipmentStatus(newStatus);
  setSellerOrderItemsStatus(userId, record.order_item_ids, itemStatus);

  const summary = buildShipmentSummaryFromRecord({ ...record, status: newStatus });
  attachShipmentToOrderItems(userId, record.order_item_ids, record.shipment_id, summary);
}

function canCancelShipment(carrier, status) {
  if (carrier === "GHN") {
    return ["PENDING", "PICKING_UP", "READY_TO_SHIP"].includes(status);
  }
  if (carrier === "MANUAL" || carrier === "SELF_DELIVERY") {
    return ["PENDING", "READY_TO_SHIP"].includes(status);
  }
  return false;
}

function releaseOrderItemsFromCancelledShipment(userId, orderItemIds) {
  const now = new Date().toISOString();
  for (const id of orderItemIds) {
    const item = findSellerOrderItem(userId, id);
    if (!item) continue;
    item.shipment_summary = null;
    item.item_status = "PROCESSING";
    item.item_updated_at = now;
  }
}

export function cancelSellerShipmentForSeller(userId, shipmentId) {
  const record = shipmentsById.get(shipmentId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-SHIPMENT", status: 404 };
  }

  if (!canCancelShipment(record.carrier, record.status)) {
    return { error: "COMMERCE-409-SHIPMENT-STATUS", status: 409 };
  }

  const now = new Date().toISOString();
  record.status = "CANCELLED";
  record.updated_at = now;
  releaseOrderItemsFromCancelledShipment(userId, record.order_item_ids);

  return { data: toDetailResponse(record) };
}

export function updateSellerShipmentForSeller(userId, shipmentId, body) {
  const record = shipmentsById.get(shipmentId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-SHIPMENT", status: 404 };
  }

  if (record.carrier === "GHN") {
    return { error: "COMMERCE-409-SHIPMENT-CARRIER", status: 409 };
  }

  if (["DELIVERED", "FAILED", "CANCELLED", "RETURNED"].includes(record.status)) {
    return { error: "COMMERCE-409-SHIPMENT-STATUS", status: 409 };
  }

  const hasStatus = body?.status != null && body.status !== "";
  const hasTracking = body?.tracking_number != null && body.tracking_number !== "";

  if (!hasStatus && !hasTracking) {
    return { error: "COMMERCE-400", status: 400 };
  }

  const now = new Date().toISOString();

  if (hasTracking) {
    const tracking = String(body.tracking_number).trim();
    if (!tracking) {
      return { error: "COMMERCE-400-VALIDATION", status: 400 };
    }
    if (tracking !== record.tracking_number && trackingNumbersInUse.has(tracking)) {
      return { error: "COMMERCE-409-TRACKING", status: 409 };
    }
    if (record.tracking_number) {
      trackingNumbersInUse.delete(record.tracking_number);
    }
    record.tracking_number = tracking;
    trackingNumbersInUse.add(tracking);
  }

  if (hasStatus) {
    const next = body.status;
    const allowed = MANUAL_TRANSITIONS[record.status] || [];
    if (!allowed.includes(next)) {
      return { error: "COMMERCE-409-SHIPMENT-STATUS", status: 409 };
    }
    record.status = next;
    if (next === "SHIPPED" && !record.shipped_at) {
      record.shipped_at = now;
    }
    if (next === "DELIVERED") {
      record.delivered_at = now;
    }
    applyShipmentStatusToItems(userId, record, next);
  }

  record.updated_at = now;

  return { data: toDetailResponse(record) };
}
