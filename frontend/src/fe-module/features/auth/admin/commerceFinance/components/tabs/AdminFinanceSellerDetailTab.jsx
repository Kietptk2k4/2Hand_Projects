import { useCallback, useEffect, useState } from "react";
import {
  fetchAdminSellerFinanceLedger,
  fetchAdminSellerFinanceSummary,
} from "../../api/adminFinancePlatformApi";
import { mapSellerFinanceLedger, mapSellerFinanceSummary } from "../../utils/adminFinanceMapper";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { AdminFinanceSellerDetailView } from "../AdminFinanceSellerDetailView.jsx";

export function AdminFinanceSellerDetailTab({ sellerId }) {
  const { showSessionExpired } = useAuthSession();
  const [summary, setSummary] = useState(null);
  const [ledger, setLedger] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    if (!sellerId) {
      setStatus("ready");
      return;
    }
    setStatus("loading");
    setErrorMessage("");
    try {
      const [summaryRaw, ledgerRaw] = await Promise.all([
        fetchAdminSellerFinanceSummary(sellerId),
        fetchAdminSellerFinanceLedger(sellerId, { page: 1, limit: 10 }),
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
  }, [sellerId, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <AdminFinanceSellerDetailView
      sellerId={sellerId}
      title="Chi tiết tài chính seller"
      subtitle="Nhập seller ID từ tab Top sellers hoặc URL ?sellerId=..."
      emptyMessage="Chưa chọn seller."
      status={status}
      errorMessage={errorMessage}
      summary={summary}
      ledgerItems={ledger?.items ?? []}
      onRetry={load}
    />
  );
}
