import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { buildAdminSearchParams } from "../../../adminUrlParams.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { fetchAdminPlatformCodPipeline } from "../../api/adminFinancePlatformApi";
import { mapPlatformCodPipeline } from "../../utils/adminFinanceMapper";
import { buildCodStageDeepLinkParams } from "../../utils/codPipelineHelpers.js";
import { AdminFinanceCodPipelineView } from "../AdminFinanceCodPipelineView.jsx";

export function AdminFinanceCodPipelineTab() {
  const { showSessionExpired } = useAuthSession();
  const [searchParams, setSearchParams] = useSearchParams();
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

  const navigateDeepLink = useCallback(
    (params) => {
      setSearchParams(
        buildAdminSearchParams({
          ...params,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [searchParams, setSearchParams],
  );

  const handleStageClick = useCallback(
    (stageKey) => {
      navigateDeepLink(buildCodStageDeepLinkParams(stageKey));
    },
    [navigateDeepLink],
  );

  const handleOpenOrderSupport = useCallback(() => {
    navigateDeepLink({ section: "orderSupport", tab: "order-detail" });
  }, [navigateDeepLink]);

  return (
    <AdminFinanceCodPipelineView
      status={status}
      errorMessage={errorMessage}
      pipeline={pipeline}
      onRetry={load}
      onStageClick={handleStageClick}
      onOpenOrderSupport={handleOpenOrderSupport}
    />
  );
}
