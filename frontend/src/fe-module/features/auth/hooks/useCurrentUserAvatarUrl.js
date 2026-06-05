import { useAccountProfile } from "../account/hooks/useAccountProfile";
import { useAuthSession } from "./useAuthSession.jsx";

const DEFAULT_AVATAR_URL = "https://i.pravatar.cc/96?img=11";

export function useCurrentUserAvatarUrl(fallback = DEFAULT_AVATAR_URL) {
  const { user } = useAuthSession();
  const { profile } = useAccountProfile();

  return (
    profile?.profile?.avatar_url ||
    user?.avatar_url ||
    user?.profile?.avatar_url ||
    fallback
  );
}
