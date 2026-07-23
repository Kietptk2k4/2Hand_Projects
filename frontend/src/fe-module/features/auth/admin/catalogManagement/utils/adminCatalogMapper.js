export function mapCategory(item) {
  if (!item) return null;
  return {
    id: item.id,
    name: item.name,
    slug: item.slug,
    parentId: item.parent_id ?? item.parentId ?? null,
    level: item.level ?? 0,
    path: item.path ?? "",
    active: item.is_active ?? item.isActive ?? true,
    productCount: Number(item.product_count ?? item.productCount) || 0,
    createdAt: item.created_at ?? item.createdAt ?? null,
    updatedAt: item.updated_at ?? item.updatedAt ?? null,
  };
}

export function mapCategoryList(raw) {
  return (raw?.items ?? []).map(mapCategory).filter(Boolean);
}

export function mapBrand(item) {
  if (!item) return null;
  return {
    id: item.id,
    name: item.name,
    slug: item.slug,
    active: item.is_active ?? item.isActive ?? true,
    productCount: Number(item.product_count ?? item.productCount) || 0,
    createdAt: item.created_at ?? item.createdAt ?? null,
    updatedAt: item.updated_at ?? item.updatedAt ?? null,
  };
}

export function mapBrandListResponse(raw) {
  const pagination = raw?.pagination ?? {};
  const page = Number(pagination.page) || 1;
  const limit = Number(pagination.limit) || 20;
  const totalItems = Number(pagination.total_items ?? pagination.totalItems) || 0;

  return {
    items: (raw?.items ?? []).map(mapBrand).filter(Boolean),
    pagination: {
      page,
      limit,
      totalItems,
      totalPages: Math.max(1, Math.ceil(totalItems / limit) || 1),
    },
  };
}
