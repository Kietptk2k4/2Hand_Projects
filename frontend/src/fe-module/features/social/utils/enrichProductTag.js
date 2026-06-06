import { getCachedProductCatalogEntry } from "./productTagEnrichmentCache";
import { mergeTagWithCatalog } from "./postProductTagMapper";

/**
 * Enrich tag từ cache (sync). Dùng useEnrichedProductTags khi cần fetch Commerce.
 */
export function enrichProductTag(tag) {
  if (!tag?.productId) return null;
  return mergeTagWithCatalog(tag, getCachedProductCatalogEntry(tag.productId));
}

export function enrichProductTags(tags) {
  if (!Array.isArray(tags) || tags.length === 0) return [];
  return tags.map(enrichProductTag).filter(Boolean);
}
