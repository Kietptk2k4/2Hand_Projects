import { useCallback, useEffect, useState } from "react";
import { fetchRecommendationModelStatus } from "../api/recommendationModelStatusApi.js";

function mapStatus(data) {
  if (!data) return null;

  return {
    mode: data.mode,
    modelVersion: data.modelVersion ?? data.model_version ?? null,
    modelName: data.modelName ?? data.model_name ?? null,
    reason: data.reason ?? null,
    configuredRankingModel: data.configuredRankingModel ?? data.configured_ranking_model ?? null,
  };
}

export function useRecommendationModelStatus({ enabled = true } = {}) {
  const [status, setStatus] = useState(enabled ? "loading" : "idle");
  const [runtimeStatus, setRuntimeStatus] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");

  const refetch = useCallback(async () => {
    if (!enabled) {
      setStatus("idle");
      setRuntimeStatus(null);
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchRecommendationModelStatus();
      setRuntimeStatus(mapStatus(data));
      setStatus("success");
    } catch (error) {
      setRuntimeStatus(null);
      setErrorMessage(error?.message || "Không tải được trạng thái runtime.");
      setStatus(error?.code === 403 || error?.code === "FORBIDDEN" ? "forbidden" : "error");
    }
  }, [enabled]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { status, runtimeStatus, errorMessage, refetch };
}
