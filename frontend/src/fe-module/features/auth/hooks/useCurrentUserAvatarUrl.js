import { useAccountProfile } from "../account/hooks/useAccountProfile";
import { useAuthSession } from "./useAuthSession.jsx";
import { resolveDevMediaUrl } from "../../../shared/utils/getClientUploadOrigin";

const DEFAULT_AVATAR_URL = "https://i.pravatar.cc/96?img=11";

export function useCurrentUserAvatarUrl(fallback = DEFAULT_AVATAR_URL) {
  const { user } = useAuthSession();
  const { profile } = useAccountProfile();

  const raw =
    profile?.profile?.avatar_url ||
    user?.avatar_url ||
    user?.profile?.avatar_url ||
    fallback;

  return resolveDevMediaUrl(raw) || fallback;
}
