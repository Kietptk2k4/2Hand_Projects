import { useCallback, useEffect, useState } from "react";
import { formatVndPrice } from "../../../../../social/utils/formatPrice";
import {
  fetchAdminSellerFinanceLedger,
  fetchAdminSellerFinanceSummary,
} from "../../api/adminFinancePlatformApi";
import { mapSellerFinanceLedger, mapSellerFinanceSummary } from "../../utils/adminFinanceMapper";
import { SellerRevenueBucketCards } from "../../../../../commerce/components/SellerRevenueBucketCards";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { AccountCard, AccountSkeleton, TabPanelHeader } from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";

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

  if (!sellerId) {
    return (
      <AccountCard>
        <TabPanelHeader title="Chi tiết seller" subtitle="Nhập seller ID từ tab Top sellers hoặc URL ?sellerId=..." />
        <p className="text-body-md text-on-surface-variant">Chưa chọn seller.</p>
      </AccountCard>
    );
  }

  if (status === "error") {
    return <ErrorState message={errorMessage} onRetry={load} />;
  }

  return (
    <AccountCard>
      <TabPanelHeader title="Chi tiết tài chính seller" subtitle={`Seller ID: ${sellerId}`} />
      {status === "loading" ? (
        <AccountSkeleton rows={4} />
      ) : (
        <>
          <div className="mb-6 grid gap-4 md:grid-cols-2">
            <div className="rounded-xl border border-outline-variant bg-surface-container-low p-4">
              <p className="text-label-md text-on-surface-variant">Số dư khả dụng</p>
              <p className="mt-2 text-headline-sm font-bold text-primary">
                {formatVndPrice(summary?.balance?.availableBalance ?? 0)}
              </p>
            </div>
            <div className="rounded-xl border border-outline-variant bg-surface-container-low p-4">
              <p className="text-label-md text-on-surface-variant">Payout đang chờ</p>
              <p className="mt-2 text-headline-sm font-bold">
                {formatVndPrice(summary?.balance?.pendingPayoutAmount ?? 0)}
              </p>
            </div>
          </div>
          <SellerRevenueBucketCards summary={summary} isLoading={false} />
          <h3 className="mb-3 mt-8 text-title-md font-semibold">Sổ cái (10 gần nhất)</h3>
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-body-sm">
              <thead className="border-b border-outline-variant text-on-surface-variant">
                <tr>
                  <th className="px-3 py-2">Thời gian</th>
                  <th className="px-3 py-2">Loại</th>
                  <th className="px-3 py-2">Net</th>
                </tr>
              </thead>
              <tbody>
                {(ledger?.items ?? []).map((entry) => (
                  <tr key={entry.id} className="border-b border-outline-variant/50">
                    <td className="px-3 py-2">
                      {entry.createdAt ? new Date(entry.createdAt).toLocaleString("vi-VN") : "—"}
                    </td>
                    <td className="px-3 py-2">{entry.entryType}</td>
                    <td className="px-3 py-2">{formatVndPrice(entry.netAmount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </AccountCard>
  );
}
