import { useCallback, useMemo } from "react";
import { useSearchParams } from "react-router-dom";
import {
  buildAdminSearchParams,
  parseCommerceFinanceOverviewFilters,
} from "../../../adminUrlParams.js";
import {
  FINANCE_DEFAULT_GRANULARITY,
  FINANCE_RANGE_PRESETS,
} from "../../constants/financeOverviewConstants.js";
import { useAdminFinanceOverview } from "../../hooks/useAdminFinanceOverview";
import { buildFinanceRange } from "../../utils/financeOverviewHelpers.js";
import { AdminFinanceOverviewView } from "../AdminFinanceOverviewView.jsx";
import { CommerceFinanceRetryPanel } from "../ui/CommerceFinanceRetryPanel.jsx";

function rangeDaysFromId(rangeId) {
  return FINANCE_RANGE_PRESETS.find((preset) => preset.id === rangeId)?.days || 30;
}

export function AdminFinanceOverviewTab() {
  const [searchParams, setSearchParams] = useSearchParams();
  const filters = useMemo(
    () => parseCommerceFinanceOverviewFilters(searchParams),
    [searchParams],
  );

  const rangeId = filters.range || "30d";
  const rangeDays = rangeDaysFromId(rangeId);

  const resolved = useMemo(() => {
    if (filters.from && filters.to) {
      return { from: filters.from, to: filters.to, days: rangeDays };
    }
    return buildFinanceRange(rangeDays);
  }, [filters.from, filters.to, rangeDays]);

  const granularity = filters.granularity || FINANCE_DEFAULT_GRANULARITY;

  const {
    summary,
    trend,
    deltas,
    feeRate,
    codPipeline,
    topSellers,
    payoutOverview,
    status: coreStatus,
    isLoading,
    errorMessage,
    retry,
    retryCod,
    retryTopSellers,
    retryPayout,
  } = useAdminFinanceOverview({
    from: resolved.from,
    to: resolved.to,
    granularity,
    rangeDays,
  });

  const updateFilters = useCallback(
    (patch) => {
      const nextFilters = {
        from: resolved.from,
        to: resolved.to,
        granularity,
        range: rangeId,
        ...patch,
      };
      setSearchParams(
        buildAdminSearchParams({
          section: "commerceFinance",
          tab: "finance-overview",
          financeOverviewFilters: nextFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [granularity, rangeId, resolved.from, resolved.to, searchParams, setSearchParams],
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

  const handleGranularityChange = useCallback(
    (nextGranularity) => {
      updateFilters({ granularity: nextGranularity });
    },
    [updateFilters],
  );

  const navigateToTab = useCallback(
    (tab) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "commerceFinance",
          tab,
          financeOverviewFilters: {
            from: resolved.from,
            to: resolved.to,
            granularity,
            range: rangeId,
          },
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [granularity, rangeId, resolved.from, resolved.to, searchParams, setSearchParams],
  );

  if (errorMessage && !summary) {
    return <CommerceFinanceRetryPanel message={errorMessage} onRetry={retry} />;
  }

  return (
    <AdminFinanceOverviewView
      summary={summary}
      trend={trend}
      deltas={deltas}
      feeRate={feeRate}
      codPipeline={codPipeline}
      topSellers={topSellers}
      payoutOverview={payoutOverview}
      coreStatus={coreStatus}
      coreErrorMessage={errorMessage}
      isLoading={isLoading}
      activeRangeId={rangeId}
      granularity={granularity}
      onRangeChange={handleRangeChange}
      onGranularityChange={handleGranularityChange}
      onRetry={retry}
      onRetryCod={retryCod}
      onRetryTopSellers={retryTopSellers}
      onRetryPayout={retryPayout}
      onNavigateCod={() => navigateToTab("cod-pipeline")}
      onNavigatePayout={() => navigateToTab("payout-queue")}
      onNavigateTopSellers={() => navigateToTab("top-sellers")}
    />
  );
}
