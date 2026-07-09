import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchAdminPlatformTopSellers } from "../../api/adminFinancePlatformApi";
import { mapPlatformTopSellers } from "../../utils/adminFinanceMapper";
import { buildAdminSearchParams } from "../../../adminUrlParams.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { AdminFinanceTopSellersView } from "../AdminFinanceTopSellersView.jsx";

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

  return (
    <AdminFinanceTopSellersView
      title="Top sellers"
      subtitle="Xếp hạng theo doanh thu đã ghi nhận (30 ngày gần nhất)."
      status={status}
      errorMessage={errorMessage}
      sellers={sellers}
      onRetry={load}
      onSellerSelect={openSellerDetail}
    />
  );
}
