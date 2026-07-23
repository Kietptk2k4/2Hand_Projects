import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  buildAdminSearchParams,
  parseCatalogBrandFilters,
} from "../../adminUrlParams.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchAdminBrands } from "../api/adminCatalogApi.js";
import { isCatalogForbiddenError } from "../constants/catalogPermissions.js";
import { mapBrandListResponse } from "../utils/adminCatalogMapper.js";
import {
  BRAND_PAGE_SIZE,
  computeBrandHeroMetrics,
  parseBrandPage,
  statusFilterToIsActive,
} from "../utils/brandHelpers.js";

const SEARCH_DEBOUNCE_MS = 300;
const METRICS_FETCH_LIMIT = 200;

export function useCatalogBrands({ canRead }) {
  const [searchParams, setSearchParams] = useSearchParams();
  const { showSessionExpired } = useAuthSession();

  const filters = useMemo(
    () => parseCatalogBrandFilters(searchParams),
    [searchParams],
  );

  const page = parseBrandPage(filters.page, 1);
  const [debouncedQuery, setDebouncedQuery] = useState(filters.q);
  const [listState, setListState] = useState({
    items: [],
    pagination: { page: 1, limit: BRAND_PAGE_SIZE, totalItems: 0, totalPages: 1 },
  });
  const [heroMetrics, setHeroMetrics] = useState(
    computeBrandHeroMetrics({ total: 0, active: 0, inactive: 0, totalProducts: 0 }),
  );
  const [loadStatus, setLoadStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedQuery(filters.q), SEARCH_DEBOUNCE_MS);
    return () => window.clearTimeout(timer);
  }, [filters.q]);

  const updateFilters = useCallback(
    (patch = {}) => {
      const next = {
        q: patch.q !== undefined ? patch.q : filters.q,
        status: patch.status !== undefined ? patch.status : filters.status,
        page: patch.page !== undefined ? String(patch.page) : String(page),
      };
      setSearchParams(
        buildAdminSearchParams({
          section: "catalogManagement",
          tab: "brands",
          catalogBrandFilters: next,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [filters.q, filters.status, page, searchParams, setSearchParams],
  );

  const loadHeroMetrics = useCallback(async () => {
    try {
      const [allRaw, activeRaw, inactiveRaw] = await Promise.all([
        fetchAdminBrands({ page: 1, limit: METRICS_FETCH_LIMIT }),
        fetchAdminBrands({ isActive: true, page: 1, limit: METRICS_FETCH_LIMIT }),
        fetchAdminBrands({ isActive: false, page: 1, limit: METRICS_FETCH_LIMIT }),
      ]);
      const all = mapBrandListResponse(allRaw);
      const active = mapBrandListResponse(activeRaw);
      const inactive = mapBrandListResponse(inactiveRaw);
      const totalProducts = all.items.reduce(
        (sum, item) => sum + (Number(item.productCount) || 0),
        0,
      );
      setHeroMetrics(
        computeBrandHeroMetrics({
          total: all.pagination.totalItems || all.items.length,
          active: active.pagination.totalItems || active.items.length,
          inactive: inactive.pagination.totalItems || inactive.items.length,
          totalProducts,
        }),
      );
    } catch {
      setHeroMetrics(computeBrandHeroMetrics({}));
    }
  }, []);

  const load = useCallback(async () => {
    if (!canRead) return;
    setLoadStatus("loading");
    setErrorMessage("");
    try {
      const raw = await fetchAdminBrands({
        q: debouncedQuery || undefined,
        isActive: statusFilterToIsActive(filters.status),
        page,
        limit: BRAND_PAGE_SIZE,
      });
      setListState(mapBrandListResponse(raw));
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
      setErrorMessage(error?.message || "Không tải được thương hiệu.");
      setLoadStatus("error");
    }
  }, [canRead, debouncedQuery, filters.status, page, showSessionExpired]);

  const refreshAll = useCallback(() => {
    load();
    loadHeroMetrics();
  }, [load, loadHeroMetrics]);

  useEffect(() => {
    if (canRead) load();
  }, [canRead, load]);

  useEffect(() => {
    if (canRead) loadHeroMetrics();
  }, [canRead, loadHeroMetrics]);

  const handleQueryChange = useCallback(
    (value) => {
      updateFilters({ q: value, page: 1 });
    },
    [updateFilters],
  );

  const handleStatusChange = useCallback(
    (status) => {
      updateFilters({ status, page: 1 });
    },
    [updateFilters],
  );

  const handlePageChange = useCallback(
    (nextPage) => {
      updateFilters({ page: Math.max(1, nextPage) });
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

  return {
    filters,
    page,
    items: listState.items,
    pagination: listState.pagination,
    heroMetrics,
    loadStatus,
    errorMessage,
    handleQueryChange,
    handleStatusChange,
    handlePageChange,
    handleKpiStatusClick,
    refreshAll,
    setListState,
  };
}
