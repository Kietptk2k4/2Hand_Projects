import { useEffect, useState } from "react";
import { fetchSocialProfile } from "../api/profileApi";
import { authorAvatarUrl, authorDisplayName } from "../utils/authorDisplay";
import { getCachedAuthorProfile, setCachedAuthorProfile } from "../utils/authorProfileCache";

function resolveHandle(profileOrData, authorId) {
  const email =
    profileOrData?.email ||
    profileOrData?.username ||
    profileOrData?.handle ||
    profileOrData?.author?.email ||
    profileOrData?.author?.username ||
    profileOrData?.author?.handle;

  if (email && typeof email === "string") {
    return email.includes("@") ? `@${email.split("@")[0]}` : `@${email}`;
  }

  const name =
    profileOrData?.displayName ||
    profileOrData?.display_name ||
    profileOrData?.authorDisplayName ||
    profileOrData?.author?.displayName ||
    profileOrData?.author?.display_name;

  if (name && name !== "User" && typeof name === "string") {
    return `@${name.toLowerCase().replace(/[^a-z0-9]/gi, "")}`;
  }

  if (!authorId || typeof authorId !== "string") return "@user";
  return `@user_${authorId.slice(0, 6)}`;
}

function fallbackAuthor(authorId, initialData = {}) {
  const safeData = initialData && typeof initialData === "object" ? initialData : {};
  const name =
    safeData.authorDisplayName ||
    safeData.displayName ||
    safeData.display_name ||
    safeData.author?.displayName ||
    safeData.author?.display_name ||
    authorDisplayName(authorId);

  const avatar =
    safeData.authorAvatarUrl ||
    safeData.avatarUrl ||
    safeData.avatar_url ||
    safeData.author?.avatarUrl ||
    safeData.author?.avatar_url ||
    authorAvatarUrl(authorId);

  return {
    displayName: name,
    avatarUrl: avatar,
    handle: resolveHandle(safeData, authorId),
  };
}

function mapProfileToAuthor(authorId, profile) {
  const name =
    profile?.displayName ||
    profile?.display_name ||
    authorDisplayName(authorId);

  const avatar =
    profile?.avatarUrl ||
    profile?.avatar_url ||
    authorAvatarUrl(authorId);

  return {
    displayName: name,
    avatarUrl: avatar,
    handle: resolveHandle(profile, authorId),
  };
}

export function usePostAuthorDisplay(authorId, initialData) {
  const [author, setAuthor] = useState(() => {
    const cached = getCachedAuthorProfile(authorId);
    if (cached) return cached;
    return fallbackAuthor(authorId, initialData);
  });

  useEffect(() => {
    if (!authorId) {
      setAuthor(fallbackAuthor(authorId, initialData));
      return undefined;
    }

    const cached = getCachedAuthorProfile(authorId);
    if (cached) {
      setAuthor(cached);
      return undefined;
    }

    setAuthor(fallbackAuthor(authorId, initialData));

    let cancelled = false;

    (async () => {
      try {
        const profile = await fetchSocialProfile(authorId);
        if (cancelled) return;
        const resolved = mapProfileToAuthor(authorId, profile);
        setCachedAuthorProfile(authorId, resolved);
        setAuthor(resolved);
      } catch {
        // keep fallback until profile is available
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [authorId, initialData?.authorDisplayName, initialData?.authorAvatarUrl]);

  return author;
}