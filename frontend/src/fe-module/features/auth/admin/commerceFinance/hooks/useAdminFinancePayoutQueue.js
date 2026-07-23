import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  buildAdminSearchParams,
  parseCommerceFinanceOverviewFilters,
  parseCommerceFinancePayoutQueueFilters,
} from "../../adminUrlParams.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchAdminPlatformPayoutOverview } from "../api/adminFinancePlatformApi.js";
import { fetchAdminPayoutQueue } from "../api/adminFinancePayoutApi.js";
import { mapPlatformPayoutOverview } from "../utils/adminFinanceMapper.js";
import { mapPayoutQueueResponse } from "../utils/adminFinancePayoutMapper.js";
import {
  FINANCE_DEFAULT_GRANULARITY,
  FINANCE_RANGE_PRESETS,
} from "../constants/financeOverviewConstants.js";
import { buildFinanceRange } from "../utils/financeOverviewHelpers.js";
import {
  computePayoutHeroMetrics,
  PAYOUT_QUEUE_PAGE_SIZE,
  parsePayoutPage,
} from "../utils/payoutQueueHelpers.js";

function emptyPanel(status = "idle") {
  return { data: null, status, errorMessage: "" };
}

function rangeDaysFromId(rangeId) {
  return FINANCE_RANGE_PRESETS.find((preset) => preset.id === rangeId)?.days || 30;
}

