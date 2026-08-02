import { useCallback, useEffect, useRef, useState } from "react";
import { fetchHomeRecommendations } from "../api/homeRecommendationsApi";
import { mapHomeRecommendationsResponse } from "../utils/homeRecommendationsMapper";

export function useHomeRecommendations({ enabled = true } = {}) {
  const [items, setItems] = useState([]);
  const [requestId, setRequestId] = useState(null);
  const [rankingMode, setRankingMode] = useState(null);
  const [status, setStatus] = useState(enabled ? "idle" : "idle");
  const [errorMessage, setErrorMessage] = useState("");
  const requestSeqRef = useRef(0);

  const refetch = useCallback(async () => {
    if (!enabled) {
      setStatus("idle");
      setItems([]);
      setRequestId(null);
      setRankingMode(null);
      setErrorMessage("");
      return;
    }

    const seq = ++requestSeqRef.current;
    setStatus("loading");
    setErrorMessage("");

    try {
      const raw = await fetchHomeRecommendations();
      if (seq !== requestSeqRef.current) return;
      const data = mapHomeRecommendationsResponse(raw);
      setItems(data.items);
      setRequestId(data.requestId);
      setRankingMode(data.rankingMode);
      setStatus("ready");
    } catch (error) {
      if (seq !== requestSeqRef.current) return;
      setItems([]);
      setRequestId(null);
      setRankingMode(null);
      setStatus("error");
      if (error?.code === 404 || error?.code === "NOT_FOUND") {
        setErrorMessage("Đề xuất Home chưa được bật trên hệ thống.");
      } else if (error?.code === 401 || error?.code === "UNAUTHORIZED") {
        setErrorMessage("Đăng nhập để xem đề xuất dành cho bạn.");
      } else {
        setErrorMessage(error?.message || "Không tải được đề xuất Home. Vui lòng thử lại.");
      }
    }
  }, [enabled]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return {
    items,
    requestId,
    rankingMode,
    status,
    errorMessage,
    isInitialLoading: status === "loading",
    hasNext: false,
    loadMore: () => {},
    retry: refetch,
  };
}
