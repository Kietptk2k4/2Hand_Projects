import { useCallback, useEffect, useMemo, useState } from "react";
import { fetchActiveCategories } from "../api/categoriesApi";
import { mapActiveCategoriesResponse, toHomeNavItems } from "../utils/categoriesMapper";

const cache = new Map();

function buildCacheKey(options) {
  return JSON.stringify({
    minLevel: options.minLevel ?? null,
    maxLevel: options.maxLevel ?? null,
    leafOnly: options.leafOnly ?? null,
    includeProductCounts: options.includeProductCounts ?? true,
  });
}

async function loadCategories(options) {
  const cacheKey = buildCacheKey(options);
  if (cache.has(cacheKey)) {
    return cache.get(cacheKey);
  }

  const request = fetchActiveCategories(options).then((raw) => {
    const mapped = mapActiveCategoriesResponse(raw);
    cache.set(cacheKey, mapped);
    return mapped;
  });

  cache.set(cacheKey, request);
  try {
    return await request;
  } catch (error) {
    cache.delete(cacheKey);
    throw error;
  }
}

export function useCommerceCategories(options = {}) {
  const {
    minLevel,
    maxLevel,
    leafOnly,
    includeProductCounts = true,
    enabled = true,
  } = options;

  const [categories, setCategories] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const requestOptions = useMemo(
    () => ({
      minLevel,
      maxLevel,
      leafOnly,
      includeProductCounts,
    }),
    [minLevel, maxLevel, leafOnly, includeProductCounts]
  );

  const reload = useCallback(async () => {
    if (!enabled) {
      setCategories([]);
      return;
    }

    setIsLoading(true);
    setErrorMessage("");
    try {
      const items = await loadCategories(requestOptions);
      setCategories(items);
    } catch (error) {
      setCategories([]);
      setErrorMessage(error?.message || "Không thể tải danh mục.");
    } finally {
      setIsLoading(false);
    }
  }, [enabled, requestOptions]);

  useEffect(() => {
    reload();
  }, [reload]);

  const homeNavItems = useMemo(() => toHomeNavItems(categories), [categories]);

  return {
    categories,
    homeNavItems,
    isLoading,
    errorMessage,
    reload,
  };
}
