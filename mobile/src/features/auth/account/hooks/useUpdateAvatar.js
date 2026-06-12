import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useState } from "react";
import { resolveDevMediaUrl } from "../../../../shared/utils/resolveDevMediaUrl";
import { requestAvatarUploadUrl, updateMyAvatar } from "../../api/authApi";
import { useCurrentUserId } from "../../../social/hooks/useCurrentUserId";
import { uploadAvatarFile } from "../api/avatarUploadApi";
import { pickAvatarImage } from "../utils/avatarPicker";
import { handleAccountQueryError } from "../utils/handleAccountQueryError";
import { invalidateAccountCaches } from "../utils/invalidateAccountCaches";
import { useAccountProfile } from "./useAccountProfile";

export function useUpdateAvatar({ onSuccess, onError } = {}) {
  const queryClient = useQueryClient();
  const userId = useCurrentUserId();
  const profileQuery = useAccountProfile();

  const currentAvatarUrl = profileQuery.userProfile?.avatar_url || null;
  const [selectedAsset, setSelectedAsset] = useState(null);
  const [previewUri, setPreviewUri] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [uploadProgress, setUploadProgress] = useState(null);

  useEffect(() => {
    if (!selectedAsset) {
      setPreviewUri(currentAvatarUrl ? resolveDevMediaUrl(currentAvatarUrl) : null);
    }
  }, [currentAvatarUrl, selectedAsset]);

  const mutation = useMutation({
    mutationFn: async (asset) => {
      setUploadProgress(0);
      const uploadMeta = await requestAvatarUploadUrl({
        content_type: asset.mimeType,
        file_size_bytes: asset.fileSizeBytes,
      });

      setUploadProgress(40);

      await uploadAvatarFile(uploadMeta.upload_url, {
        uri: asset.uri,
        mimeType: asset.mimeType,
      });

      setUploadProgress(75);

      await updateMyAvatar({ avatar_url: uploadMeta.avatar_url });

      setUploadProgress(100);
      return uploadMeta.avatar_url;
    },
    onSuccess: async (avatarUrl) => {
      await invalidateAccountCaches(queryClient, userId, {
        profilePatch: { avatar_url: avatarUrl },
      });
      setSelectedAsset(null);
      setUploadProgress(null);
      setErrorMessage("");
      onSuccess?.();
    },
    onError: async (error) => {
      await handleAccountQueryError(error);
      const message = error?.message || "Có lỗi xảy ra. Vui lòng thử lại.";
      setErrorMessage(message);
      setUploadProgress(null);
      onError?.(message);
    },
  });

  const pickImage = useCallback(async () => {
    if (mutation.isPending) return;

    setErrorMessage("");
    const result = await pickAvatarImage();

    if (result.errorMessage) {
      setErrorMessage(result.errorMessage);
      return;
    }

    if (result.canceled || !result.asset) {
      return;
    }

    setSelectedAsset(result.asset);
    setPreviewUri(result.asset.uri);
    setUploadProgress(null);
  }, [mutation.isPending]);

  const resetSelection = useCallback(() => {
    if (mutation.isPending) return;
    setSelectedAsset(null);
    setPreviewUri(currentAvatarUrl ? resolveDevMediaUrl(currentAvatarUrl) : null);
    setUploadProgress(null);
    setErrorMessage("");
  }, [currentAvatarUrl, mutation.isPending]);

  const submitUpload = useCallback(() => {
    if (!selectedAsset || mutation.isPending) return;
    setErrorMessage("");
    mutation.mutate(selectedAsset);
  }, [selectedAsset, mutation]);

  return {
    previewUri,
    selectedAsset,
    errorMessage,
    uploadProgress,
    isLoading: profileQuery.isLoading,
    isProfileError: profileQuery.isError || profileQuery.isEmpty,
    profileErrorMessage: profileQuery.errorMessage,
    retryProfile: profileQuery.retry,
    isUploading: mutation.isPending,
    pickImage,
    resetSelection,
    submitUpload,
  };
}