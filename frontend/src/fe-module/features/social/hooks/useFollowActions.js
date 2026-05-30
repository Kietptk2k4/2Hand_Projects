import { useCallback, useState } from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { followUser, unfollowUser } from "../api/followApi";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { mapSocialWriteError } from "../utils/socialWriteErrors";

export function useFollowActions({ userId, profile, onToast, refetchProfile }) {
  const { showSessionExpired } = useAuthSession();
  const { isWriteBlocked, suspendMessage } = useSocialWriteBlock();
  const [isLoading, setIsLoading] = useState(false);

  const handleFollowToggle = useCallback(async () => {
    if (!userId || !profile || isLoading || isWriteBlocked) {
      return;
    }

    const { followStatus } = profile;
    if (followStatus === "SELF" || !followStatus) {
      return;
    }

    setIsLoading(true);

    try {
      if (followStatus === "NONE") {
        const data = await followUser(userId);
        const message =
          data?.status === "PENDING" ? "Đã gửi yêu cầu theo dõi." : "Đã theo dõi.";
        onToast?.(message);
      } else if (followStatus === "PENDING" || followStatus === "ACCEPTED") {
        await unfollowUser(userId);
        onToast?.("Đã hủy theo dõi.");
      }

      await refetchProfile?.();
    } catch (error) {
      const mapped = mapSocialWriteError(error);
      if (mapped.type === "session") {
        showSessionExpired(mapped.message);
        return;
      }
      if (mapped.type === "suspended") {
        return;
      }
      onToast?.(mapped.message);
    } finally {
      setIsLoading(false);
    }
  }, [
    isLoading,
    isWriteBlocked,
    onToast,
    profile,
    refetchProfile,
    showSessionExpired,
    userId,
  ]);

  return {
    handleFollowToggle,
    isFollowLoading: isLoading,
    followDisabled: isWriteBlocked,
    followDisabledTitle: suspendMessage,
  };
}
