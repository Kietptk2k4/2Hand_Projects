import { getCategoryById } from "./commerceCategoryData";
import { mockCommerceProducts } from "./commerceProductListData";
import { getShopById } from "./commerceShopData";

export const MOCK_PRODUCT_DETAIL_IDS = {
  ACTIVE_SALE: "c1000000-0000-4000-8000-000000000001",
  OUT_OF_STOCK: "c1000000-0000-4000-8000-000000000007",
  VACATION: "c1000000-0000-4000-8000-000000000008",
  NOT_FOUND: "c1000000-0000-4000-8000-000000000099",
};

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export function isValidProductId(productId) {
  return typeof productId === "string" && UUID_REGEX.test(productId);
}

function buildMedia(product) {
  const seed = product.product_id.replace(/-/g, "").slice(0, 12);
  const baseUrl = product.thumbnail_url || `https://picsum.photos/seed/${seed}/800/600`;
  const items = [
    {
      media_id: `m-${seed}-0`,
      media_url: baseUrl,
      media_type: "IMAGE",
      sort_order: 0,
    },
    {
      media_id: `m-${seed}-1`,
      media_url: `https://picsum.photos/seed/${seed}-1/800/600`,
      media_type: "IMAGE",
      sort_order: 1,
    },
    {
      media_id: `m-${seed}-2`,
      media_url: `https://picsum.photos/seed/${seed}-2/800/600`,
      media_type: "IMAGE",
      sort_order: 2,
    },
  ];
  return items;
}

function buildAttributes(product) {
  const defaults = [
    { attribute_name: "size", attribute_value: "M" },
    { attribute_name: "color", attribute_value: "Den" },
    { attribute_name: "material", attribute_value: "Cotton blend" },
  ];
  if (product.category_name) {
    defaults.push({ attribute_name: "Danh mục", attribute_value: product.category_name });
  }
  return defaults;
}

function buildInventorySummary(product) {
  const inStock = product.in_stock !== false && product.status !== "OUT_OF_STOCK";
  const lowStock = Boolean(product.low_stock) && inStock;
  let stockQuantity = 0;
  if (inStock) {
    stockQuantity = lowStock ? 1 : 1;
  }
  return {
    stock_quantity: stockQuantity,
    low_stock_threshold: 0,
    in_stock: inStock,
    low_stock: lowStock,
  };
}

export function findListProductById(productId) {
  return mockCommerceProducts.find((item) => item.product_id === productId) || null;
}

export function buildDetailFromListProduct(product) {
  const category = getCategoryById(product.category_id);
  const shopRecord = getShopById(product.shop_id);
  const shopVacation = Boolean(shopRecord?.shop_vacation || product.shop_vacation);
  const vacationMessage =
    shopRecord?.vacation_message || product.vacation_message || null;

  let salePrice = product.sale_price;
  let effectivePrice = product.effective_price;

  if (product.product_id === MOCK_PRODUCT_DETAIL_IDS.ACTIVE_SALE && salePrice == null) {
    salePrice = Math.round(product.price * 0.85);
    effectivePrice = salePrice;
  }

  return {
    product_id: product.product_id,
    title: product.title,
    description:
      product.description ||
      `${product.title} — san pham second-hand tu tu do ca nhan tren 2Hands Commerce.`,
    condition: product.condition || "GOOD",
    weight_gram: product.weight_gram ?? 1850,
    status: product.status || "ACTIVE",
    category: {
      category_id: product.category_id,
      name: category?.category_name || product.category_name || "Sản phẩm",
      slug: category?.category_slug || "san-pham",
    },
    shop: {
      shop_id: product.shop_id,
      shop_name: shopRecord?.shop_name || product.shop_name,
      avatar_url: shopRecord?.avatar_url || "",
      cover_url: shopRecord?.cover_url || null,
    },
    media: buildMedia(product),
    attributes: buildAttributes(product),
    price: product.price,
    sale_price: salePrice,
    effective_price: effectivePrice,
    inventory_summary: buildInventorySummary(product),
    rating_avg: product.rating_avg,
    rating_count: product.rating_count,
    shop_vacation: shopVacation,
    vacation_message: vacationMessage,
  };
}

export function buildProductDetail(productId) {
  const listProduct = findListProductById(productId);
  if (!listProduct) return null;
  return buildDetailFromListProduct(listProduct);
}
