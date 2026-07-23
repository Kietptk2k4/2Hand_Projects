import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  buildAdminSearchParams,
  parseCommerceFinanceOverviewFilters,
  parseCommerceFinanceSellerShop,
} from "../../../adminUrlParams.js";
import {
  FINANCE_DEFAULT_GRANULARITY,
  FINANCE_RANGE_PRESETS,
} from "../../constants/financeOverviewConstants.js";
import {
  fetchAdminSellerFinanceLedger,
  fetchAdminSellerFinanceSummary,
} from "../../api/adminFinancePlatformApi";
import { mapSellerFinanceLedger, mapSellerFinanceSummary } from "../../utils/adminFinanceMapper";
import { buildFinanceRange } from "../../utils/financeOverviewHelpers.js";
import {
  computeSellerDetailHeroMetrics,
  computeSellerBucketShares,
  parseLedgerPage,
  SELLER_LEDGER_PAGE_SIZE,
  summaryToPipelineShape,
} from "../../utils/sellerDetailHelpers.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { AdminFinanceSellerDetailView } from "../AdminFinanceSellerDetailView.jsx";

function rangeDaysFromId(rangeId) {
  return FINANCE_RANGE_PRESETS.find((preset) => preset.id === rangeId)?.days || 30;
}

export function AdminFinanceSellerDetailTab({ sellerId: sellerIdProp }) {
  const [searchParams, setSearchParams] = useSearchParams();
  const { showSessionExpired } = useAuthSession();
  const [summary, setSummary] = useState(null);
  const [ledger, setLedger] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const sellerId = sellerIdProp || "";
  const sellerShop = parseCommerceFinanceSellerShop(searchParams);

  const filters = useMemo(
    () => parseCommerceFinanceOverviewFilters(searchParams),
    [searchParams],
  );

  const rangeId = filters.range || "30d";
  const rangeDays = rangeDaysFromId(rangeId);
  const ledgerPage = parseLedgerPage(filters.ledgerPage, 1);

  const resolved = useMemo(() => {
    if (filters.from && filters.to) {
      return { from: filters.from, to: filters.to, days: rangeDays };
    }
    return buildFinanceRange(rangeDays);
  }, [filters.from, filters.to, rangeDays]);

  const load = useCallback(async () => {
    if (!sellerId) {
      setSummary(null);
      setLedger(null);
      setStatus("ready");
      return;
    }
    setStatus("loading");
    setErrorMessage("");
    try {
      const [summaryRaw, ledgerRaw] = await Promise.all([
        fetchAdminSellerFinanceSummary(sellerId, {
          from: resolved.from,
          to: resolved.to,
        }),
        fetchAdminSellerFinanceLedger(sellerId, {
          page: ledgerPage,
          limit: SELLER_LEDGER_PAGE_SIZE,
        }),
      ]);
      setSummary(mapSellerFinanceSummary(summaryRaw));
      setLedger(mapSellerFinanceLedger(ledgerRaw));
      setStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setErrorMessage(error?.message || "Không tải được dữ liệu seller.");
      setStatus("error");
    }
  }, [ledgerPage, resolved.from, resolved.to, sellerId, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  const updateFilters = useCallback(
    (patch) => {
      const nextFilters = {
        from: resolved.from,
        to: resolved.to,
        range: rangeId,
        granularity: filters.granularity || FINANCE_DEFAULT_GRANULARITY,
        limit: filters.limit || "",
        ledgerPage: String(ledgerPage),
        ...patch,
      };
      setSearchParams(
        buildAdminSearchParams({
          section: "commerceFinance",
          tab: "seller-detail",
          sellerId,
          sellerShop: sellerShop || undefined,
          financeOverviewFilters: nextFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      filters.granularity,
      filters.limit,
      ledgerPage,
      rangeId,
      resolved.from,
      resolved.to,
      searchParams,
      sellerId,
      sellerShop,
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
        ledgerPage: "1",
      });
    },
    [updateFilters],
  );

  const handleLedgerPageChange = useCallback(
    (nextPage) => {
      updateFilters({ ledgerPage: String(Math.max(1, nextPage)) });
    },
    [updateFilters],
  );

  const handleSubmitSellerId = useCallback(
    (nextSellerId) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "commerceFinance",
          tab: "seller-detail",
          sellerId: nextSellerId,
          financeOverviewFilters: {
            from: resolved.from,
            to: resolved.to,
            range: rangeId,
            granularity: filters.granularity || FINANCE_DEFAULT_GRANULARITY,
            ledgerPage: "1",
          },
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      filters.granularity,
      rangeId,
      resolved.from,
      resolved.to,
      searchParams,
      setSearchParams,
    ],
  );

  const handleOpenTopSellers = useCallback(() => {
    setSearchParams(
      buildAdminSearchParams({
        section: "commerceFinance",
        tab: "top-sellers",
        financeOverviewFilters: {
          from: resolved.from,
          to: resolved.to,
          range: rangeId,
          granularity: filters.granularity || FINANCE_DEFAULT_GRANULARITY,
          limit: filters.limit || "",
        },
        preserve: searchParams,
      }),
      { replace: true },
    );
  }, [
    filters.granularity,
    filters.limit,
    rangeId,
    resolved.from,
    resolved.to,
    searchParams,
    setSearchParams,
  ]);

  const heroMetrics = useMemo(() => computeSellerDetailHeroMetrics(summary), [summary]);
  const bucketShares = useMemo(() => computeSellerBucketShares(summary), [summary]);
  const pipeline = useMemo(() => summaryToPipelineShape(summary), [summary]);

  return (
    <AdminFinanceSellerDetailView
      sellerId={sellerId}
      sellerShop={sellerShop}
      status={status}
      errorMessage={errorMessage}
      summary={summary}
      heroMetrics={heroMetrics}
      pipeline={pipeline}
      bucketShares={bucketShares}
      ledgerItems={ledger?.items ?? []}
      ledgerPagination={ledger?.pagination}
      from={resolved.from}
      to={resolved.to}
      activeRangeId={rangeId}
      onRangeChange={handleRangeChange}
      onRetry={load}
      onLedgerPageChange={handleLedgerPageChange}
      onSubmitSellerId={handleSubmitSellerId}
      onOpenTopSellers={handleOpenTopSellers}
      onBackToTopSellers={handleOpenTopSellers}
    />
  );
}
