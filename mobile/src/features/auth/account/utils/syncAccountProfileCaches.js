import { profileKeys } from "../../../social/api/profileKeys";
import { clearCachedAuthorProfile } from "../../../social/utils/authorProfileCache";
import { normalizePublicProfile } from "../../../social/utils/resolveProfileDetails";

function pickDisplayName(profilePatch = {}) {
  return profilePatch.display_name ?? profilePatch.displayName ?? null;
}

function pickAvatarUrl(profilePatch = {}) {
  return profilePatch.avatar_url ?? profilePatch.avatarUrl ?? null;
}

function pickIsPrivate(profilePatch = {}) {
  if (profilePatch.is_private !== undefined) return Boolean(profilePatch.is_private);
  if (profilePatch.isPrivate !== undefined) return Boolean(profilePatch.isPrivate);
  return null;
}

export function applyAccountProfilePatch(queryClient, userId, patch = {}) {
  if (!userId) return;

  const displayName = pickDisplayName(patch);
  const avatarUrl = pickAvatarUrl(patch);
  const isPrivate = pickIsPrivate(patch);

  queryClient.setQueryData(profileKeys.detail(userId), (current) => {
    if (!current) return current;

    return {
      ...current,
      ...(displayName != null ? { displayName } : null),
      ...(avatarUrl != null ? { avatarUrl } : null),
      ...(isPrivate != null ? { isPrivate } : null),
    };
  });

  queryClient.setQueryData(profileKeys.publicDetails(userId), (current) => {
    const base = current ?? normalizePublicProfile({ user_id: userId });
    if (!base) return current;

    const next = {
      ...base,
      userId,
      ...(displayName != null ? { displayName } : null),
      ...(avatarUrl != null ? { avatarUrl } : null),
      ...(isPrivate != null ? { isPrivate } : null),
      ...(patch.bio !== undefined ? { bio: patch.bio ?? "" } : null),
      ...(patch.website !== undefined ? { website: patch.website ?? "" } : null),
      ...(patch.social_links !== undefined || patch.socialLinks !== undefined
        ? { socialLinks: patch.social_links ?? patch.socialLinks ?? {} }
        : null),
    };

    return next;
  });

  clearCachedAuthorProfile(userId);
}
