import { useQuery } from "@tanstack/react-query";
import { fetchPublicUserProfile } from "../../auth/api/authApi";
import { profileKeys } from "../api/profileKeys";
import {
  normalizePublicProfile,
  resolvePublicProfileDetails,
} from "../utils/resolveProfileDetails";

export function usePublicProfileDetails(userId, { enabled = true } = {}) {
  const query = useQuery({
    queryKey: profileKeys.publicDetails(userId),
    queryFn: async () => {
      const data = await fetchPublicUserProfile(userId);
      return normalizePublicProfile(data);
    },
    enabled: Boolean(userId) && enabled,
  });

  const details = resolvePublicProfileDetails(query.data);

  return {
    publicProfile: query.data ?? null,
    details,
    isLoading: query.isLoading,
    isError: query.isError,
    errorMessage: query.error?.message || "Không tải được thông tin hồ sơ công khai.",
    errorCode: query.error?.code ?? null,
    retry: query.refetch,
  };
}
