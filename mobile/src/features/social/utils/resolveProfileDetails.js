import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";

export function normalizePublicProfile(data) {
  if (!data) return null;

  const rawSocialLinks = data.social_links ?? data.socialLinks ?? {};
  const socialLinks =
    rawSocialLinks && typeof rawSocialLinks === "object" && !Array.isArray(rawSocialLinks)
      ? rawSocialLinks
      : {};

  return {
    userId: data.user_id ?? data.userId ?? "",
    displayName: data.display_name ?? data.displayName ?? "",
    username: data.username ?? "",
    avatarUrl: resolveDevMediaUrl(data.avatar_url ?? data.avatarUrl ?? ""),
    bio: data.bio ?? "",
    website: data.website ?? "",
    socialLinks,
    isPrivate: Boolean(data.is_private ?? data.isPrivate),
  };
}

export function resolveSelfProfileDetails(accountProfile) {
  const profile = accountProfile?.profile ?? {};

  return {
    bio: profile.bio || "",
    username: profile.username || "",
    website: profile.website || "",
    socialLinks: profile.social_links ?? profile.socialLinks ?? {},
    isPrivate: Boolean(profile.is_private ?? profile.isPrivate),
    showPrivateNotice: false,
  };
}

export function resolvePublicProfileDetails(publicProfile) {
  if (!publicProfile) {
    return {
      bio: "",
      username: "",
      website: "",
      socialLinks: {},
      isPrivate: false,
      showPrivateNotice: false,
    };
  }

  if (publicProfile.isPrivate) {
    return {
      bio: "",
      username: "",
      website: "",
      socialLinks: {},
      isPrivate: true,
      showPrivateNotice: true,
    };
  }

  return {
    bio: publicProfile.bio || "",
    username: publicProfile.username || "",
    website: publicProfile.website || "",
    socialLinks: publicProfile.socialLinks ?? {},
    isPrivate: false,
    showPrivateNotice: false,
  };
}
