import { useCallback, useEffect, useState } from "react";
import { fetchSystemConfigs } from "../api/systemConfigApi.js";
import { SYSTEM_CONFIG_STAT_PRESETS } from "../constants/systemConfigListConstants.js";

function extractTotal(data) {
  return data?.total_elements ?? data?.totalElements ?? 0;
}

export function useSystemConfigStats({ enabled = true } = {}) {
  const [stats, setStats] = useState(null);
  const [status, setStatus] = useState("idle");

  const fetchStats = useCallback(async () => {
    if (!enabled) return;

    setStatus("loading");

    try {
      const requests = SYSTEM_CONFIG_STAT_PRESETS.map((preset) =>
        fetchSystemConfigs({
          page: 1,
          size: 1,
          is_active: preset.is_active === "true",
        }).then((data) => ({
          id: preset.id,
          label: preset.label,
          count: extractTotal(data),
        })),
      );

      const [totalData, ...presetResults] = await Promise.all([
        fetchSystemConfigs({ page: 1, size: 1 }),
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
