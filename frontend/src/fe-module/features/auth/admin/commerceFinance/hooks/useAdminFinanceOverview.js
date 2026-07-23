import { useCallback, useEffect, useMemo, useState } from "react";
import {
  fetchAdminPlatformCodPipeline,
  fetchAdminPlatformFinanceSummary,
  fetchAdminPlatformPayoutOverview,
  fetchAdminPlatformRevenueTrend,
  fetchAdminPlatformTopSellers,
} from "../api/adminFinancePlatformApi";
import {
  mapPlatformCodPipeline,
  mapPlatformFinanceSummary,
  mapPlatformPayoutOverview,
  mapPlatformRevenueTrend,
  mapPlatformTopSellers,
} from "../utils/adminFinanceMapper";
import {
  buildFinanceRange,
  feeRatePercent,
  percentDelta,
  shiftFinanceRangeBack,
  zeroFillTrendPoints,
} from "../utils/financeOverviewHelpers";
import {
  FINANCE_DEFAULT_GRANULARITY,
  FINANCE_DEFAULT_RANGE_DAYS,
} from "../constants/financeOverviewConstants";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";

function emptyPanel(status = "idle") {
  return { data: null, status, errorMessage: "" };
}

export function useAdminFinanceOverview({
  from,
  to,
  granularity = FINANCE_DEFAULT_GRANULARITY,
  rangeDays = FINANCE_DEFAULT_RANGE_DAYS,
} = {}) {
  const { showSessionExpired } = useAuthSession();
  const [summary, setSummary] = useState(null);
  const [previousSummary, setPreviousSummary] = useState(null);
  const [trend, setTrend] = useState(null);
  const [codPipeline, setCodPipeline] = useState(emptyPanel());
  const [topSellers, setTopSellers] = useState(emptyPanel());
  const [payoutOverview, setPayoutOverview] = useState(emptyPanel());
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const resolvedRange = useMemo(() => {
    if (from && to) {
      return { from, to, days: rangeDays };
    }
    return buildFinanceRange(rangeDays || FINANCE_DEFAULT_RANGE_DAYS);
  }, [from, to, rangeDays]);

  const loadCore = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");
    const prevRange = shiftFinanceRangeBack(resolvedRange.from, resolvedRange.to);

    try {
      const [summaryRaw, previousRaw, trendRaw] = await Promise.all([
        fetchAdminPlatformFinanceSummary({
          from: resolvedRange.from,
          to: resolvedRange.to,
        }),
        fetchAdminPlatformFinanceSummary({
          from: prevRange.from,
          to: prevRange.to,
        }).catch(() => null),
        fetchAdminPlatformRevenueTrend({
          from: resolvedRange.from,
          to: resolvedRange.to,
          granularity,
        }),
      ]);

      const mappedSummary = mapPlatformFinanceSummary(summaryRaw);
      const mappedTrend = mapPlatformRevenueTrend(trendRaw);
      mappedTrend.points = zeroFillTrendPoints(
        mappedTrend.points,
        mappedSummary.from || resolvedRange.from,
        mappedSummary.to || resolvedRange.to,
        granularity,
      );

      setSummary(mappedSummary);
      setPreviousSummary(previousRaw ? mapPlatformFinanceSummary(previousRaw) : null);
      setTrend(mappedTrend);
      setStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setErrorMessage(error?.message || "Không tải được dữ liệu tài chính sàn.");
      setStatus("error");
    }
  }, [granularity, resolvedRange.from, resolvedRange.to, showSessionExpired]);

  const loadCod = useCallback(async () => {
    setCodPipeline((prev) => ({ ...prev, status: "loading", errorMessage: "" }));
    try {
      const raw = await fetchAdminPlatformCodPipeline();
      setCodPipeline({ data: mapPlatformCodPipeline(raw), status: "ready", errorMessage: "" });
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setCodPipeline({
        data: null,
        status: "error",
        errorMessage: error?.message || "Không tải được COD pipeline.",
      });
    }
  }, [showSessionExpired]);

  const loadTopSellers = useCallback(async () => {
    setTopSellers((prev) => ({ ...prev, status: "loading", errorMessage: "" }));
    try {
      const raw = await fetchAdminPlatformTopSellers({
        from: resolvedRange.from,
        to: resolvedRange.to,
        limit: 5,
      });
      setTopSellers({ data: mapPlatformTopSellers(raw), status: "ready", errorMessage: "" });
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setTopSellers({
        data: null,
        status: "error",
        errorMessage: error?.message || "Không tải được top sellers.",
      });
    }
  }, [resolvedRange.from, resolvedRange.to, showSessionExpired]);

  const loadPayout = useCallback(async () => {
    setPayoutOverview((prev) => ({ ...prev, status: "loading", errorMessage: "" }));
    try {
      const raw = await fetchAdminPlatformPayoutOverview({
        from: resolvedRange.from,
        to: resolvedRange.to,
      });
      setPayoutOverview({
        data: mapPlatformPayoutOverview(raw),
        status: "ready",
        errorMessage: "",
      });
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setPayoutOverview({
        data: null,
        status: "error",
        errorMessage: error?.message || "Không tải được payout overview.",
      });
    }
  }, [resolvedRange.from, resolvedRange.to, showSessionExpired]);

  const retryAll = useCallback(() => {
    loadCore();
    loadCod();
    loadTopSellers();
    loadPayout();
  }, [loadCore, loadCod, loadTopSellers, loadPayout]);

  useEffect(() => {
    loadCore();
  }, [loadCore]);

  useEffect(() => {
    loadCod();
  }, [loadCod]);

  useEffect(() => {
    loadTopSellers();
  }, [loadTopSellers]);

  useEffect(() => {
    loadPayout();
  }, [loadPayout]);

  const deltas = useMemo(() => {
    if (!summary) return null;
    const prev = previousSummary || {};
    return {
      gmv: percentDelta(summary.recognizedGmv, prev.recognizedGmv),
      fee: percentDelta(summary.totalPlatformFee, prev.totalPlatformFee),
      feeRate: percentDelta(
        feeRatePercent(summary.recognizedGmv, summary.totalPlatformFee),
        feeRatePercent(prev.recognizedGmv, prev.totalPlatformFee),
      ),
      cod: percentDelta(summary.codPipelineAmount, prev.codPipelineAmount),
      pendingPayout: percentDelta(summary.pendingPayoutAmount, prev.pendingPayoutAmount),
      paidPayout: percentDelta(summary.paidPayoutAmount, prev.paidPayoutAmount),
    };
  }, [summary, previousSummary]);

  const feeRate = useMemo(() => {
    if (!summary) return 0;
    return feeRatePercent(summary.recognizedGmv, summary.totalPlatformFee);
  }, [summary]);

  return {
    summary,
    previousSummary,
    trend,
    deltas,
    feeRate,
    codPipeline,
    topSellers,
    payoutOverview,
    status,
    errorMessage,
    isLoading: status === "loading",
    resolvedRange,
    retry: retryAll,
    retryCod: loadCod,
    retryTopSellers: loadTopSellers,
    retryPayout: loadPayout,
  };
}
