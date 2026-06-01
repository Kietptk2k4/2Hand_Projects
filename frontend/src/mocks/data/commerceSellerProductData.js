import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";
import {
  getCategoryById,
  MOCK_CATEGORY_DRILL_ID,
  MOCK_CATEGORY_SAFETY_ID,
  MOCK_CATEGORY_TOOLS_ID,
} from "./commerceCategoryData";
import { getShopById } from "./commerceShopData";
import { mockCommerceProducts, removeDiscoveryProduct, upsertDiscoveryProduct } from "./commerceProductListData";
import { getShopBySellerId, MOCK_DEMO_SELLER_SHOP_ID } from "./commerceSellerShopData";

/** QA product IDs — demo seller shop */
export const MOCK_SELLER_PRODUCT_ACTIVE_LOW = "c2000000-0000-4000-8000-000000000101";
export const MOCK_SELLER_PRODUCT_ACTIVE = "c2000000-0000-4000-8000-000000000102";
export const MOCK_SELLER_PRODUCT_OOS = "c2000000-0000-4000-8000-000000000103";
export const MOCK_SELLER_PRODUCT_DRAFT = "c2000000-0000-4000-8000-000000000104";
export const MOCK_SELLER_PRODUCT_PAUSED = "c2000000-0000-4000-8000-000000000105";
export const MOCK_SELLER_PRODUCT_ARCHIVED = "c2000000-0000-4000-8000-000000000106";
/** DRAFT đủ giá + tồn kho, thiếu media — test publish fail COMMERCE-400-MEDIA-URL */
export const MOCK_SELLER_PRODUCT_DRAFT_NO_MEDIA = "c2000000-0000-4000-8000-000000000107";
/** DRAFT đủ điều kiện publish */
export const MOCK_SELLER_PRODUCT_DRAFT_READY = "c2000000-0000-4000-8000-000000000108";

const ATTRIBUTE_NAME_MAX = 255;
const ATTRIBUTE_VALUE_MAX = 500;

const productsById = new Map();
const productIdsBySellerId = new Map();

const TITLE_MAX = 500;

function generateProductId() {
  const segment = () =>
    Math.floor(Math.random() * 0x10000)
      .toString(16)
      .padStart(4, "0");
  return `c2${segment()}${segment()}-4000-8000-${segment()}${segment()}`;
}

function getSellerProductIds(userId) {
  return productIdsBySellerId.get(userId) || [];
}

function registerProduct(record) {
  productsById.set(record.product_id, record);
  const ids = getSellerProductIds(record.seller_id);
  if (!ids.includes(record.product_id)) {
    productIdsBySellerId.set(record.seller_id, [...ids, record.product_id]);
  }
}

function getShopForSeller(userId) {
  const shop = getShopBySellerId(userId);
  if (!shop) return null;
  return getShopById(shop.shop_id) || shop;
}

function toDiscoveryRow(record, shop) {
  const inStock = record.stock_quantity > 0;
  const lowStock =
    inStock &&
    record.low_stock_threshold != null &&
    record.stock_quantity <= record.low_stock_threshold;

  return {
    product_id: record.product_id,
    title: record.title,
    description: record.description || "",
    thumbnail_url: record.thumbnail_url || "",
    shop_id: record.shop_id,
    shop_name: shop?.shop_name || "Shop",
    category_id: record.category_id,
    category_name: record.category_name,
    condition: record.condition,
    status: record.status,
    in_stock: inStock,
    low_stock: lowStock,
    shop_vacation: Boolean(shop?.shop_vacation),
    vacation_message: shop?.vacation_message ?? null,
    price: record.price,
    sale_price: record.sale_price,
    effective_price: record.effective_price,
    rating_avg: 0,
    rating_count: 0,
    created_at: record.created_at,
  };
}

function syncDiscovery(record) {
  const shop = getShopById(record.shop_id);
  if (!shop || shop.status !== "ACTIVE" || shop.shop_vacation) {
    removeDiscoveryProduct(record.product_id);
    return;
  }
  if (record.status === "ACTIVE" || record.status === "OUT_OF_STOCK") {
    upsertDiscoveryProduct(toDiscoveryRow(record, shop));
  } else {
    removeDiscoveryProduct(record.product_id);
  }
}

