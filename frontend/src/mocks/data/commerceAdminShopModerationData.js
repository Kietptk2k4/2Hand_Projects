import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";
import { MOCK_DEMO_SELLER_SHOP_ID } from "./commerceSellerShopData";

/** QA: shop ACTIVE — thử SUSPEND / CLOSE */
export const MOCK_ADMIN_SHOP_ACTIVE = "s2000000-0000-4000-8000-000000000101";
/** QA: shop SUSPENDED — thử RESTORE / CLOSE */
export const MOCK_ADMIN_SHOP_SUSPENDED = "s2000000-0000-4000-8000-000000000102";
/** QA: shop CLOSED — thử RESTORE */
export const MOCK_ADMIN_SHOP_CLOSED = "s2000000-0000-4000-8000-000000000103";

const VALID_ACTIONS = ["SUSPEND", "CLOSE", "RESTORE"];
const VALID_STATUSES = ["ACTIVE", "SUSPENDED", "CLOSED"];

const TRANSITIONS = {
  ACTIVE: { SUSPEND: "SUSPENDED", CLOSE: "CLOSED" },
  SUSPENDED: { RESTORE: "ACTIVE", CLOSE: "CLOSED" },
  CLOSED: { RESTORE: "ACTIVE" },
};

const shopsById = new Map();
const moderationHistoryByShopId = new Map();

function appendShopHistory(shopId, entry) {
  const list = moderationHistoryByShopId.get(shopId) || [];
  moderationHistoryByShopId.set(shopId, [entry, ...list]);
}

function buildShop(index, overrides = {}) {
  const seed = String(index).padStart(12, "0");
  const created = new Date(2026, 3, 15 - index, 10, 0, 0).toISOString();

  return {
    shop_id: overrides.shop_id ?? `s2000000-0000-4000-8000-${seed}`,
    seller_id:
      overrides.seller_id ??
      `a1000000-0000-4000-8000-${String(index + 10).padStart(12, "0")}`,
    shop_name: overrides.shop_name ?? `Cửa hàng mẫu ${index}`,
    logo_url:
      overrides.logo_url ??
      `https://picsum.photos/seed/admin-shop-${index}/80/80`,
    description: overrides.description ?? `Mô tả cửa hàng mẫu ${index}`,
    status: overrides.status ?? "ACTIVE",
    created_at: overrides.created_at ?? created,
    updated_at: overrides.updated_at ?? created,
    product_count: overrides.product_count ?? index * 3 + 2,
    active_product_count: overrides.active_product_count ?? index * 2 + 1,
    open_order_count: overrides.open_order_count ?? index % 5,
  };
}

function seedAdminShops() {
  if (shopsById.size > 0) return;

  shopsById.set(
    MOCK_DEMO_SELLER_SHOP_ID,
    buildShop(10, {
      shop_id: MOCK_DEMO_SELLER_SHOP_ID,
      seller_id: MOCK_CART_DEMO_USER_ID,
      shop_name: "Cửa hàng Demo 2Hands",
      logo_url: "https://picsum.photos/seed/demo-seller-shop-avatar/80/80",
      status: "ACTIVE",
      created_at: "2026-05-01T08:00:00Z",
    }),
  );

  shopsById.set(
    MOCK_ADMIN_SHOP_ACTIVE,
    buildShop(1, {
      shop_id: MOCK_ADMIN_SHOP_ACTIVE,
      shop_name: "Tech Haven Repairs",
      status: "ACTIVE",
    }),
  );

  shopsById.set(
    MOCK_ADMIN_SHOP_SUSPENDED,
    buildShop(2, {
      shop_id: MOCK_ADMIN_SHOP_SUSPENDED,
      shop_name: "Premium Audio Hub",
      status: "SUSPENDED",
    }),
  );

  shopsById.set(
    MOCK_ADMIN_SHOP_CLOSED,
    buildShop(3, {
      shop_id: MOCK_ADMIN_SHOP_CLOSED,
      shop_name: "Vintage Gear Co.",
      status: "CLOSED",
    }),
  );

  const names = [
    "Điện máy Minh Phát",
    "Công cụ ProLine",
    "Nội thất GreenHome",
    "Thời trang Lumière",
    "Sách & Văn phòng phẩm An",
    "Thể thao FitZone",
    "Mỹ phẩm Bloom",
    "Đồ chơi KidJoy",
  ];

  names.forEach((name, i) => {
    const idx = i + 4;
    const status = idx % 3 === 0 ? "SUSPENDED" : idx % 3 === 1 ? "CLOSED" : "ACTIVE";
    const shop = buildShop(idx, { shop_name: name, status });
    shopsById.set(shop.shop_id, shop);
  });
}

seedAdminShops();

