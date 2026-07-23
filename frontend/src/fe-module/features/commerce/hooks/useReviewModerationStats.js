import { useCallback, useEffect, useState } from "react";
import { fetchAdminReviewList } from "../api/adminReviewModerationApi.js";
import { REVIEW_MODERATION_STAT_PRESETS } from "../constants/reviewModerationListConstants.js";

function extractTotal(data) {
  return data?.pagination?.total_items ?? data?.pagination?.totalItems ?? 0;
}

export function useReviewModerationStats({ enabled = true } = {}) {
  const [stats, setStats] = useState(null);
  const [status, setStatus] = useState("idle");

  const fetchStats = useCallback(async () => {
    if (!enabled) return;

    setStatus("loading");

    try {
      const requests = REVIEW_MODERATION_STAT_PRESETS.map((preset) =>
        fetchAdminReviewList({
          page: 1,
          limit: 1,
          status: preset.status,
        }).then((data) => ({
          id: preset.id,
          label: preset.label,
          count: extractTotal(data),
        })),
      );

      const [totalData, ...presetResults] = await Promise.all([
        fetchAdminReviewList({ page: 1, limit: 1 }),
        ...requests,
      ]);

      setStats({
        total: extractTotal(totalData),
        presets: presetResults,
      });
      setStatus("ready");
    } catch {
      setStats(null);
      setStatus("error");
    }
  }, [enabled]);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  return {
    stats,
    status,
    refetch: fetchStats,
  };
}
