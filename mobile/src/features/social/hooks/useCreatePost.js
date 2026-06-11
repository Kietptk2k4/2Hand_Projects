import { useCallback, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createPost } from "../api/createPostApi";
import { invalidateFeedAfterCreate } from "../utils/postFormCache";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import { mapSocialWriteError } from "../utils/socialWriteErrors";
import { usePostComposer } from "./usePostComposer";

export function useCreatePost({ onSuccess } = {}) {
  const queryClient = useQueryClient();
  const composer = usePostComposer();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const mutation = useMutation({
    mutationFn: createPost,
    onSuccess: async (created, variables) => {
      await invalidateFeedAfterCreate(queryClient);
      onSuccess?.(created, { publish: variables.publish });
    },
  });

  const submit = useCallback(
    async (publish) => {
      if (isSubmitting || mutation.isPending) return;
      if (!composer.validateForm({ requireContent: true })) return;

      composer.setGlobalError("");
      setIsSubmitting(true);

      const readyMedia = composer.getReadyMedia();
      const payload = {
        caption: composer.caption.trim() || undefined,
        visibility: composer.visibility,
        allowComments: composer.allowComments,
        hashtags: composer.hashtags.length > 0 ? composer.hashtags : undefined,
        publish,
      };

      if (readyMedia.length > 0) {
        payload.media = readyMedia;
      }

      try {
        await mutation.mutateAsync(payload);
        composer.resetForm();
      } catch (error) {
        const handled = await handleSocialQueryError(error);
        if (handled) return;

        const mapped = mapSocialWriteError(error);
        if (mapped.type === "suspended") {
          composer.setGlobalError(mapped.message);
          return;
        }

        if (mapped.raw?.errors?.length) {
          const fieldMapped = mapped.raw.errors.reduce((acc, item) => {
            if (item.field) acc[item.field] = item.reason;
            return acc;
          }, {});
          composer.setFieldErrors(fieldMapped);
        }

        composer.setGlobalError(mapped.message || "Không tạo được bài viết.");
      } finally {
        setIsSubmitting(false);
      }
    },
    [composer, isSubmitting, mutation]
  );

  return {
    ...composer,
    submit,
    isSubmitting: isSubmitting || mutation.isPending,
  };
}
