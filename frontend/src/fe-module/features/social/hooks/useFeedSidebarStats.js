import { useCallback, useEffect, useState } from "react";
import { fetchSocialProfile } from "../api/profileApi";
import { fetchSavedPosts } from "../api/savedPostsApi";
import { fetchUserPosts } from "../api/userPostsApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useFeedSidebarStats(userId) {
  const { showSessionExpired } = useAuthSession();
  const [postCount, setPostCount] = useState(null);
  const [followerCount, setFollowerCount] = useState(null);
  const [savedCount, setSavedCount] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const load = useCallback(async () => {
    if (!userId) {
      setPostCount(null);
      setFollowerCount(null);
      setSavedCount(null);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    try {
      const [profile, posts, saved] = await Promise.all([
        fetchSocialProfile(userId),
        fetchUserPosts(userId, { page: 0, size: 1, statusFilter: "published" }),
        fetchSavedPosts({ page: 0, size: 1 }),
      ]);
      setFollowerCount(profile?.followerCount ?? 0);
      setPostCount(posts?.meta?.totalElements ?? 0);
      setSavedCount(saved?.meta?.totalElements ?? 0);
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      setPostCount(null);
      setFollowerCount(null);
      setSavedCount(null);
    } finally {
      setIsLoading(false);
    }
  }, [userId, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  return { postCount, followerCount, savedCount, isLoading };
}