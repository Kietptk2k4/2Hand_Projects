import { useQuery } from "@tanstack/react-query";
import { fetchSocialProfile } from "../api/profileApi";
import { profileKeys } from "../api/profileKeys";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";

export function useProfile(userId) {
  const query = useQuery({
    queryKey: profileKeys.detail(userId),
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
