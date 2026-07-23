import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  buildAdminSearchParams,
  parseCatalogCategoryFilters,
} from "../../adminUrlParams.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchAdminCategories } from "../api/adminCatalogApi.js";
import { isCatalogForbiddenError } from "../constants/catalogPermissions.js";
import { mapCategoryList } from "../utils/adminCatalogMapper.js";
import {
  buildCategoryIndex,
  buildCategoryTree,
  computeCategoryHeroMetrics,
  getDefaultExpandedIds,
  statusFilterToIsActive,
} from "../utils/categoryHelpers.js";

const SEARCH_DEBOUNCE_MS = 300;

export function useCatalogCategories({ canRead }) {
  const [searchParams, setSearchParams] = useSearchParams();
  const { showSessionExpired } = useAuthSession();

  const filters = useMemo(
    () => parseCatalogCategoryFilters(searchParams),
    [searchParams],
  );

  const [debouncedQuery, setDebouncedQuery] = useState(filters.q);
  const [items, setItems] = useState([]);
  const [allItems, setAllItems] = useState([]);
  const [loadStatus, setLoadStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [expandedIds, setExpandedIds] = useState(() => new Set());

  useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedQuery(filters.q), SEARCH_DEBOUNCE_MS);
    return () => window.clearTimeout(timer);
  }, [filters.q]);

  const updateFilters = useCallback(
    (patch = {}) => {
      const next = {
        q: patch.q !== undefined ? patch.q : filters.q,
        status: patch.status !== undefined ? patch.status : filters.status,
      };
      setSearchParams(
        buildAdminSearchParams({
          section: "catalogManagement",
          tab: "categories",
          catalogCategoryFilters: next,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [filters.q, filters.status, searchParams, setSearchParams],
  );

  const loadAll = useCallback(async () => {
    try {
      const data = await fetchAdminCategories();
      const mapped = mapCategoryList(data);
      setAllItems(mapped);
      return mapped;
    } catch {
      return [];
    }
  }, []);

  const load = useCallback(async () => {
    if (!canRead) return;
    setLoadStatus("loading");
    setErrorMessage("");
    try {
      const [listRaw] = await Promise.all([
        fetchAdminCategories({
          q: debouncedQuery || undefined,
          isActive: statusFilterToIsActive(filters.status),
        }),
        loadAll(),
      ]);
      const mapped = mapCategoryList(listRaw);
      setItems(mapped);
      setExpandedIds((prev) => {
        if (prev.size) return prev;
        return getDefaultExpandedIds(mapped, 1);
      });
      setLoadStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      if (isCatalogForbiddenError(error)) {
        setErrorMessage(error?.message || "Tài khoản thiếu quyền CATALOG_READ.");
        setLoadStatus("forbidden");
        return;
      }
      setErrorMessage(error?.message || "Không tải được danh mục.");
      setLoadStatus("error");
    }
  }, [canRead, debouncedQuery, filters.status, loadAll, showSessionExpired]);

  useEffect(() => {
    if (canRead) load();
  }, [canRead, load]);

  const heroMetrics = useMemo(
    () => computeCategoryHeroMetrics(allItems.length ? allItems : items),
    [allItems, items],
  );

  const categoryIndex = useMemo(() => buildCategoryIndex(allItems.length ? allItems : items), [allItems, items]);
  const tree = useMemo(() => buildCategoryTree(items), [items]);

  const toggleExpanded = useCallback((categoryId) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(categoryId)) next.delete(categoryId);
      else next.add(categoryId);
      return next;
    });
  }, []);

  const handleQueryChange = useCallback(
    (value) => {
      updateFilters({ q: value });
    },
    [updateFilters],
  );

  const handleStatusChange = useCallback(
    (status) => {
      updateFilters({ status });
    },
    [updateFilters],
  );

  const handleKpiStatusClick = useCallback(
    (statusKey) => {
      if (statusKey === "total") {
        handleStatusChange("");
        return;
      }
      if (statusKey === "active") {
        handleStatusChange("active");
        return;
      }
      if (statusKey === "inactive") {
        handleStatusChange("inactive");
      }
    },
    [handleStatusChange],
  );

  const refresh = useCallback(() => {
    load();
  }, [load]);

  return {
    filters,
    debouncedQuery,
    items,
    allItems,
    tree,
    categoryIndex,
    expandedIds,
    heroMetrics,
    loadStatus,
    errorMessage,
    handleQueryChange,
    handleStatusChange,
    handleKpiStatusClick,
    toggleExpanded,
    refresh,
    setItems,
  };
}
