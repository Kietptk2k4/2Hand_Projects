import { mapProductItem } from "./productListMapper";

export function mapCategoryProductsResponse(data) {
  const items = (data?.items || []).map(mapProductItem).filter(Boolean);
  const pagination = data?.pagination || {};

  return {
    category: {
      categoryId: data?.category_id,
      categoryName: data?.category_name,
      categorySlug: data?.category_slug,
      includeChildren: data?.include_children,
    },
    items,
    pagination: {
      page: pagination.page,
      limit: pagination.limit,
      totalItems: pagination.total_items,
      totalPages: pagination.total_pages,
      hasNext: pagination.has_next,
    },
  };
}
