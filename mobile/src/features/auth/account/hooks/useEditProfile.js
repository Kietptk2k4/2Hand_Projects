import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useMemo, useState } from "react";
import { updateMyProfile } from "../../api/authApi";
import {
  mapSocialLinksToObject,
  mapSocialLinksToRows,
  validateEditProfileForm,
} from "../../utils/accountSchemas";
import { useCurrentUserId } from "../../../social/hooks/useCurrentUserId";
import { handleAccountQueryError } from "../utils/handleAccountQueryError";
import { invalidateAccountCaches } from "../utils/invalidateAccountCaches";
import { resolveServerFieldErrors } from "../utils/resolveServerFieldErrors";
import { useAccountProfile } from "./useAccountProfile";

const EMPTY_FORM = {
  display_name: "",
  bio: "",
  website: "",
  social_links: [{ platform: "github", url: "" }],
};

export function useEditProfile({ onSuccess, onError } = {}) {
  const queryClient = useQueryClient();
  const userId = useCurrentUserId();
  const profileQuery = useAccountProfile();
  const userProfile = profileQuery.userProfile;

  const [form, setForm] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});
  const [globalError, setGlobalError] = useState("");

  useEffect(() => {
    if (!userProfile) return;
    setForm({
      display_name: userProfile.display_name || "",
      bio: userProfile.bio || "",
      website: userProfile.website || "",
      social_links: mapSocialLinksToRows(userProfile.social_links),
    });
    setErrors({});
    setGlobalError("");
  }, [
    userProfile?.display_name,
    userProfile?.bio,
    userProfile?.website,
    userProfile?.social_links,
  ]);

  const validation = useMemo(() => validateEditProfileForm(form), [form]);

  const mutation = useMutation({
    mutationFn: async (payload) => updateMyProfile(payload),
    onSuccess: async (updatedProfile) => {
      await invalidateAccountCaches(queryClient, userId, {
        profilePatch: updatedProfile,
      });
      onSuccess?.();
    },
    onError: async (error) => {
      await handleAccountQueryError(error);
      const serverErrors = resolveServerFieldErrors(error?.errors);
      if (Object.keys(serverErrors).length > 0) {
        setErrors((prev) => ({ ...prev, ...serverErrors }));
      }
      const message = error?.message || "Có lỗi xảy ra. Vui lòng thử lại.";
      setGlobalError(message);
      onError?.(message);
    },
  });

  const updateField = useCallback((field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: "" }));
    setGlobalError("");
  }, []);

  const updateSocialRow = useCallback((index, key, value) => {
    setForm((prev) => {
      const rows = [...prev.social_links];
      rows[index] = { ...rows[index], [key]: value };
      return { ...prev, social_links: rows };
    });
    setErrors((prev) => ({ ...prev, [`social_links.${index}.url`]: "" }));
    setGlobalError("");
  }, []);

  const addSocialRow = useCallback(() => {
    setForm((prev) => ({
      ...prev,
      social_links: [...prev.social_links, { platform: "other", url: "" }],
    }));
  }, []);

  const removeSocialRow = useCallback((index) => {
    setForm((prev) => ({
      ...prev,
      social_links: prev.social_links.filter((_, i) => i !== index),
    }));
  }, []);

  const resetForm = useCallback(() => {
    if (!userProfile) return;
    setForm({
      display_name: userProfile.display_name || "",
      bio: userProfile.bio || "",
      website: userProfile.website || "",
      social_links: mapSocialLinksToRows(userProfile.social_links),
    });
    setErrors({});
    setGlobalError("");
  }, [userProfile]);

  const submit = useCallback(() => {
    const nextValidation = validateEditProfileForm(form);
    setErrors(nextValidation.errors);
    if (!nextValidation.isValid || mutation.isPending) return;

    setGlobalError("");
    mutation.mutate({
      display_name: form.display_name.trim(),
      bio: form.bio?.trim() || "",
      website: form.website?.trim() || "",
      social_links: mapSocialLinksToObject(form.social_links),
    });
  }, [form, mutation]);

  return {
    form,
    errors,
    globalError,
    validation,
    isLoading: profileQuery.isLoading,
    isProfileError: profileQuery.isError || profileQuery.isEmpty,
    profileErrorMessage: profileQuery.errorMessage,
    retryProfile: profileQuery.retry,
    isSubmitting: mutation.isPending,
    updateField,
    updateSocialRow,
    addSocialRow,
    removeSocialRow,
    resetForm,
    submit,
  };
}