import { useCallback, useEffect, useState } from "react";
import { getMyProfile } from "../../api/authApi";
import { useAuthSession } from "../../hooks/useAuthSession.jsx";

export function useAccountProfile() {
  const { showSessionExpired } = useAuthSession();
  const [profile, setProfile] = useState(null);
  const [status, setStatus] = useState("loading");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");
    try {
      const data = await getMyProfile();
      setProfile(data);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      if (error?.code === 404) {
        setStatus("not_found");
        setErrorMessage(error?.message || "Khong tim thay tai khoan.");
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Khong tai duoc thong tin tai khoan.");
    }
  }, [showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    profile,
    status,
    errorMessage,
    isLoading: status === "loading",
    refetch: load,
  };
}
