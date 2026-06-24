import { socialApiClient } from "../../../services/http/socialApiClient";
import { resolveDevMediaUrl } from "../../../shared/utils/getClientUploadOrigin";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

function normalizeSocialProfile(data) {
  if (!data) return data;

  return {
    ...data,
    userId: data.userId ?? data.user_id ?? "",
    displayName: data.displayName ?? data.display_name ?? "",
    avatarUrl: resolveDevMediaUrl(data.avatarUrl ?? data.avatar_url ?? ""),
    coverUrl: resolveDevMediaUrl(data.coverUrl ?? data.cover_url ?? ""),
    isPrivate: Boolean(data.isPrivate ?? data.is_private),
    followStatus: data.followStatus ?? data.follow_status ?? "NONE",
    canViewFullProfile: Boolean(data.canViewFullProfile ?? data.can_view_full_profile),
  };
}

export async function fetchSocialProfile(userId) {
  try {
    const response = await socialApiClient.get(`/api/v1/social/users/${userId}/profile`);
    return normalizeSocialProfile(unwrapResponse(response));
  } catch (error) {
    throw mapAxiosError(error);
  }
}