export function useAdminFinancePayoutQueue() {
  const [searchParams, setSearchParams] = useSearchParams();
  const { showSessionExpired } = useAuthSession();

  const payoutFilters = useMemo(
    () => parseCommerceFinancePayoutQueueFilters(searchParams),
    [searchParams],
  );
  const financeFilters = useMemo(
    () => parseCommerceFinanceOverviewFilters(searchParams),
    [searchParams],
  );

  const statusFilter = payoutFilters.status;
  const page = parsePayoutPage(payoutFilters.page, 1);
  const rangeId = financeFilters.range || "30d";
  const rangeDays = rangeDaysFromId(rangeId);

  const resolvedRange = useMemo(() => {
    if (financeFilters.from && financeFilters.to) {
      return { from: financeFilters.from, to: financeFilters.to, days: rangeDays };
    }
    return buildFinanceRange(rangeDays);
  }, [financeFilters.from, financeFilters.to, rangeDays]);

  const [queue, setQueue] = useState({
    items: [],
    pagination: { page: 1, limit: PAYOUT_QUEUE_PAGE_SIZE, totalItems: 0, totalPages: 1, hasNext: false },
  });
  const [listPanel, setListPanel] = useState(emptyPanel("idle"));
  const [overviewPanel, setOverviewPanel] = useState(emptyPanel("idle"));

  const updateUrl = useCallback(
    (patch = {}) => {
      const nextPayout = {
        status: patch.status !== undefined ? patch.status : statusFilter,
        page: patch.page !== undefined ? String(patch.page) : String(page),
      };
      const nextFinance = {
        from: resolvedRange.from,
        to: resolvedRange.to,
        range: rangeId,
        granularity: financeFilters.granularity || FINANCE_DEFAULT_GRANULARITY,
        limit: financeFilters.limit || "",
        ledgerPage: financeFilters.ledgerPage || "1",
        ...(patch.financeOverviewFilters || {}),
      };

      setSearchParams(
        buildAdminSearchParams({
          section: "commerceFinance",
          tab: "payout-queue",
          financeOverviewFilters: nextFinance,
          payoutQueueFilters: nextPayout,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      financeFilters.granularity,
      financeFilters.ledgerPage,
      financeFilters.limit,
      page,
      rangeId,
      resolvedRange.from,
      resolvedRange.to,
      searchParams,
      setSearchParams,
      statusFilter,
    ],
  );

  const loadList = useCallback(async () => {
    setListPanel((prev) => ({ ...prev, status: "loading", errorMessage: "" }));
    try {
      const raw = await fetchAdminPayoutQueue({
        status: statusFilter || undefined,
        page,
        limit: PAYOUT_QUEUE_PAGE_SIZE,
      });
      const mapped = mapPayoutQueueResponse(raw);
      setQueue(mapped);
      setListPanel({ data: mapped, status: "ready", errorMessage: "" });
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setListPanel({
        data: null,
        status: "error",
        errorMessage: error?.message || "Không tải được hàng đợi rút tiền.",
      });
    }
  }, [page, showSessionExpired, statusFilter]);

  const loadOverview = useCallback(async () => {
    setOverviewPanel((prev) => ({ ...prev, status: "loading", errorMessage: "" }));
    try {
      const raw = await fetchAdminPlatformPayoutOverview({
        from: resolvedRange.from,
        to: resolvedRange.to,
      });
      setOverviewPanel({
        data: mapPlatformPayoutOverview(raw),
        status: "ready",
        errorMessage: "",
      });
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setOverviewPanel({
        data: null,
        status: "error",
        errorMessage: error?.message || "Không tải được tổng quan payout.",
      });
    }
  }, [resolvedRange.from, resolvedRange.to, showSessionExpired]);

  const refreshAll = useCallback(() => {
    loadList();
    loadOverview();
  }, [loadList, loadOverview]);

  useEffect(() => {
    loadList();
  }, [loadList]);

  useEffect(() => {
    loadOverview();
  }, [loadOverview]);

  const handleStatusChange = useCallback(
    (nextStatus) => {
      updateUrl({ status: nextStatus, page: 1 });
    },
    [updateUrl],
  );

  const handlePageChange = useCallback(
    (nextPage) => {
      updateUrl({ page: Math.max(1, nextPage) });
    },
    [updateUrl],
  );

  const handleRangeChange = useCallback(
    (nextRangeId) => {
      const days = rangeDaysFromId(nextRangeId);
      const next = buildFinanceRange(days);
      updateUrl({
        page: 1,
        financeOverviewFilters: {
          range: nextRangeId,
          from: next.from,
          to: next.to,
        },
      });
    },
    [updateUrl],
  );

  const handleKpiStatusClick = useCallback(
    (statusKey) => {
      if (statusKey === "TOTAL") {
        handleStatusChange("");
        return;
      }
      handleStatusChange(statusKey);
    },
    [handleStatusChange],
  );

  const navigateToSellerDetail = useCallback(
    (sellerId) => {
      if (!sellerId) return;
      setSearchParams(
        buildAdminSearchParams({
          section: "commerceFinance",
          tab: "seller-detail",
          sellerId,
          sellerShop: sellerId,
          financeOverviewFilters: {
            from: resolvedRange.from,
            to: resolvedRange.to,
            range: rangeId,
            granularity: financeFilters.granularity || FINANCE_DEFAULT_GRANULARITY,
          },
          preserve: searchParams,
        }),
        { replace: false },
      );
    },
    [
      financeFilters.granularity,
      rangeId,
      resolvedRange.from,
      resolvedRange.to,
      searchParams,
      setSearchParams,
    ],
  );

  const heroMetrics = useMemo(
    () => computePayoutHeroMetrics(overviewPanel.data),
    [overviewPanel.data],
  );

  const setQueueState = setQueue;

  return {
    statusFilter,
    page,
    rangeId,
    resolvedRange,
    queue,
    setQueueState,
    listPanel,
    overviewPanel,
    heroMetrics,
    isListLoading: listPanel.status === "loading" || listPanel.status === "idle",
    isOverviewLoading: overviewPanel.status === "loading" || overviewPanel.status === "idle",
    handleStatusChange,
    handlePageChange,
    handleRangeChange,
    handleKpiStatusClick,
    refreshAll,
    retryList: loadList,
    retryOverview: loadOverview,
    navigateToSellerDetail,
    updateUrl,
  };
}
