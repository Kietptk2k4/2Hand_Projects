import { useEffect, useState } from "react";
import { getInvestigationProfile } from "../../userInvestigation/api/userInvestigationApi.js";

function buildAuthorIdsKey(authorIds) {
  return [...new Set((authorIds || []).map((id) => String(id || "").trim()).filter(Boolean))]
    .sort()
    .join("|");
}

export function usePostAuthorSummaries(authorIds) {
  const [summaries, setSummaries] = useState({});
  const idsKey = buildAuthorIdsKey(authorIds);

  useEffect(() => {
    const uniqueIds = idsKey ? idsKey.split("|") : [];

    if (!uniqueIds.length) {
      setSummaries({});
      return;
    }

    let cancelled = false;

    (async () => {
      const pairs = await Promise.all(
        uniqueIds.map(async (authorId) => {
          try {
            const profile = await getInvestigationProfile(authorId);
            return [
              authorId,
              {
                displayName: profile?.display_name || profile?.displayName || "",
                avatarUrl: profile?.avatar_url || profile?.avatarUrl || "",
              },
            ];
          } catch {
            return [authorId, null];
          }
        }),
      );

      if (cancelled) return;

      setSummaries(
        pairs.reduce((acc, [authorId, profile]) => {
          if (profile) acc[authorId] = profile;
          return acc;
        }, {}),
      );
    })();

    return () => {
      cancelled = true;
    };
  }, [idsKey]);

  return summaries;
}
