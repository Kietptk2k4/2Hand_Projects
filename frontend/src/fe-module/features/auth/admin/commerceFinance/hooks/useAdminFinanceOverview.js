import { useCallback, useEffect, useState } from "react";
import {
  fetchAdminPlatformFinanceSummary,
  fetchAdminPlatformRevenueTrend,
} from "../api/adminFinancePlatformApi";
import { mapPlatformFinanceSummary, mapPlatformRevenueTrend } from "../utils/adminFinanceMapper";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";

export function useAdminFinanceOverview({ granularity = "DAY" } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [summary, setSummary] = useState(null);
  const [trend, setTrend] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");
    try {
      const [summaryRaw, trendRaw] = await Promise.all([
        fetchAdminPlatformFinanceSummary(),
        fetchAdminPlatformRevenueTrend({ granularity }),
      ]);
      setSummary(mapPlatformFinanceSummary(summaryRaw));
      setTrend(mapPlatformRevenueTrend(trendRaw));
      setStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setErrorMessage(error?.message || "Không tải được dữ liệu tài chính sàn.");
      setStatus("error");
    }
  }, [granularity, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  return { summary, trend, status, errorMessage, isLoading: status === "loading", retry: load };
}
