import { mapProductItem } from "./productListMapper";

export function mapFlashSaleResponse(data) {
  const items = (data?.items || []).map(mapProductItem).filter(Boolean);
  return {
    items,
    slotStart: data?.slot_start ?? data?.slotStart ?? null,
    slotEnd: data?.slot_end ?? data?.slotEnd ?? null,
  };
}
