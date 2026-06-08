import { mockCommerceProducts } from "./commerceProductListData";
import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";
import { MOCK_DEMO_SELLER_SHOP_ID } from "./commerceSellerShopData";

/** QA: ACTIVE — thử remove */
export const MOCK_ADMIN_PRODUCT_ACTIVE = "c1000000-0000-4000-8000-000000000001";
/** QA: OUT_OF_STOCK — thử remove */
export const MOCK_ADMIN_PRODUCT_OUT_OF_STOCK = "c1000000-0000-4000-8000-000000000007";
/** QA: REMOVED — thử remove lại (idempotent) / Xem hồ sơ */
export const MOCK_ADMIN_PRODUCT_REMOVED = "cap-0000-4000-8000-000000000001";

const VALID_LIST_STATUSES = ["ACTIVE", "OUT_OF_STOCK", "REMOVED", "DRAFT", "PAUSED", "ARCHIVED"];

function sellerIdFromShop(shopId) {
  if (!shopId) return MOCK_CART_DEMO_USER_ID;
  const suffix = shopId.replace(/-/g, "").slice(-12);
  return `a1000000-0000-4000-8000-${suffix}`;
}

function toAdminProduct(row, overrides = {}) {
  const sellerId = overrides.seller_id ?? sellerIdFromShop(row.shop_id);
  return {
    product_id: row.product_id,
    seller_id: sellerId,
    shop_id: row.shop_id,
    shop_name: row.shop_name,
    title: row.title,
    thumbnail_url: row.thumbnail_url,
    category_id: row.category_id,
    category_name: row.category_name,
    price: row.price,
    effective_price: row.effective_price ?? row.price,
    status: overrides.status ?? row.status ?? "ACTIVE",
    created_at: row.created_at,
    removed_at: overrides.removed_at ?? null,
    remove_reason: overrides.remove_reason ?? null,
  };
}

function seedAdminProducts() {
  const list = mockCommerceProducts.map((p) => toAdminProduct(p));

  list.push(
    toAdminProduct(
      {
        product_id: MOCK_ADMIN_PRODUCT_REMOVED,
        title: "Tui hang gia vi pham thuong hieu",
        thumbnail_url: "https://picsum.photos/seed/admin-prod-removed-1/64/64",
        shop_id: MOCK_DEMO_SELLER_SHOP_ID,
        shop_name: "Cua hang Demo 2Hands",
        category_id: "f1000000-0000-4000-8000-000000000014",
        category_name: "Tui & Phu kien nu",
        price: 890000,
        effective_price: 890000,
        created_at: "2026-04-10T08:00:00Z",
      },
      {
        seller_id: MOCK_CART_DEMO_USER_ID,
        status: "REMOVED",
        removed_at: "2026-05-18T14:30:00Z",
        remove_reason: "Vi phạm chính sách nhãn hiệu — bán hàng giả mạo thương hiệu.",
      },
    ),
    toAdminProduct(
      {
        product_id: "cap-0000-4000-8000-000000000002",
        title: "Hang cam kinh doanh (mau)",
        thumbnail_url: "https://picsum.photos/seed/admin-prod-removed-2/64/64",
        shop_id: "s1000000-0000-4000-8000-000000000002",
        shop_name: "Vintage Closet",
        category_id: "f1000000-0000-4000-8000-000000000040",
        category_name: "Vintage & Designer",
        price: 2500000,
        effective_price: 2500000,
        created_at: "2026-03-22T11:00:00Z",
      },
      {
        status: "REMOVED",
        removed_at: "2026-05-10T09:15:00Z",
        remove_reason: "Nội dung mô tả sai sự thật, quảng cáo gây hiểu nhầm.",
      },
    ),
    toAdminProduct(
      {
        product_id: "cap-0000-4000-8000-000000000003",
        title: "Sản phẩm nháp admin test",
        thumbnail_url: "https://picsum.photos/seed/admin-prod-draft/64/64",
        shop_id: "s1000000-0000-4000-8000-000000000003",
        shop_name: "Streetwear 2Hand",
        category_id: "f1000000-0000-4000-8000-000000000030",
        category_name: "Unisex / Streetwear",
        price: 120000,
        effective_price: 120000,
        created_at: "2026-05-19T16:00:00Z",
      },
      { status: "DRAFT" },
    ),
  );

  return list;
}

const productsById = new Map(seedAdminProducts().map((p) => [p.product_id, { ...p }]));

/** @type {Map<string, Array<object>>} */
const moderationHistoryByProductId = new Map();

function nextModerationLogId(prefix) {
  return `${prefix}-${Date.now().toString(36)}`;
}

function appendProductHistory(productId, entry) {
  const list = moderationHistoryByProductId.get(productId) || [];
  moderationHistoryByProductId.set(productId, [entry, ...list]);
}

function seedProductHistory() {
  appendProductHistory(MOCK_ADMIN_PRODUCT_REMOVED, {
    moderation_log_id: "pmh-0000-4000-8000-000000000001",
    action: "REMOVE",
    reason: "Vi pham chinh sach nhan hieu — ban hang gia mao thuong hieu.",
    note: null,
    admin_id: "a1000000-0000-4000-8000-000000000001",
    created_at: "2026-05-18T14:30:00Z",
  });
}

seedProductHistory();

function mockCartInvalidated(productId) {
  const hash = productId.split("").reduce((acc, c) => acc + c.charCodeAt(0), 0);
  return hash % 6;
}

