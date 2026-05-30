import { useCallback, useEffect, useState } from "react";
import { fetchSocialProfile } from "../api/profileApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useSocialProfile(userId) {
  const { showSessionExpired } = useAuthSession();
  const [profile, setProfile] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [errorCode, setErrorCode] = useState(null);

  const load = useCallback(async () => {
    if (!userId) {
      setProfile(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");
    setErrorCode(null);
    setProfile(null);

    try {
      const data = await fetchSocialProfile(userId);
      setProfile(data);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorCode(error?.code || 500);
      setErrorMessage(error?.message || "Không tải được hồ sơ.");
    }
  }, [userId, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    profile,
    status,
    errorMessage,
    errorCode,
    isLoading: status === "loading",
    isError: status === "error",
    retry: load,
  };
}
