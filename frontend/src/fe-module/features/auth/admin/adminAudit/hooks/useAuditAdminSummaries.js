import { useEffect, useMemo, useState } from "react";
import { lookupAuditAdminById } from "../api/auditAdminApi.js";

export function useAuditAdminSummaries(adminIds) {
  const [summaries, setSummaries] = useState({});

  const uniqueIds = useMemo(
    () => [...new Set((adminIds || []).map((id) => String(id || "").trim()).filter(Boolean))],
    [adminIds],
  );

  const idsKey = uniqueIds.join("|");

  useEffect(() => {
    if (!uniqueIds.length) {
      setSummaries({});
      return;
    }

    let cancelled = false;

    (async () => {
      const pairs = await Promise.all(
        uniqueIds.map(async (adminId) => {
          try {
            const admin = await lookupAuditAdminById(adminId);
            return [adminId, admin];
          } catch {
            return [adminId, null];
          }
        }),
      );

      if (cancelled) return;

      setSummaries(
        pairs.reduce((acc, [adminId, admin]) => {
          if (admin) acc[adminId] = admin;
          return acc;
        }, {}),
      );
    })();

    return () => {
      cancelled = true;
    };
  }, [idsKey, uniqueIds]);

  return summaries;
}
