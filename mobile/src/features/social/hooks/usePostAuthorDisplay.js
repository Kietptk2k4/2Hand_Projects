import { useEffect, useState } from "react";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { fetchSocialProfile } from "../api/profileApi";
import { authorAvatarUrl, authorDisplayName } from "../utils/authorDisplay";
import { getCachedAuthorProfile, setCachedAuthorProfile } from "../utils/authorProfileCache";

function fallbackAuthor(authorId) {
  return {
    displayName: authorDisplayName(authorId),
    avatarUrl: authorAvatarUrl(authorId),
  };
}

function mapProfileToAuthor(authorId, profile) {
  return {
    displayName:
      profile?.displayName ||
      profile?.display_name ||
      authorDisplayName(authorId),
    avatarUrl: resolveDevMediaUrl(
      profile?.avatarUrl ||
        profile?.avatar_url ||
        authorAvatarUrl(authorId)
    ),
  };
}

export function usePostAuthorDisplay(authorId) {
  const [author, setAuthor] = useState(() => {
    const cached = getCachedAuthorProfile(authorId);
    return cached || fallbackAuthor(authorId);
  });

  useEffect(() => {
    if (!authorId) {
      setAuthor(fallbackAuthor(authorId));
      return undefined;
    }

    const cached = getCachedAuthorProfile(authorId);
    if (cached) {
      setAuthor(cached);
      return undefined;
    }

    setAuthor(fallbackAuthor(authorId));

    let cancelled = false;

    (async () => {
      try {
        const profile = await fetchSocialProfile(authorId);
        if (cancelled) return;
        const resolved = mapProfileToAuthor(authorId, profile);
        setCachedAuthorProfile(authorId, resolved);
        setAuthor(resolved);
      } catch {
        // keep placeholder until profile is available
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [authorId]);

  return author;
}
