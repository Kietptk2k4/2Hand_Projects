import { useCallback, useEffect, useState } from "react";
import { listOrdersForSupport } from "../api/orderSupportApi.js";
import { ORDER_STAT_PRESETS } from "../constants/orderSupportListConstants.js";

function extractTotal(data) {
  return data?.total_elements ?? data?.totalElements ?? 0;
}

export function useOrderSupportOrderStats({ enabled = true } = {}) {
  const [stats, setStats] = useState(null);
  const [status, setStatus] = useState("idle");

  const refetch = useCallback(async () => {
    if (!enabled) return;

    setStatus("loading");

    try {
      const presetRequests = ORDER_STAT_PRESETS.map((preset) =>
        listOrdersForSupport({
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
        listOrdersForSupport({ page: 1, size: 1 }),
        ...presetRequests,
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
    refetch();
  }, [refetch]);

  return { stats, status, refetch };
}
