import { useCallback, useEffect, useState } from "react";
import { fetchSystemAnnouncements } from "../api/systemAnnouncementApi.js";
import { ANNOUNCEMENT_STAT_PRESETS } from "../constants/announcementListConstants.js";

function extractTotal(data) {
  return data?.total_elements ?? data?.totalElements ?? 0;
}

export function useSystemAnnouncementStats({ enabled = true } = {}) {
  const [stats, setStats] = useState(null);
  const [status, setStatus] = useState("idle");

  const fetchStats = useCallback(async () => {
    if (!enabled) return;

    setStatus("loading");

    try {
      const requests = ANNOUNCEMENT_STAT_PRESETS.map((preset) =>
        fetchSystemAnnouncements({
          page: 1,
          size: 1,
          status: preset.status,
        }).then((data) => ({
          id: preset.id,
          label: preset.label,
          count: extractTotal(data),
        })),
      );

      const [totalData, ...presetResults] = await Promise.all([
        fetchSystemAnnouncements({ page: 1, size: 1 }),
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
