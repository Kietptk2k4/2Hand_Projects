import { useQuery } from "@tanstack/react-query";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { fetchSocialProfile } from "../api/profileApi";
import { profileKeys } from "../api/profileKeys";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";

function withResolvedAvatar(profile) {
  if (!profile) return profile;

  return {
    ...profile,
    avatarUrl: resolveDevMediaUrl(profile.avatarUrl || profile.avatar_url || ""),
  };
}

export function useProfile(userId) {
  const query = useQuery({
    queryKey: profileKeys.detail(userId),
    select: withResolvedAvatar,
    queryFn: async () => {
      try {
        return await fetchSocialProfile(userId);
      } catch (error) {
        const handled = await handleSocialQueryError(error);
        if (handled) throw error;
        throw error;
      }
    },
    enabled: Boolean(userId),
  });

  return {
    profile: query.data ?? null,
    isLoading: query.isLoading,
    isError: query.isError,
    errorMessage: query.error?.message || "Không tải được hồ sơ.",
    errorCode: query.error?.code ?? null,
    retry: query.refetch,
  };
}