export function validateAdminShopListQuery({ page, limit, status, q, sort }) {
  const pageNum = Number(page);
  const limitNum = Number(limit);

  if (!Number.isInteger(pageNum) || pageNum < 1) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (!Number.isInteger(limitNum) || limitNum < 1 || limitNum > 100) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (status && status !== "all" && !VALID_STATUSES.includes(status)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const sortValue = sort || "NEWEST";
  if (!["NEWEST", "OLDEST", "NAME_ASC", "UPDATED_AT"].includes(sortValue)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  return {
    page: pageNum,
    limit: limitNum,
    status: status && status !== "all" ? status : null,
    q: q?.trim() || null,
    sort: sortValue,
  };
}

export function listAdminShops({ page, limit, status, q, sort }) {
  let items = [...shopsById.values()];

  if (status) {
    items = items.filter((s) => s.status === status);
  }

  if (q) {
    const needle = q.toLowerCase();
    items = items.filter(
      (s) =>
        s.shop_name.toLowerCase().includes(needle) ||
        s.shop_id.toLowerCase().includes(needle) ||
        s.seller_id.toLowerCase().includes(needle),
    );
  }

  if (sort === "OLDEST") {
    items.sort((a, b) => new Date(a.created_at) - new Date(b.created_at));
  } else if (sort === "NAME_ASC") {
    items.sort((a, b) => a.shop_name.localeCompare(b.shop_name, "vi"));
  } else if (sort === "UPDATED_AT") {
    items.sort((a, b) => new Date(b.updated_at) - new Date(a.updated_at));
  } else {
    items.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));
  }

  const total = items.length;
  const totalPages = Math.max(1, Math.ceil(total / limit) || 1);
  const start = (page - 1) * limit;

  return {
    data: {
      items: items.slice(start, start + limit).map(toListItem),
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

function toListItem(record) {
  return {
    shop_id: record.shop_id,
    seller_id: record.seller_id,
    shop_name: record.shop_name,
    logo_url: record.logo_url,
    status: record.status,
    created_at: record.created_at,
    updated_at: record.updated_at,
    product_count: record.product_count ?? Math.floor(Math.random() * 20) + 1,
    active_product_count: record.active_product_count ?? Math.floor(Math.random() * 15) + 1,
  };
}

export function getAdminShopById(shopId) {
  return shopsById.get(shopId) || null;
}

export function getAdminShopDetailForModeration(shopId) {
  const record = getAdminShopById(shopId);
  if (!record) {
    return { error: "COMMERCE-404-SHOP", status: 404 };
  }

  return {
    data: {
      shop_id: record.shop_id,
      seller_id: record.seller_id,
      shop_name: record.shop_name,
      description: record.description || "",
      logo_url: record.logo_url,
      status: record.status,
      created_at: record.created_at,
      updated_at: record.updated_at,
      total_product_count: record.product_count ?? 0,
      active_product_count: record.active_product_count ?? 0,
      open_order_count: record.open_order_count ?? 0,
    },
  };
}

export function getShopModerationHistory(shopId, { page = 1, size = 20 } = {}) {
  const record = getAdminShopById(shopId);
  if (!record) {
    return { error: "ADMIN-404", status: 404, message: "Shop not found." };
  }

  const pageNum = Number(page);
  const sizeNum = Number(size);

  if (!Number.isInteger(pageNum) || pageNum < 1 || !Number.isInteger(sizeNum) || sizeNum < 1) {
    return { error: "ADMIN-400-PAGINATION", status: 400 };
  }

  const all = moderationHistoryByShopId.get(shopId) || [];
  const totalElements = all.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / sizeNum) || 1);
  const start = (pageNum - 1) * sizeNum;

  return {
    data: {
      shop_id: shopId,
      page: pageNum,
      size: sizeNum,
      total_elements: totalElements,
      total_pages: totalPages,
      history: all.slice(start, start + sizeNum),
    },
    message: "Shop moderation history retrieved successfully",
  };
}

function actionMessage(action) {
  if (action === "SUSPEND") return "Suspend shop thanh cong.";
  if (action === "CLOSE") return "Dong shop thanh cong.";
  return "Khoi phuc shop thanh cong.";
}

export function moderateAdminShop(shopId, body, { isAdmin, permissions = [] }) {
  const action = body?.action;
  const reason = String(body?.reason ?? "").trim();

  if (!VALID_ACTIONS.includes(action)) {
    return { error: "COMMERCE-400-SHOP-MODERATION", status: 400 };
  }

  if (!reason) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const permissionOk = checkModerationPermission(action, { isAdmin, permissions });
  if (!permissionOk) {
    return { error: "COMMERCE-403", status: 403 };
  }

  const record = shopsById.get(shopId);
  if (!record) {
    return { error: "COMMERCE-404-SHOP", status: 404 };
  }

  const nextStatus = TRANSITIONS[record.status]?.[action];
  if (!nextStatus) {
    return { error: "COMMERCE-409-SHOP-STATUS", status: 409 };
  }

  const previousStatus = record.status;
  const alreadyModerated = previousStatus === nextStatus;

  if (!alreadyModerated) {
    record.status = nextStatus;
    record.updated_at = new Date().toISOString();
    shopsById.set(shopId, record);
    appendShopHistory(shopId, {
      moderation_log_id: `smh-${Date.now().toString(36)}`,
      action,
      reason,
      note: body?.note || null,
      admin_id: "a1000000-0000-4000-8000-000000000001",
      created_at: new Date().toISOString(),
    });
  }

  const cartInvalidated =
    !alreadyModerated && (action === "SUSPEND" || action === "CLOSE")
      ? Math.floor(Math.random() * 8) + 1
      : 0;

  return {
    data: {
      shop_id: record.shop_id,
      seller_id: record.seller_id,
      shop_name: record.shop_name,
      status: record.status,
      previous_status: previousStatus,
      already_moderated: alreadyModerated,
      cart_items_invalidated: cartInvalidated,
      moderated_at: new Date().toISOString(),
    },
    message: actionMessage(action),
  };
}

function checkModerationPermission(action, { isAdmin, permissions }) {
  if (isAdmin) return true;

  const set = new Set(permissions);
  if (action === "SUSPEND") return set.has("COMMERCE_SHOP_SUSPEND");
  if (action === "CLOSE") return set.has("COMMERCE_SHOP_CLOSE");
  if (action === "RESTORE") {
    return set.has("COMMERCE_SHOP_SUSPEND") || set.has("COMMERCE_SHOP_CLOSE");
  }
  return false;
}

export function userHasAdminShopModerationAccess(user) {
  return Boolean(user?.is_admin);
}
