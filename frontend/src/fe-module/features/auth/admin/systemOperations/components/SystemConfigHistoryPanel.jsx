import { useSystemConfigHistory } from "../hooks/useSystemConfigHistory.js";
import { SystemConfigHistoryPanelView } from "./SystemConfigHistoryPanelView.jsx";

export function SystemConfigHistoryPanel({ configId, enabled }) {
  const { result, status, errorMessage, refetch } = useSystemConfigHistory({
    configId,
    enabled,
  });

  if (!configId) return null;

  return (
    <SystemConfigHistoryPanelView
      status={status}
      errorMessage={errorMessage}
      valuesMasked={result?.valuesMasked}
      history={result?.history}
      onRetry={refetch}
    />
  );
}