function computeDetailFlags(record) {
  const hasMedia =
    (record.thumbnail_url && String(record.thumbnail_url).startsWith("http")) ||
    (Array.isArray(record.media_urls) && record.media_urls.length > 0);
  return {
    has_price: Boolean(record.price_id && record.effective_price != null),
    has_inventory: Boolean(record.has_inventory),
    has_media: hasMedia,
  };
}

function toDetailItem(record) {
  const flags = computeDetailFlags(record);
  return {
    ...toListItem(record),
    brand_id: record.brand_id,
    attributes: (record.attributes || []).map((attr) => ({
      attribute_name: attr.attribute_name,
      attribute_value: attr.attribute_value,
    })),
    media_urls: record.media_urls || [],
    price_id: record.price_id,
    reserved_quantity: record.reserved_quantity ?? 0,
    ...flags,
  };
}

function toListItem(record) {
  return {
    product_id: record.product_id,
    seller_id: record.seller_id,
    shop_id: record.shop_id,
    status: record.status,
    product_type: record.product_type,
    category_id: record.category_id,
    category_name: record.category_name,
    condition: record.condition,
    title: record.title,
    description: record.description,
    weight_gram: record.weight_gram,
    sku_code: record.sku_code,
    thumbnail_url: record.thumbnail_url,
    price: record.price,
    sale_price: record.sale_price,
    effective_price: record.effective_price,
    stock_quantity: record.stock_quantity,
    low_stock_threshold: record.low_stock_threshold,
    reserved_quantity: record.reserved_quantity,
    created_at: record.created_at,
    updated_at: record.updated_at,
    published_at: record.published_at,
    paused_at: record.paused_at,
    archived_at: record.archived_at,
  };
}

function computeSummary(items) {
  const summary = {
    total: items.length,
    active: 0,
    out_of_stock: 0,
    draft: 0,
    paused: 0,
    archived: 0,
    low_stock: 0,
  };

  for (const item of items) {
    if (item.status === "ACTIVE") summary.active += 1;
    if (item.status === "OUT_OF_STOCK") summary.out_of_stock += 1;
    if (item.status === "DRAFT") summary.draft += 1;
    if (item.status === "PAUSED") summary.paused += 1;
    if (item.status === "ARCHIVED") summary.archived += 1;
    if (
      item.status === "ACTIVE" &&
      item.stock_quantity > 0 &&
      item.stock_quantity <= (item.low_stock_threshold ?? 3)
    ) {
      summary.low_stock += 1;
    }
  }

  return summary;
}

export function listSellerProducts(userId, { page = 1, limit = 10, status, q }) {
  const shop = getShopBySellerId(userId);
  if (!shop) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409, message: "Seller chua co shop." };
  }

  let items = getSellerProductIds(userId)
    .map((id) => productsById.get(id))
    .filter(Boolean);

  if (status) {
    items = items.filter((row) => row.status === status);
  }

  if (q) {
    const needle = String(q).trim().toLowerCase();
    if (needle) {
      items = items.filter(
        (row) =>
          row.title.toLowerCase().includes(needle) ||
          (row.sku_code && row.sku_code.toLowerCase().includes(needle)),
      );
    }
  }

  items.sort((a, b) => new Date(b.updated_at) - new Date(a.updated_at));

  const total = items.length;
  const totalPages = Math.max(1, Math.ceil(total / limit) || 1);
  const start = (page - 1) * limit;
  const slice = items.slice(start, start + limit).map(toListItem);

  return {
    data: {
      items: slice,
      pagination: {
        page,
        limit,
        total,
        total_pages: totalPages,
        has_next: page < totalPages,
      },
      summary: computeSummary(
        getSellerProductIds(userId).map((id) => productsById.get(id)).filter(Boolean),
      ),
    },
  };
}

