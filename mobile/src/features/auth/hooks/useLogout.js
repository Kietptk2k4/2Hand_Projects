import { useMutation } from "@tanstack/react-query";
import { useCallback } from "react";
import { logoutWithRefreshToken } from "../api/authApi";
import { getRefreshToken } from "../../../services/auth/tokenStorage";
import { clearAuthSession } from "../utils/clearAuthSession";

export const LOGOUT_FALLBACK_MESSAGE =
  "Đã đăng xuất trên thiết bị này. Nếu cần, vui lòng thử lại.";

async function logoutRequest() {
  const refreshToken = await getRefreshToken();
  let fallbackMessage = null;

  try {
    if (refreshToken) {
      await logoutWithRefreshToken(refreshToken);
    }
  } catch (error) {
    if (error?.code === 500 || error?.code === "NETWORK" || !error?.code) {
      fallbackMessage = LOGOUT_FALLBACK_MESSAGE;
    }
  }

  await clearAuthSession();
  return { fallbackMessage };
}

export function useLogout({ onFallbackMessage } = {}) {
  const mutation = useMutation({
    mutationFn: logoutRequest,
    onSuccess: (result) => {
      if (result?.fallbackMessage) {
        onFallbackMessage?.(result.fallbackMessage);
      }
    },
  });

  const performLogout = useCallback(() => {
    if (mutation.isPending) return;
    mutation.mutate();
  }, [mutation]);

  return {
    performLogout,
    isLoggingOut: mutation.isPending,
  };
}