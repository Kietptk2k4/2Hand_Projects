import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  buildAdminSearchParams,
  parseCommerceFinanceOverviewFilters,
} from "../../../adminUrlParams.js";
import {
  FINANCE_DEFAULT_TOP_SELLERS_LIMIT,
  FINANCE_RANGE_PRESETS,
} from "../../constants/financeOverviewConstants.js";
import { fetchAdminPlatformTopSellers } from "../../api/adminFinancePlatformApi";
import { mapPlatformTopSellers } from "../../utils/adminFinanceMapper";
import { buildFinanceRange } from "../../utils/financeOverviewHelpers.js";
import {
  computeTopSellersListMetrics,
  parseTopSellersLimit,
} from "../../utils/topSellersHelpers.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { AdminFinanceTopSellersView } from "../AdminFinanceTopSellersView.jsx";

function rangeDaysFromId(rangeId) {
  return FINANCE_RANGE_PRESETS.find((preset) => preset.id === rangeId)?.days || 30;
}

export function AdminFinanceTopSellersTab() {
  const [searchParams, setSearchParams] = useSearchParams();
  const { showSessionExpired } = useAuthSession();
  const [sellers, setSellers] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const filters = useMemo(
    () => parseCommerceFinanceOverviewFilters(searchParams),
    [searchParams],
  );

  const rangeId = filters.range || "30d";
  const rangeDays = rangeDaysFromId(rangeId);
  const limit = parseTopSellersLimit(filters.limit, FINANCE_DEFAULT_TOP_SELLERS_LIMIT);

  const resolved = useMemo(() => {
    if (filters.from && filters.to) {
      return { from: filters.from, to: filters.to, days: rangeDays };
    }
    return buildFinanceRange(rangeDays);
  }, [filters.from, filters.to, rangeDays]);

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");
    try {
      const raw = await fetchAdminPlatformTopSellers({
        from: resolved.from,
        to: resolved.to,
        limit,
      });
      setSellers(mapPlatformTopSellers(raw));
      setStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setErrorMessage(error?.message || "Không tải được top sellers.");
      setStatus("error");
    }
  }, [limit, resolved.from, resolved.to, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  const updateFilters = useCallback(
    (patch) => {
      const nextFilters = {
        from: resolved.from,
        to: resolved.to,
        range: rangeId,
        limit,
        granularity: filters.granularity || "DAY",
        ...patch,
      };
      setSearchParams(
        buildAdminSearchParams({
          section: "commerceFinance",
          tab: "top-sellers",
          financeOverviewFilters: nextFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      filters.granularity,
      limit,
      rangeId,
      resolved.from,
      resolved.to,
      searchParams,
      setSearchParams,
    ],
  );

  const handleRangeChange = useCallback(
    (nextRangeId) => {
      const days = rangeDaysFromId(nextRangeId);
      const next = buildFinanceRange(days);
      updateFilters({
        range: nextRangeId,
        from: next.from,
        to: next.to,
      });
    },
    [updateFilters],
  );

  const handleLimitChange = useCallback(
    (nextLimit) => {
      updateFilters({ limit: nextLimit });
    },
    [updateFilters],
  );

  const openSellerDetail = useCallback(
    (sellerId, shopName) => {
      const resolvedShop =
        shopName || sellers.find((seller) => seller.sellerId === sellerId)?.shopName || "";
      setSearchParams(
        buildAdminSearchParams({
          section: "commerceFinance",
          tab: "seller-detail",
          sellerId,
          sellerShop: resolvedShop || undefined,
          financeOverviewFilters: {
            from: resolved.from,
            to: resolved.to,
            range: rangeId,
            limit,
            granularity: filters.granularity || "DAY",
            ledgerPage: "1",
          },
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      filters.granularity,
      limit,
      rangeId,
      resolved.from,
      resolved.to,
      searchParams,
      sellers,
      setSearchParams,
    ],
  );

  const metrics = useMemo(() => computeTopSellersListMetrics(sellers), [sellers]);

  return (
    <AdminFinanceTopSellersView
      status={status}
      errorMessage={errorMessage}
      sellers={sellers}
      metrics={metrics}
      from={resolved.from}
      to={resolved.to}
      activeRangeId={rangeId}
      limit={limit}
      onRangeChange={handleRangeChange}
      onLimitChange={handleLimitChange}
      onRetry={load}
      onSellerSelect={openSellerDetail}
    />
  );
}
