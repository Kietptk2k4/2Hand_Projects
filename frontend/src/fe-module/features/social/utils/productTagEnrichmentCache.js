const catalogCache = new Map();
const missingProductIds = new Set();

export function getCachedProductCatalogEntry(productId) {
  if (!productId) return null;
  return catalogCache.get(productId) ?? null;
}

export function isProductCatalogEntryMissing(productId) {
  if (!productId) return false;
  return missingProductIds.has(productId);
}

export function markProductCatalogEntryMissing(productId) {
  if (!productId) return;
  missingProductIds.add(productId);
}

export function setCachedProductCatalogEntry(productId, entry) {
  if (!productId || !entry) return;
  missingProductIds.delete(productId);
  catalogCache.set(productId, entry);
}

export function seedProductCatalogCache(entries) {
  if (!Array.isArray(entries)) return;
  for (const entry of entries) {
    if (entry?.productId) {
      catalogCache.set(entry.productId, entry);
    }
  }
}