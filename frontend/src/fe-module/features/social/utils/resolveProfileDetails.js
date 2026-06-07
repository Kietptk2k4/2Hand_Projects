export function resolveSelfProfileDetails(accountProfile, user) {
  const profile = accountProfile?.profile ?? {};

  return {
    bio: profile.bio || user?.bio || "",
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
      website: "",
      socialLinks: {},
      isPrivate: false,
      showPrivateNotice: false,
    };
  }

  if (publicProfile.isPrivate) {
    return {
      bio: "",
      website: "",
      socialLinks: {},
      isPrivate: true,
      showPrivateNotice: true,
    };
  }

  return {
    bio: publicProfile.bio || "",
    website: publicProfile.website || "",
    socialLinks: publicProfile.socialLinks ?? {},
    isPrivate: false,
    showPrivateNotice: false,
  };
}