export function validateAdminProductListQuery({ page, limit, status, q }) {
  const pageNum = Number(page);
  const limitNum = Number(limit);

  if (!Number.isInteger(pageNum) || pageNum < 1) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (!Number.isInteger(limitNum) || limitNum < 1 || limitNum > 50) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (status && !VALID_LIST_STATUSES.includes(status)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  return { page: pageNum, limit: limitNum, status: status || null, q: q || null };
}

export function listAdminProductsForAdmin({ page, limit, status, q }) {
  let items = [...productsById.values()];

  if (status) {
    items = items.filter((p) => p.status === status);
  }

  if (q) {
    const needle = String(q).trim().toLowerCase();
    if (needle) {
      items = items.filter(
        (p) =>
          p.title.toLowerCase().includes(needle) ||
          p.product_id.toLowerCase().includes(needle) ||
          p.shop_id.toLowerCase().includes(needle) ||
          (p.shop_name && p.shop_name.toLowerCase().includes(needle)),
      );
    }
  }

  items.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));

  const total = items.length;
  const totalPages = Math.max(1, Math.ceil(total / limit) || 1);
  const start = (page - 1) * limit;

  return {
    data: {
      items: items.slice(start, start + limit),
      pagination: {
        page,
        limit,
        total_items: total,
        total_pages: totalPages,
        has_next: page < totalPages,
      },
    },
  };
}

export function removeProductByAdmin(productId, body, { isAdmin }) {
  if (!isAdmin) {
    return { error: "COMMERCE-403", status: 403 };
  }

  const reason = String(body?.reason ?? "").trim();
  if (!reason) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const record = productsById.get(productId);
  if (!record) {
    return { error: "COMMERCE-404-PRODUCT", status: 404 };
  }

  const previousStatus = record.status;
  let alreadyRemoved = false;
  let cartItemsInvalidated = 0;

  if (previousStatus === "REMOVED") {
    alreadyRemoved = true;
  } else {
    record.status = "REMOVED";
    record.remove_reason = reason;
    record.removed_at = new Date().toISOString();
    cartItemsInvalidated = mockCartInvalidated(productId);
    productsById.set(productId, record);
    appendProductHistory(productId, {
      moderation_log_id: nextModerationLogId("pmh"),
      action: "REMOVE",
      reason,
      note: body?.note ?? null,
      admin_id: "a1000000-0000-4000-8000-000000000001",
      created_at: record.removed_at,
    });
  }

  return {
    data: {
      product_id: record.product_id,
      moderation_log_id: nextModerationLogId("pmh"),
      reason,
      note: body?.note ?? null,
      removed_at: record.removed_at,
      outbox_event_id: nextModerationLogId("ob"),
      seller_id: record.seller_id,
      shop_id: record.shop_id,
      title: record.title,
      status: record.status,
      previous_status: previousStatus,
      already_removed: alreadyRemoved,
      cart_items_invalidated: alreadyRemoved ? 0 : cartItemsInvalidated,
    },
    message: alreadyRemoved ? "San pham da duoc go truoc do." : "Go san pham thanh cong.",
  };
}

export function restoreProductByAdmin(productId, body, { isAdmin }) {
  if (!isAdmin) {
    return { error: "ADMIN-403", status: 403 };
  }

  const reason = String(body?.reason ?? "").trim();
  if (!reason) {
    return { error: "ADMIN-400-VALIDATION", status: 400 };
  }

  const record = productsById.get(productId);
  if (!record) {
    return { error: "ADMIN-404", status: 404 };
  }

  if (record.status !== "REMOVED") {
    return { error: "ADMIN-409", status: 409, message: "San pham khong o trang thai REMOVED." };
  }

  const restoredAt = new Date().toISOString();
  record.status = "ACTIVE";
  record.remove_reason = null;
  record.removed_at = null;
  productsById.set(productId, record);

  appendProductHistory(productId, {
    moderation_log_id: nextModerationLogId("pmh"),
    action: "RESTORE",
    reason,
    note: body?.note ?? null,
    admin_id: "a1000000-0000-4000-8000-000000000001",
    created_at: restoredAt,
  });

  return {
    data: {
      product_id: record.product_id,
      moderation_log_id: nextModerationLogId("pmh"),
      reason,
      note: body?.note ?? null,
      restored_at: restoredAt,
      outbox_event_id: nextModerationLogId("ob"),
    },
    message: "Khoi phuc san pham thanh cong.",
  };
}

export function getProductModerationHistory(productId, { page = 1, size = 20 } = {}) {
  const pageNum = Number(page);
  const sizeNum = Number(size);

  if (!Number.isInteger(pageNum) || pageNum < 1 || !Number.isInteger(sizeNum) || sizeNum < 1) {
    return { error: "ADMIN-400-PAGINATION", status: 400 };
  }

  const record = productsById.get(productId);
  if (!record) {
    return { error: "ADMIN-404", status: 404 };
  }

  const all = moderationHistoryByProductId.get(productId) || [];
  const totalElements = all.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / sizeNum) || 1);
  const start = (pageNum - 1) * sizeNum;

  return {
    data: {
      product_id: productId,
      page: pageNum,
      size: sizeNum,
      total_elements: totalElements,
      total_pages: totalPages,
      history: all.slice(start, start + sizeNum),
    },
  };
}

export function userHasAdminProductAccess(user) {
  return Boolean(user?.is_admin);
}

export function getAdminProductById(productId) {
  return productsById.get(productId) ?? null;
}
