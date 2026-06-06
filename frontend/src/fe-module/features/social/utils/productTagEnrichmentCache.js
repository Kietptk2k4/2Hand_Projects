const catalogCache = new Map();

export function getCachedProductCatalogEntry(productId) {
  if (!productId) return null;
  return catalogCache.get(productId) ?? null;
}

export function setCachedProductCatalogEntry(productId, entry) {
  if (!productId || !entry) return;
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