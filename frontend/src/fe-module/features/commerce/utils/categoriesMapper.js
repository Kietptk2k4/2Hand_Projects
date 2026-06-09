export function mapCategorySummaryItem(item) {
  if (!item?.category_id) return null;

  return {
    categoryId: item.category_id,
    categoryName: item.category_name || "",
    categorySlug: item.category_slug || "",
    parentId: item.parent_id || null,
    level: item.level ?? 0,
    isLeaf: Boolean(item.is_leaf),
    productCount: item.product_count ?? 0,
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
