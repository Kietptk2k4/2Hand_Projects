import { useCallback, useEffect, useState } from "react";
import { fetchRecommendationModelArtifacts } from "../api/recommendationModelArtifactsApi.js";
import { mapRecommendationModelArtifacts } from "../utils/modelRegistryDisplayUtils.js";
import { DEFAULT_MODEL_NAME } from "../constants/modelRegistryConstants.js";

export function useRecommendationModelArtifacts({ enabled = true, modelName = DEFAULT_MODEL_NAME } = {}) {
  const [status, setStatus] = useState(enabled ? "loading" : "idle");
  const [items, setItems] = useState([]);
  const [errorMessage, setErrorMessage] = useState("");

  const refetch = useCallback(async () => {
    if (!enabled) {
      setStatus("idle");
      setItems([]);
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchRecommendationModelArtifacts(modelName);
      setItems(mapRecommendationModelArtifacts(data));
      setStatus("success");
    } catch (error) {
      setItems([]);
      setErrorMessage(error?.message || "Không tải được danh sách model.");
      setStatus(error?.code === 403 || error?.code === "FORBIDDEN" ? "forbidden" : "error");
    }
  }, [enabled, modelName]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { status, items, errorMessage, refetch };
}
