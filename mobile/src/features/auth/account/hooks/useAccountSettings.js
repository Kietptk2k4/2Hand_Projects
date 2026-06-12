import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useMemo, useState } from "react";
import { updateMySettings } from "../../api/authApi";
import { useAppearance } from "../../context/AppearanceContext";
import { normalizeAppearanceMode } from "../../utils/appearanceTheme";
import { useCurrentUserId } from "../../../social/hooks/useCurrentUserId";
import { handleAccountQueryError } from "../utils/handleAccountQueryError";
import { invalidateAccountCaches } from "../utils/invalidateAccountCaches";
import { useAccountProfile } from "./useAccountProfile";

export function useAccountSettings({ onSuccess, onError } = {}) {
  const queryClient = useQueryClient();
  const userId = useCurrentUserId();
  const { setAppearanceMode: applyAppearanceMode } = useAppearance();
  const profileQuery = useAccountProfile();

  const savedModeFromDb = useMemo(
    () => normalizeAppearanceMode(profileQuery.settings?.appearance_mode),
    [profileQuery.settings?.appearance_mode]
  );

  const [appearanceMode, setAppearanceMode] = useState("SYSTEM");
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    setAppearanceMode(savedModeFromDb);
    setErrorMessage("");
  }, [savedModeFromDb]);

  const mutation = useMutation({
    mutationFn: () => updateMySettings({ appearance_mode: appearanceMode }),
    onSuccess: async (data) => {
      const saved = normalizeAppearanceMode(data?.appearance_mode || appearanceMode);
      applyAppearanceMode(saved);
      setAppearanceMode(saved);
      await invalidateAccountCaches(queryClient, userId);
      onSuccess?.(saved);
    },
    onError: async (error) => {
      await handleAccountQueryError(error);
      const message = error?.message || "Có lỗi xảy ra. Vui lòng thử lại.";
      setErrorMessage(message);
      onError?.(message);
    },
  });

  const submit = useCallback(() => {
    if (mutation.isPending || profileQuery.isLoading) return;
    setErrorMessage("");
    mutation.mutate();
  }, [mutation, profileQuery.isLoading]);

  const isDirty = normalizeAppearanceMode(appearanceMode) !== savedModeFromDb;

  return {
    appearanceMode,
    setAppearanceMode,
    savedModeFromDb,
    isLoading: profileQuery.isLoading,
    isProfileError: profileQuery.isError || profileQuery.isEmpty,
    profileErrorMessage: profileQuery.errorMessage,
    retryProfile: profileQuery.retry,
    isSubmitting: mutation.isPending,
    errorMessage,
    submit,
    isDirty,
  };
}
