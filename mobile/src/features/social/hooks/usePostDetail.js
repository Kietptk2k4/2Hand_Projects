import { useQuery } from "@tanstack/react-query";
import { fetchPostDetail } from "../api/postApi";
import { postKeys } from "../api/postKeys";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";

export function usePostDetail(postId) {
  const query = useQuery({
    queryKey: postKeys.detail(postId),
    queryFn: async () => {
      try {
        return await fetchPostDetail(postId);
      } catch (error) {
        const handled = await handleSocialQueryError(error);
        if (handled) throw error;
        throw error;
      }
    },
    enabled: Boolean(postId),
  });

  const errorCode = query.error?.code ?? null;

  return {
    post: query.data ?? null,
    isLoading: query.isLoading,
    isError: query.isError,
    errorMessage: query.error?.message || "Không tải được bài viết.",
    errorCode,
    retry: query.refetch,
  };
}