export function getSellerProductForUser(userId, productId) {
  const record = productsById.get(productId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-PRODUCT", status: 404 };
  }
  return { data: toDetailItem(record) };
}

export function createProductForSeller(userId, body) {
  const shopRecord = getShopBySellerId(userId);
  if (!shopRecord) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409 };
  }

  const publicShop = getShopById(shopRecord.shop_id);
  if (publicShop?.status === "SUSPENDED" || shopRecord.status === "SUSPENDED") {
    return { error: "COMMERCE-409-SHOP-STATUS", status: 409 };
  }

  const category = getCategoryById(body?.category_id);
  if (!category || !category.is_active) {
    return { error: "COMMERCE-404-CATEGORY", status: 404 };
  }

  const title = body?.title?.trim();
  const description = body?.description?.trim();
  const weight = Number(body?.weight_gram);

  if (!body?.product_type || !body?.condition || !title || !description) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  if (title.length > TITLE_MAX || !Number.isInteger(weight) || weight <= 0) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const now = new Date().toISOString();
  const productId = generateProductId();

  const record = {
    product_id: productId,
    seller_id: userId,
    shop_id: shopRecord.shop_id,
    status: "DRAFT",
    product_type: body.product_type,
    category_id: category.category_id,
    category_name: category.category_name,
    brand_id: body.brand_id || null,
    condition: body.condition,
    title,
    description,
    weight_gram: weight,
    sku_code: null,
    thumbnail_url: body.thumbnail_url?.trim() || null,
    media_urls: body.thumbnail_url ? [body.thumbnail_url.trim()] : [],
    price: null,
    sale_price: null,
    effective_price: null,
    price_id: null,
    stock_quantity: null,
    low_stock_threshold: 3,
    reserved_quantity: 0,
    has_inventory: false,
    created_at: now,
    updated_at: now,
    published_at: null,
    paused_at: null,
    archived_at: null,
    attributes: [],
    has_price: false,
  };

  registerProduct(record);
  return { data: toDetailItem(record) };
}

export function setProductMedia(userId, productId, thumbnailUrl) {
  return updateProductMediaForSeller(userId, productId, {
    thumbnail_url: thumbnailUrl,
    media_urls: thumbnailUrl ? [thumbnailUrl] : [],
  });
}

/** FE-only / MSW — chờ API media chính thức (MinIO presigned) */
export function updateProductMediaForSeller(userId, productId, body) {
  const record = productsById.get(productId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-PRODUCT", status: 404 };
  }

  if (record.status === "ARCHIVED") {
    return { error: "COMMERCE-409-PRODUCT-STATUS", status: 409 };
  }

  const urls = Array.isArray(body?.media_urls)
    ? body.media_urls.filter((u) => typeof u === "string" && u.startsWith("http"))
    : [];

  if (body?.thumbnail_url && typeof body.thumbnail_url === "string") {
    record.thumbnail_url = body.thumbnail_url;
  } else if (urls.length > 0) {
    record.thumbnail_url = urls[0];
  } else {
    record.thumbnail_url = null;
  }

  record.media_urls = urls.length > 0 ? urls : record.thumbnail_url ? [record.thumbnail_url] : [];
  record.updated_at = new Date().toISOString();
  syncDiscovery(record);

  return { data: toDetailItem(record) };
}

