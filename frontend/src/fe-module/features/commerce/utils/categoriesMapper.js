export function mapCategorySummaryItem(item) {
  if (!item?.category_id && !item?.categoryId) return null;

  return {
    categoryId: item.category_id ?? item.categoryId,
    categoryName: item.category_name ?? item.categoryName ?? "",
    categorySlug: item.category_slug ?? item.categorySlug ?? "",
    parentId: item.parent_id ?? item.parentId ?? null,
    level: item.level ?? 0,
    isLeaf: Boolean(item.is_leaf ?? item.isLeaf),
    productCount: item.product_count ?? item.productCount ?? 0,
  };
}

export function mapActiveCategoriesResponse(data) {
  return (data?.items || []).map(mapCategorySummaryItem).filter(Boolean);
}

export function toSellerCategoryOptions(categories) {
  return categories
    .filter((item) => item.isLeaf)
    .map((item) => ({
      id: item.categoryId,
      name: item.categoryName,
    }));
}

export function toHomeNavItems(categories) {
  return categories
    .filter((item) => item.level === 1)
    .map((item) => ({
      label: item.categoryName,
      categoryId: item.categoryId,
    }));
}

export function toSidebarCategoryItems(categories) {
  return categories.map((item) => ({
    categoryId: item.categoryId,
    categoryName: item.categoryName,
    categorySlug: item.categorySlug,
    parentId: item.parentId,
    productCount: item.productCount,
  }));
}
