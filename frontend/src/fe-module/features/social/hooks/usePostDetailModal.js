import { useCallback } from "react";
import { useSearchParams } from "react-router-dom";

export function usePostDetailModal() {
  const [searchParams, setSearchParams] = useSearchParams();
  const postId = searchParams.get("postId");
  const focusComments = searchParams.get("focusComments") === "1";

  const openPost = useCallback(
    (id, { focusComments: shouldFocusComments = false } = {}) => {
      if (!id) return;
      setSearchParams(
        (prev) => {
          const next = new URLSearchParams(prev);
          next.set("postId", id);
          if (shouldFocusComments) {
            next.set("focusComments", "1");
          } else {
            next.delete("focusComments");
          }
          return next;
        },
        { replace: false }
      );
    },
    [setSearchParams]
  );

  const closePost = useCallback(() => {
    setSearchParams(
      (prev) => {
        const next = new URLSearchParams(prev);
        next.delete("postId");
        next.delete("focusComments");
        return next;
      },
      { replace: true }
    );
  }, [setSearchParams]);

  return {
    postId,
    focusComments,
    isOpen: Boolean(postId),
    openPost,
    closePost,
  };
}
