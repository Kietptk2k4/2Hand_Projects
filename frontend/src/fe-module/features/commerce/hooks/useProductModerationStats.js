import { useCallback, useEffect, useState } from "react";
import { fetchAdminProductList } from "../api/adminProductRemovalApi.js";
import { PRODUCT_MODERATION_STAT_PRESETS } from "../constants/productModerationListConstants.js";

function extractTotal(data) {
  return data?.pagination?.total_items ?? data?.pagination?.totalItems ?? 0;
}

export function useProductModerationStats({ enabled = true } = {}) {
  const [stats, setStats] = useState(null);
  const [status, setStatus] = useState("idle");

  const fetchStats = useCallback(async () => {
    if (!enabled) return;

    setStatus("loading");

    try {
      const requests = PRODUCT_MODERATION_STAT_PRESETS.map((preset) =>
        fetchAdminProductList({
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
        fetchAdminProductList({ page: 1, limit: 1 }),
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
