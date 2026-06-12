import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useState } from "react";
import { updateMyPrivacy } from "../../api/authApi";
import { useCurrentUserId } from "../../../social/hooks/useCurrentUserId";
import { handleAccountQueryError } from "../utils/handleAccountQueryError";
import { invalidateAccountCaches } from "../utils/invalidateAccountCaches";
import { useAccountProfile } from "./useAccountProfile";

export function usePrivacySettings({ onSuccess, onError } = {}) {
  const queryClient = useQueryClient();
  const userId = useCurrentUserId();
  const profileQuery = useAccountProfile();
  const isPrivateInitial = Boolean(profileQuery.userProfile?.is_private);

  const [isPrivate, setIsPrivate] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    setIsPrivate(isPrivateInitial);
    setErrorMessage("");
  }, [isPrivateInitial]);

  const mutation = useMutation({
    mutationFn: (nextValue) => updateMyPrivacy({ is_private: nextValue }),
    onMutate: async (nextValue) => {
      const previous = isPrivate;
      setIsPrivate(nextValue);
      setErrorMessage("");
      return { previous };
    },
    onSuccess: async (_, nextValue) => {
      await invalidateAccountCaches(queryClient, userId, {
        profilePatch: { is_private: nextValue },
      });
      onSuccess?.(nextValue);
    },
    onError: async (error, _nextValue, context) => {
      if (context?.previous !== undefined) {
        setIsPrivate(context.previous);
      }
      await handleAccountQueryError(error);
      const message = error?.message || "Cập nhật thất bại. Vui lòng thử lại.";
      setErrorMessage(message);
      onError?.(message);
    },
  });

  const toggle = useCallback(
    (nextValue) => {
      if (mutation.isPending || profileQuery.isLoading) return;
      const targetValue = typeof nextValue === "boolean" ? nextValue : !isPrivate;
      if (targetValue === isPrivate) return;
      mutation.mutate(targetValue);
    },
    [isPrivate, mutation, profileQuery.isLoading]
  );

  return {
    isPrivate,
    isLoading: profileQuery.isLoading,
    isProfileError: profileQuery.isError || profileQuery.isEmpty,
    profileErrorMessage: profileQuery.errorMessage,
    retryProfile: profileQuery.retry,
    isSaving: mutation.isPending,
    errorMessage,
    toggle,
  };
}
