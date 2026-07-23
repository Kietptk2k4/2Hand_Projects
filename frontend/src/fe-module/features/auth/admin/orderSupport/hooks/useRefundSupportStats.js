import { useCallback, useEffect, useState } from "react";
import { fetchAdminRefundApprovals } from "../api/adminRefundApprovalApi.js";
import { REFUND_STAT_PRESETS } from "../constants/refundSupportListConstants.js";

function extractTotal(data) {
  return data?.pagination?.total_items ?? data?.pagination?.totalItems ?? 0;
}

export function useRefundSupportStats({ enabled = true } = {}) {
  const [stats, setStats] = useState(null);
  const [status, setStatus] = useState("idle");

  const refetch = useCallback(async () => {
    if (!enabled) return;

    setStatus("loading");

    try {
      const presetRequests = REFUND_STAT_PRESETS.map((preset) =>
        fetchAdminRefundApprovals({ status: preset.status, page: 1, limit: 1 }).then((data) => ({
          id: preset.id,
          label: preset.label,
          count: extractTotal(data),
        })),
      );

      const [totalData, ...presetResults] = await Promise.all([
        fetchAdminRefundApprovals({ page: 1, limit: 1 }),
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
