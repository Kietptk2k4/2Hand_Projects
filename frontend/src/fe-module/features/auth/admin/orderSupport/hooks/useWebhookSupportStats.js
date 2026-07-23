import { useCallback, useEffect, useState } from "react";
import { getWebhookLogStatsForSupport } from "../api/orderSupportApi.js";
import { datetimeLocalToIso } from "../utils/webhookSupportDateUtils.js";

function toApiFilters(webhookFilters = {}) {
  return {
    provider: webhookFilters.provider || undefined,
    reference_id: webhookFilters.reference_id || undefined,
    q: webhookFilters.q || undefined,
    event_type: webhookFilters.event_type || undefined,
    status: webhookFilters.status || undefined,
    from: datetimeLocalToIso(webhookFilters.from) || webhookFilters.from || undefined,
    to: datetimeLocalToIso(webhookFilters.to) || webhookFilters.to || undefined,
  };
}

export function useWebhookSupportStats({ enabled = true, webhookFilters = {} } = {}) {
  const [stats, setStats] = useState(null);
  const [status, setStatus] = useState("idle");

  const filterSignature = JSON.stringify(webhookFilters || {});

  const refetch = useCallback(async () => {
    if (!enabled) return;

    setStatus("loading");
    try {
      const data = await getWebhookLogStatsForSupport(toApiFilters(webhookFilters));
      setStats(data);
      setStatus("ready");
    } catch {
      setStats(null);
      setStatus("error");
    }
  }, [enabled, filterSignature, webhookFilters]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { stats, status, refetch };
}
