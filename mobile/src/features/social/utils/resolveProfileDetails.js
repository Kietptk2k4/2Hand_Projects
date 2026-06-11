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
    avatarUrl: data.avatar_url ?? data.avatarUrl ?? "",
    bio: data.bio ?? "",
    website: data.website ?? "",
    socialLinks,
    isPrivate: Boolean(data.is_private ?? data.isPrivate),
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
