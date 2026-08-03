import { mapProductItem } from "./productListMapper";

export function mapFlashSaleResponse(data) {
  const items = (data?.items || []).map(mapProductItem).filter(Boolean);
  const pagination = data?.pagination || {};
  return {
    items,
    pagination: {
      page: pagination.page,
      limit: pagination.limit,
      totalItems: pagination.total_items ?? pagination.totalItems,
      totalPages: pagination.total_pages ?? pagination.totalPages,
      hasNext: Boolean(pagination.has_next ?? pagination.hasNext),
    },
    slotStart: data?.slot_start ?? data?.slotStart ?? null,
    slotEnd: data?.slot_end ?? data?.slotEnd ?? null,
  };
}
