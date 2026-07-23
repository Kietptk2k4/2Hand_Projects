import { useCallback, useEffect, useState } from "react";
import { fetchAdminShopList } from "../api/adminShopModerationApi.js";
import { SHOP_MODERATION_STAT_PRESETS } from "../constants/shopModerationListConstants.js";

function extractTotal(data) {
  return data?.pagination?.total_items ?? data?.pagination?.totalItems ?? 0;
}

export function useShopModerationStats({ enabled = true } = {}) {
  const [stats, setStats] = useState(null);
  const [status, setStatus] = useState("idle");

  const fetchStats = useCallback(async () => {
    if (!enabled) return;

    setStatus("loading");

    try {
      const requests = SHOP_MODERATION_STAT_PRESETS.map((preset) =>
        fetchAdminShopList({
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
        fetchAdminShopList({ page: 1, limit: 1 }),
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
