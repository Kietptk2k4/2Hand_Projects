import { useEffect, useMemo, useState } from "react";
import { loadProductCatalogEntry } from "../api/postProductTagApi";
import { getCachedProductCatalogEntry } from "../utils/productTagEnrichmentCache";
import { mergeTagWithCatalog } from "../utils/postProductTagMapper";

function tagsCacheKey(tags) {
  if (!tags?.length) return "";
  return tags.map((tag) => `${tag.productId}:${tag.price ?? ""}`).join("|");
}

function enrichFromCache(tags) {
  return (tags || [])
    .map((tag) => mergeTagWithCatalog(tag, getCachedProductCatalogEntry(tag.productId)))
    .filter(Boolean);
}

export function useEnrichedProductTags(tags) {
  const cacheKey = useMemo(() => tagsCacheKey(tags), [tags]);
  const [enrichedTags, setEnrichedTags] = useState(() => enrichFromCache(tags));

  useEffect(() => {
    const list = tags || [];
    setEnrichedTags(enrichFromCache(list));

    if (!list.length) return undefined;

    let cancelled = false;

    (async () => {
      const results = await Promise.all(
        list.map(async (tag) => {
          const cached = getCachedProductCatalogEntry(tag.productId);
          if (cached) {
            return mergeTagWithCatalog(tag, cached);
          }
          const loaded = await loadProductCatalogEntry(tag.productId);
          return mergeTagWithCatalog(tag, loaded);
        }),
      );

      if (!cancelled) {
        setEnrichedTags(results.filter(Boolean));
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [cacheKey, tags]);

  return enrichedTags;
}