export function updateProductForSeller(userId, productId, body) {
  const record = productsById.get(productId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-PRODUCT", status: 404 };
  }

  if (record.status === "ARCHIVED") {
    return { error: "COMMERCE-409-PRODUCT-STATUS", status: 409 };
  }

  const shopRecord = getShopBySellerId(userId);
  const publicShop = shopRecord ? getShopById(shopRecord.shop_id) : null;
  if (!shopRecord || publicShop?.status === "SUSPENDED" || shopRecord.status === "SUSPENDED") {
    return { error: "COMMERCE-409-SHOP-STATUS", status: 409 };
  }

  const allowed = ["DRAFT", "ACTIVE", "PAUSED", "OUT_OF_STOCK"];
  if (!allowed.includes(record.status)) {
    return { error: "COMMERCE-409-PRODUCT-STATUS", status: 409 };
  }

  const category = getCategoryById(body?.category_id);
  if (!category || !category.is_active) {
    return { error: "COMMERCE-404-CATEGORY", status: 404 };
  }

  const title = body?.title?.trim();
  const description = body?.description?.trim();
  const weight = Number(body?.weight_gram);

  if (!body?.product_type || !body?.condition || !title || !description) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  if (title.length > TITLE_MAX || !Number.isInteger(weight) || weight <= 0) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  record.product_type = body.product_type;
  record.category_id = category.category_id;
  record.category_name = category.category_name;
  record.brand_id = body.brand_id || null;
  record.condition = body.condition;
  record.title = title;
  record.description = description;
  record.weight_gram = weight;
  record.updated_at = new Date().toISOString();

  syncDiscovery(record);
  return { data: toDetailItem(record) };
}

