function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapSellerProductListResponse(data) {
  if (!data) return { items: [], pagination: null, summary: null };

  return {
    items: (data.items || []).map(mapSellerProductListItem).filter(Boolean),
    pagination: data.pagination
      ? {
          page: data.pagination.page,
          limit: data.pagination.limit,
          total: data.pagination.total ?? data.pagination.total_items,
          totalPages: data.pagination.total_pages,
          hasNext: Boolean(data.pagination.has_next),
        }
      : null,
    summary: data.summary
      ? {
          total: data.summary.total ?? 0,
          active: data.summary.active ?? 0,
          outOfStock: data.summary.out_of_stock ?? data.summary.outOfStock ?? 0,
          draft: data.summary.draft ?? 0,
          paused: data.summary.paused ?? 0,
          archived: data.summary.archived ?? 0,
          lowStock: data.summary.low_stock ?? data.summary.lowStock ?? 0,
        }
      : null,
  };
}

export function mapSellerProductListItem(item) {
  if (!item) return null;

  return {
    productId: pick(item, "productId", "product_id"),
    sellerId: pick(item, "sellerId", "seller_id"),
    shopId: pick(item, "shopId", "shop_id"),
    status: item.status,
    productType: pick(item, "productType", "product_type"),
    categoryId: pick(item, "categoryId", "category_id"),
    categoryName: pick(item, "categoryName", "category_name"),
    condition: item.condition,
    title: item.title,
    description: item.description,
    weightGram: item.weight_gram ?? item.weightGram,
    skuCode: pick(item, "skuCode", "sku_code"),
    thumbnailUrl: pick(item, "thumbnailUrl", "thumbnail_url"),
    price: item.price,
    salePrice: item.sale_price ?? item.salePrice,
    effectivePrice: item.effective_price ?? item.effectivePrice,
    stockQuantity: item.stock_quantity ?? item.stockQuantity,
    lowStockThreshold: item.low_stock_threshold ?? item.lowStockThreshold,
    createdAt: pick(item, "createdAt", "created_at"),
    updatedAt: pick(item, "updatedAt", "updated_at"),
    publishedAt: pick(item, "publishedAt", "published_at"),
  };
}

export function mapCreateProductPayload(form) {
  const body = {
    product_type: form.productType,
    category_id: form.categoryId,
    condition: form.condition,
    title: form.title.trim(),
    description: form.description.trim(),
    weight_gram: Number(form.weightGram),
  };

  if (form.thumbnailUrl?.trim()) {
    body.thumbnail_url = form.thumbnailUrl.trim();
  }

  return body;
}

export function mapCreateProductResponse(data) {
  return mapSellerProductListItem(data);
}

export function mapUpdatePricePayload(form) {
  return {
    price: Number(form.price),
    sale_price: form.salePrice !== "" ? Number(form.salePrice) : undefined,
    start_at: new Date().toISOString(),
  };
}

export function mapUpdateInventoryPayload(form) {
  return {
    stock_quantity: Number(form.stockQuantity),
    low_stock_threshold: Number(form.lowStockThreshold) || 3,
  };
}

export function formatProductUpdatedAt(iso) {
  if (!iso) return "—";
  try {
    const date = new Date(iso);
    if (Number.isNaN(date.getTime())) return iso;
    const now = Date.now();
    const diffMs = now - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    if (diffDays < 1) return "Hôm nay";
    if (diffDays === 1) return "Hôm qua";
    if (diffDays < 7) return `${diffDays} ngày trước`;
    return date.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" });
  } catch {
    return iso;
  }
}
