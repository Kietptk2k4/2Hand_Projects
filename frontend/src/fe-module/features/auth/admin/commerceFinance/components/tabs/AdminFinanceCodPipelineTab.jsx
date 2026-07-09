import { useCallback, useEffect, useState } from "react";
import { fetchAdminPlatformCodPipeline } from "../../api/adminFinancePlatformApi";
import { mapPlatformCodPipeline } from "../../utils/adminFinanceMapper";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { AdminFinanceCodPipelineView } from "../AdminFinanceCodPipelineView.jsx";

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

  return (
    <AdminFinanceCodPipelineView
      title="COD pipeline toàn sàn"
      subtitle="Tổng giá trị đơn hàng theo trạng thái vận chuyển và ghi nhận."
      status={status}
      errorMessage={errorMessage}
      pipeline={pipeline}
      onRetry={load}
    />
  );
}
