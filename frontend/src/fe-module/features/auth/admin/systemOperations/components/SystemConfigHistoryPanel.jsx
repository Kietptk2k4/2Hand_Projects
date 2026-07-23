import { useEffect, useState } from "react";
import { CONFIG_PAGE_SIZE } from "../constants/systemConfigConstants.js";
import { useSystemConfigHistory } from "../hooks/useSystemConfigHistory.js";
import { SystemConfigHistoryPanelView } from "./SystemConfigHistoryPanelView.jsx";

export function SystemConfigHistoryPanel({ configId, enabled, onRefresh }) {
  const [page, setPage] = useState(1);
  const pageSize = CONFIG_PAGE_SIZE;

  useEffect(() => {
    setPage(1);
  }, [configId]);

  const { result, status, errorMessage, refetch } = useSystemConfigHistory({
    configId,
    page,
    size: pageSize,
    enabled,
  });

  if (!configId) return null;

  const handleRetry = () => {
    refetch();
    onRefresh?.();
  };

  return (
    <SystemConfigHistoryPanelView
      status={status}
      errorMessage={errorMessage}
      valuesMasked={result?.valuesMasked}
      history={result?.history}
      currentPage={result?.page || page}
      totalPages={result?.totalPages || 1}
      totalElements={result?.totalElements || 0}
      onPageChange={setPage}
      onRetry={handleRetry}
    />
  );
}
