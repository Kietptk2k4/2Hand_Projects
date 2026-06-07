import { useCallback, useEffect, useState } from "react";
import { fetchPublicUserProfile } from "../../auth/api/authApi";

function normalizePublicProfile(data) {
  if (!data) return null;

  const rawSocialLinks = data.social_links ?? data.socialLinks ?? {};
  const socialLinks =
    rawSocialLinks && typeof rawSocialLinks === "object" && !Array.isArray(rawSocialLinks)
      ? rawSocialLinks
      : {};

  return {
    userId: data.user_id ?? data.userId ?? "",
    displayName: data.display_name ?? data.displayName ?? "",
    avatarUrl: data.avatar_url ?? data.avatarUrl ?? "",
    bio: data.bio ?? "",
    website: data.website ?? "",
    socialLinks,
    isPrivate: Boolean(data.is_private ?? data.isPrivate),
  };
}

export function usePublicUserProfile(userId, { enabled = true } = {}) {
  const [publicProfile, setPublicProfile] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [errorCode, setErrorCode] = useState(null);

  const load = useCallback(async () => {
    if (!userId || !enabled) {
      setPublicProfile(null);
      setStatus("idle");
      setErrorMessage("");
      setErrorCode(null);
      return;
    }

    setStatus("loading");
    setErrorMessage("");
    setErrorCode(null);
    setPublicProfile(null);

    try {
      const data = await fetchPublicUserProfile(userId);
      setPublicProfile(normalizePublicProfile(data));
      setStatus("ready");
    } catch (error) {
      setStatus("error");
      setErrorCode(error?.code || 500);
      setErrorMessage(error?.message || "Không tải được thông tin hồ sơ công khai.");
    }
  }, [userId, enabled]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    publicProfile,
    status,
    errorMessage,
    errorCode,
    isLoading: status === "loading",
    isError: status === "error",
    retry: load,
  };
}
