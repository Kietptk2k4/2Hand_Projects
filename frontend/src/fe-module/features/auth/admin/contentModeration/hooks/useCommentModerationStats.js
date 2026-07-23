import { useCallback, useEffect, useState } from "react";
import { getCommentsForModeration } from "../api/socialModerationListApi.js";
import { COMMENT_MODERATION_STAT_PRESETS } from "../constants/commentModerationListConstants.js";

function extractTotal(data) {
  return data?.pagination?.total_items ?? 0;
}

export function useCommentModerationStats({ enabled = true } = {}) {
  const [stats, setStats] = useState(null);
  const [status, setStatus] = useState("idle");

  const fetchStats = useCallback(async () => {
    if (!enabled) return;

    setStatus("loading");

    try {
      const requests = COMMENT_MODERATION_STAT_PRESETS.map((preset) =>
        getCommentsForModeration({
          status: preset.status || undefined,
          moderation_status: preset.moderation_status || undefined,
          page: 1,
          size: 1,
        }).then((data) => ({
          id: preset.id,
          label: preset.label,
          count: extractTotal(data),
        })),
      );

      const [totalData, ...presetResults] = await Promise.all([
        getCommentsForModeration({ page: 1, size: 1 }),
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
