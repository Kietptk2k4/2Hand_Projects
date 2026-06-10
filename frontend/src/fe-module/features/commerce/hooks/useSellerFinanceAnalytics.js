import { useCallback, useEffect, useState } from "react";
import {
  fetchSellerLedger,
  fetchSellerRevenueSummary,
  fetchSellerRevenueTrend,
} from "../api/sellerFinanceApi";
import {
  mapSellerLedgerResponse,
  mapSellerRevenueSummaryResponse,
  mapSellerRevenueTrendResponse,
} from "../utils/sellerFinanceMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function useSellerFinanceAnalytics({ granularity = "DAY" } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [summary, setSummary] = useState(null);
  const [trend, setTrend] = useState(null);
  const [ledger, setLedger] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const [summaryRaw, trendRaw, ledgerRaw] = await Promise.all([
        fetchSellerRevenueSummary(),
        fetchSellerRevenueTrend({ granularity }),
        fetchSellerLedger({ page: 1, limit: 10 }),
      ]);
      setSummary(mapSellerRevenueSummaryResponse(summaryRaw));
      setTrend(mapSellerRevenueTrendResponse(trendRaw));
      setLedger(mapSellerLedgerResponse(ledgerRaw));
      setStatus("ready");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return;
      }
      setSummary(null);
      setTrend(null);
      setLedger(null);
      setErrorMessage(error?.message || "Không tải được dữ liệu thống kê.");
      setStatus("error");
    }
  }, [granularity, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    summary,
    trend,
    ledger,
    status,
    errorMessage,
    isLoading: status === "loading",
    retry: load,
  };
}
