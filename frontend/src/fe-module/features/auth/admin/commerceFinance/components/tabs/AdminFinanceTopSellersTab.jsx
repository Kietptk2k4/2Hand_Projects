import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { formatVndPrice } from "../../../../../social/utils/formatPrice";
import { fetchAdminPlatformTopSellers } from "../../api/adminFinancePlatformApi";
import { mapPlatformTopSellers } from "../../utils/adminFinanceMapper";
import { buildAdminSearchParams } from "../../../adminUrlParams.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { AccountCard, AccountSkeleton, TabPanelHeader } from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";

export function AdminFinanceTopSellersTab() {
  const [searchParams, setSearchParams] = useSearchParams();
  const { showSessionExpired } = useAuthSession();
  const [sellers, setSellers] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");
    try {
      const raw = await fetchAdminPlatformTopSellers({ limit: 15 });
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
  }, [showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  const openSellerDetail = (sellerId) => {
    setSearchParams(
      buildAdminSearchParams({
        section: "commerceFinance",
        tab: "seller-detail",
        sellerId,
        preserve: searchParams,
      }),
      { replace: true },
    );
  };

  if (status === "error") {
    return <ErrorState message={errorMessage} onRetry={load} />;
  }

  return (
    <AccountCard>
      <TabPanelHeader title="Top sellers" subtitle="Xếp hạng theo doanh thu đã ghi nhận (30 ngày gần nhất)." />
      {status === "loading" ? (
        <AccountSkeleton rows={5} />
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-body-sm">
            <thead className="border-b border-outline-variant text-on-surface-variant">
              <tr>
                <th className="px-3 py-2">Shop</th>
                <th className="px-3 py-2">Seller ID</th>
                <th className="px-3 py-2">Gross</th>
                <th className="px-3 py-2">Phí sàn</th>
                <th className="px-3 py-2">Đơn</th>
              </tr>
            </thead>
            <tbody>
              {sellers.length ? (
                sellers.map((seller) => (
                  <tr key={seller.sellerId} className="border-b border-outline-variant/50">
                    <td className="px-3 py-2 font-medium">{seller.shopName}</td>
                    <td className="px-3 py-2">
                      <button type="button" onClick={() => openSellerDetail(seller.sellerId)} className="font-mono text-xs text-primary hover:underline">
                        {seller.sellerId}
                      </button>
                    </td>
                    <td className="px-3 py-2">{formatVndPrice(seller.recognizedGross)}</td>
                    <td className="px-3 py-2">{formatVndPrice(seller.platformFee)}</td>
                    <td className="px-3 py-2">{seller.completedItemCount}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} className="px-3 py-8 text-center text-on-surface-variant">
                    Chưa có dữ liệu seller.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </AccountCard>
  );
}
