import { profileKeys } from "../api/profileKeys";

export function patchProfileCache(queryClient, userId, patch) {
  queryClient.setQueryData(profileKeys.detail(userId), (old) =>
    old ? { ...old, ...patch } : old
  );
}

export function snapshotProfileCache(queryClient, userId) {
  return queryClient.getQueryData(profileKeys.detail(userId));
}

export function restoreProfileCache(queryClient, userId, snapshot) {
  if (snapshot !== undefined) {
    queryClient.setQueryData(profileKeys.detail(userId), snapshot);
  }
}

export function computeOptimisticFollow(profile, action) {
  if (!profile) return {};

  if (action === "follow") {
    const nextStatus = profile.isPrivate ? "PENDING" : "ACCEPTED";
    const patch = { followStatus: nextStatus };

    if (nextStatus === "ACCEPTED") {
      patch.canViewFullProfile = true;
      if (profile.followerCount !== null && profile.followerCount !== undefined) {
        patch.followerCount = Number(profile.followerCount) + 1;
      }
    } else {
      patch.canViewFullProfile = false;
    }

    return patch;
  }

  const patch = { followStatus: "NONE", canViewFullProfile: false };

  if (profile.followStatus === "ACCEPTED") {
    if (profile.followerCount !== null && profile.followerCount !== undefined) {
      patch.followerCount = Math.max(0, Number(profile.followerCount) - 1);
    }
  }

  if (profile.isPrivate) {
    patch.followerCount = null;
    patch.followingCount = null;
  }

  return patch;
}

export function reconcileFollowSuccess(profile, followData, action) {
  if (action === "follow") {
    const nextStatus = followData?.status || (profile?.isPrivate ? "PENDING" : "ACCEPTED");
    const patch = { followStatus: nextStatus };

    if (nextStatus === "ACCEPTED") {
      patch.canViewFullProfile = true;
    }

    return patch;
  }

  return {
    followStatus: "NONE",
    canViewFullProfile: false,
  };
}
