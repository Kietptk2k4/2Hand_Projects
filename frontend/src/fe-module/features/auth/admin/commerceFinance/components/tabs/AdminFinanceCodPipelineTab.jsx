import { useCallback, useEffect, useState } from "react";
import { fetchAdminPlatformCodPipeline } from "../../api/adminFinancePlatformApi";
import { mapPlatformCodPipeline } from "../../utils/adminFinanceMapper";
import { SellerRevenueBucketCards } from "../../../../../commerce/components/SellerRevenueBucketCards";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { AccountCard, AccountSkeleton, TabPanelHeader } from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";

export function AdminFinanceCodPipelineTab() {
  const { showSessionExpired } = useAuthSession();
  const [pipeline, setPipeline] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");
    try {
      const raw = await fetchAdminPlatformCodPipeline();
      setPipeline(mapPlatformCodPipeline(raw));
      setStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setErrorMessage(error?.message || "Không tải được COD pipeline.");
      setStatus("error");
    }
  }, [showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  if (status === "error") {
    return <ErrorState message={errorMessage} onRetry={load} />;
  }

  return (
    <AccountCard>
      <TabPanelHeader
        title="COD pipeline toàn sàn"
        subtitle="Tổng giá trị đơn hàng theo trạng thái vận chuyển và ghi nhận."
      />
      {status === "loading" ? (
        <AccountSkeleton rows={3} />
      ) : (
        <SellerRevenueBucketCards summary={pipeline} isLoading={false} />
      )}
    </AccountCard>
  );
}