export function updateProductAttributesForSeller(userId, productId, body) {
  const record = productsById.get(productId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-PRODUCT", status: 404 };
  }

  if (record.status === "ARCHIVED") {
    return { error: "COMMERCE-409-PRODUCT-STATUS", status: 409 };
  }

  const attrs = body?.attributes;
  if (!Array.isArray(attrs)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const names = new Set();
  const normalized = [];

  for (const item of attrs) {
    const name = item?.attribute_name?.trim();
    const value = item?.attribute_value?.trim();
    if (!name || !value) {
      return { error: "COMMERCE-400-VALIDATION", status: 400 };
    }
    if (name.length > ATTRIBUTE_NAME_MAX || value.length > ATTRIBUTE_VALUE_MAX) {
      return { error: "COMMERCE-400-VALIDATION", status: 400 };
    }
    const key = name.toLowerCase();
    if (names.has(key)) {
      return { error: "COMMERCE-400-VALIDATION", status: 400 };
    }
    names.add(key);
    normalized.push({ attribute_name: name, attribute_value: value });
  }

  record.attributes = normalized;
  record.updated_at = new Date().toISOString();
  syncDiscovery(record);

  return {
    data: {
      product_id: productId,
      seller_id: record.seller_id,
      shop_id: record.shop_id,
      status: record.status,
      attributes: normalized,
    },
  };
}

export function updateProductPriceForSeller(userId, productId, body) {
  const record = productsById.get(productId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-PRODUCT", status: 404 };
  }

  const price = Number(body?.price);
  if (!Number.isFinite(price) || price < 0) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const salePrice = body.sale_price != null ? Number(body.sale_price) : null;
  if (salePrice != null && (salePrice < 0 || salePrice > price)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  record.price = price;
  record.sale_price = salePrice;
  record.effective_price = salePrice != null ? salePrice : price;
  record.price_id = `price-${productId}`;
  record.has_price = true;
  record.updated_at = new Date().toISOString();
  syncDiscovery(record);

  return {
    data: {
      product_id: productId,
      price_id: record.price_id,
      price: record.price,
      sale_price: record.sale_price,
      effective_price: record.effective_price,
      status: record.status,
    },
  };
}

export function updateProductInventoryForSeller(userId, productId, body) {
  const record = productsById.get(productId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-PRODUCT", status: 404 };
  }

  const stock = Number(body?.stock_quantity);
  if (!Number.isInteger(stock) || stock < 0) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  if (body.low_stock_threshold !== undefined) {
    const threshold = Number(body.low_stock_threshold);
    if (!Number.isInteger(threshold) || threshold < 0) {
      return { error: "COMMERCE-400-VALIDATION", status: 400 };
    }
    record.low_stock_threshold = threshold;
  }

  const previousStatus = record.status;
  record.stock_quantity = stock;
  record.has_inventory = true;
  record.updated_at = new Date().toISOString();

  if (record.status === "ACTIVE" && stock === 0) {
    record.status = "OUT_OF_STOCK";
  } else if (record.status === "OUT_OF_STOCK" && stock > 0) {
    record.status = "ACTIVE";
  }

  syncDiscovery(record);

  return {
    data: {
      product_id: productId,
      status: record.status,
      previous_status: previousStatus,
      status_changed: previousStatus !== record.status,
      stock_quantity: record.stock_quantity,
      low_stock_threshold: record.low_stock_threshold,
    },
  };
}

function validatePublishPreconditions(record) {
  const shop = getShopById(record.shop_id);
  if (!shop || shop.status !== "ACTIVE") {
    return { error: "COMMERCE-409-SHOP-STATUS", status: 409 };
  }

  const category = getCategoryById(record.category_id);
  if (!category?.is_active) {
    return { error: "COMMERCE-404-CATEGORY", status: 409 };
  }

  if (!record.title?.trim() || !record.description?.trim() || !record.condition) {
    return { error: "COMMERCE-400", status: 400 };
  }

  if (!record.weight_gram || record.weight_gram <= 0) {
    return { error: "COMMERCE-400", status: 400 };
  }

  if (!record.price_id || record.effective_price == null) {
    return { error: "COMMERCE-409-PRICE", status: 409 };
  }

  if (!record.has_inventory) {
    return { error: "COMMERCE-409-PRODUCT-STATUS", status: 409 };
  }

  const hasMedia =
    (record.thumbnail_url && record.thumbnail_url.startsWith("http")) ||
    (record.media_urls?.length > 0);
  if (!hasMedia) {
    return { error: "COMMERCE-400-MEDIA-URL", status: 400 };
  }

  return null;
}

export function publishProductForSeller(userId, productId) {
  const record = productsById.get(productId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-PRODUCT", status: 404 };
  }

  if (record.status === "ARCHIVED") {
    return { error: "COMMERCE-409-PRODUCT-STATUS", status: 409 };
  }

  if (record.status === "ACTIVE" || record.status === "OUT_OF_STOCK") {
    const expected = record.stock_quantity > 0 ? "ACTIVE" : "OUT_OF_STOCK";
    if (record.status === expected) {
      return {
        data: {
          product_id: productId,
          shop_id: record.shop_id,
          status: record.status,
          published_at: record.published_at,
          already_published: true,
        },
      };
    }
  }

  if (record.status !== "DRAFT" && record.status !== "PAUSED") {
    return { error: "COMMERCE-409-PRODUCT-STATUS", status: 409 };
  }

  const validation = validatePublishPreconditions(record);
  if (validation) return validation;

  const now = new Date().toISOString();
  record.status = record.stock_quantity > 0 ? "ACTIVE" : "OUT_OF_STOCK";
  record.published_at = record.published_at || now;
  record.updated_at = now;
  record.paused_at = null;
  syncDiscovery(record);

  return {
    data: {
      product_id: productId,
      shop_id: record.shop_id,
      status: record.status,
      published_at: record.published_at,
      already_published: false,
    },
  };
}

export function pauseProductForSeller(userId, productId) {
  const record = productsById.get(productId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-PRODUCT", status: 404 };
  }

  if (record.status === "PAUSED") {
    return {
      data: {
        product_id: productId,
        shop_id: record.shop_id,
        status: "PAUSED",
        paused_at: record.paused_at,
        already_paused: true,
      },
    };
  }

  if (record.status !== "ACTIVE" && record.status !== "OUT_OF_STOCK") {
    return { error: "COMMERCE-409-PRODUCT-STATUS", status: 409 };
  }

  const now = new Date().toISOString();
  record.status = "PAUSED";
  record.paused_at = now;
  record.updated_at = now;
  syncDiscovery(record);

  return {
    data: {
      product_id: productId,
      shop_id: record.shop_id,
      status: "PAUSED",
      paused_at: now,
      already_paused: false,
    },
  };
}

export function archiveProductForSeller(userId, productId) {
  const record = productsById.get(productId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-PRODUCT", status: 404 };
  }

  if (record.status === "ARCHIVED") {
    return {
      data: {
        product_id: productId,
        shop_id: record.shop_id,
        status: "ARCHIVED",
        archived_at: record.archived_at,
        already_archived: true,
      },
    };
  }

  const allowed = ["DRAFT", "ACTIVE", "PAUSED", "OUT_OF_STOCK"];
  if (!allowed.includes(record.status)) {
    return { error: "COMMERCE-409-PRODUCT-STATUS", status: 409 };
  }

  const now = new Date().toISOString();
  record.status = "ARCHIVED";
  record.archived_at = now;
  record.updated_at = now;
  syncDiscovery(record);

  return {
    data: {
      product_id: productId,
      shop_id: record.shop_id,
      status: "ARCHIVED",
      archived_at: now,
      already_archived: false,
    },
  };
}

function buildSeedProduct(overrides) {
  const category = getCategoryById(overrides.category_id);
  const now = overrides.updated_at || new Date().toISOString();
  return {
    product_type: "PHYSICAL",
    brand_id: null,
    low_stock_threshold: 3,
    reserved_quantity: 0,
    media_urls: [],
    attributes: [],
    has_price: false,
    ...overrides,
    category_name: category?.category_name || "",
    created_at: overrides.created_at || now,
    updated_at: now,
  };
}

function seedDemoSellerProducts() {
  if (getSellerProductIds(MOCK_CART_DEMO_USER_ID).length > 0) return;

  const shopId = MOCK_DEMO_SELLER_SHOP_ID;
  const sellerId = MOCK_CART_DEMO_USER_ID;
  const base = { seller_id: sellerId, shop_id: shopId };

  const seeds = [
    buildSeedProduct({
      ...base,
      product_id: MOCK_SELLER_PRODUCT_ACTIVE_LOW,
      status: "ACTIVE",
      category_id: MOCK_CATEGORY_DRILL_ID,
      condition: "NEW",
      title: "Máy khoan pin demo — sắp hết hàng",
      description: "Sản phẩm demo ACTIVE tồn kho thấp.",
      weight_gram: 1800,
      sku_code: "SKU-DEMO-101",
      thumbnail_url: "https://picsum.photos/seed/seller-demo-101/400/300",
      media_urls: ["https://picsum.photos/seed/seller-demo-101/400/300"],
      price: 890000,
      sale_price: null,
      effective_price: 890000,
      price_id: "price-101",
      stock_quantity: 2,
      has_inventory: true,
      published_at: "2026-05-10T08:00:00Z",
    }),
    buildSeedProduct({
      ...base,
      product_id: MOCK_SELLER_PRODUCT_ACTIVE,
      status: "ACTIVE",
      category_id: MOCK_CATEGORY_TOOLS_ID,
      condition: "NEW",
      title: "Bộ tuốc lực điện demo",
      description: "Sản phẩm demo đang bán.",
      weight_gram: 2500,
      sku_code: "SKU-DEMO-102",
      thumbnail_url: "https://picsum.photos/seed/seller-demo-102/400/300",
      media_urls: ["https://picsum.photos/seed/seller-demo-102/400/300"],
      price: 1250000,
      sale_price: 1100000,
      effective_price: 1100000,
      price_id: "price-102",
      stock_quantity: 15,
      has_inventory: true,
      published_at: "2026-05-12T08:00:00Z",
    }),
    buildSeedProduct({
      ...base,
      product_id: MOCK_SELLER_PRODUCT_OOS,
      status: "OUT_OF_STOCK",
      category_id: MOCK_CATEGORY_SAFETY_ID,
      condition: "NEW",
      title: "Găng tay chống cắt demo — hết hàng",
      description: "Demo OUT_OF_STOCK.",
      weight_gram: 200,
      sku_code: "SKU-DEMO-103",
      thumbnail_url: "https://picsum.photos/seed/seller-demo-103/400/300",
      media_urls: ["https://picsum.photos/seed/seller-demo-103/400/300"],
      price: 95000,
      effective_price: 95000,
      price_id: "price-103",
      stock_quantity: 0,
      has_inventory: true,
      published_at: "2026-05-08T08:00:00Z",
    }),
    buildSeedProduct({
      ...base,
      product_id: MOCK_SELLER_PRODUCT_DRAFT,
      status: "DRAFT",
      category_id: MOCK_CATEGORY_DRILL_ID,
      condition: "USED",
      title: "Máy mài góc nháp — chưa có giá",
      description: "Draft thiếu giá để test publish fail.",
      weight_gram: 3200,
      sku_code: "SKU-DEMO-104",
      thumbnail_url: "https://picsum.photos/seed/seller-demo-104/400/300",
      media_urls: ["https://picsum.photos/seed/seller-demo-104/400/300"],
      has_inventory: false,
      stock_quantity: null,
    }),
    buildSeedProduct({
      ...base,
      product_id: MOCK_SELLER_PRODUCT_PAUSED,
      status: "PAUSED",
      category_id: MOCK_CATEGORY_TOOLS_ID,
      condition: "NEW",
      title: "Đầu bits từ tính demo — tạm dừng",
      description: "Demo PAUSED.",
      weight_gram: 500,
      sku_code: "SKU-DEMO-105",
      thumbnail_url: "https://picsum.photos/seed/seller-demo-105/400/300",
      media_urls: ["https://picsum.photos/seed/seller-demo-105/400/300"],
      price: 180000,
      effective_price: 180000,
      price_id: "price-105",
      stock_quantity: 8,
      has_inventory: true,
      published_at: "2026-05-01T08:00:00Z",
      paused_at: "2026-05-15T08:00:00Z",
    }),
    buildSeedProduct({
      ...base,
      product_id: MOCK_SELLER_PRODUCT_ARCHIVED,
      status: "ARCHIVED",
      category_id: MOCK_CATEGORY_SAFETY_ID,
      condition: "USED",
      title: "Kính bảo hộ demo — đã lưu trữ",
      description: "Demo ARCHIVED.",
      weight_gram: 300,
      sku_code: "SKU-DEMO-106",
      thumbnail_url: "https://picsum.photos/seed/seller-demo-106/400/300",
      media_urls: ["https://picsum.photos/seed/seller-demo-106/400/300"],
      price: 120000,
      effective_price: 120000,
      price_id: "price-106",
      stock_quantity: 0,
      has_inventory: true,
      archived_at: "2026-05-18T08:00:00Z",
    }),
    buildSeedProduct({
      ...base,
      product_id: MOCK_SELLER_PRODUCT_DRAFT_NO_MEDIA,
      status: "DRAFT",
      category_id: MOCK_CATEGORY_DRILL_ID,
      condition: "NEW",
      title: "Máy cắt nháp — thiếu ảnh (test publish fail)",
      description: "Draft có giá và tồn kho nhưng chưa có media.",
      weight_gram: 1500,
      sku_code: "SKU-DEMO-107",
      thumbnail_url: null,
      media_urls: [],
      price: 450000,
      effective_price: 450000,
      price_id: "price-107",
      stock_quantity: 5,
      has_inventory: true,
      has_price: true,
    }),
    buildSeedProduct({
      ...base,
      product_id: MOCK_SELLER_PRODUCT_DRAFT_READY,
      status: "DRAFT",
      category_id: MOCK_CATEGORY_TOOLS_ID,
      condition: "NEW",
      title: "Cờ lê điện nháp — sẵn sàng đăng bán",
      description: "Draft đủ giá, tồn kho và ảnh.",
      weight_gram: 900,
      sku_code: "SKU-DEMO-108",
      thumbnail_url: "https://picsum.photos/seed/seller-demo-108/400/300",
      media_urls: ["https://picsum.photos/seed/seller-demo-108/400/300"],
      price: 320000,
      effective_price: 320000,
      price_id: "price-108",
      stock_quantity: 12,
      has_inventory: true,
      has_price: true,
      attributes: [{ attribute_name: "Điện áp", attribute_value: "20V" }],
    }),
  ];

  for (const record of seeds) {
    registerProduct(record);
    if (record.status === "ACTIVE" || record.status === "OUT_OF_STOCK") {
      syncDiscovery(record);
    }
  }
}

seedDemoSellerProducts();
