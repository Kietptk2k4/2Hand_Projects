import { useLocalSearchParams, Stack } from "expo-router";
import { LikesListScreen } from "../../../src/features/social/components/LikesListScreen";

export default function PostLikesRoute() {
  const { postId, likeCount, targetType } = useLocalSearchParams();

  const resolvedType = targetType === "comment" ? "comment" : "post";
  const resolvedCount = Number(likeCount) || 0;
  const title =
    resolvedType === "comment"
      ? `Người thích bình luận (${resolvedCount})`
      : `Người thích bài viết (${resolvedCount})`;

  return (
    <>
      <Stack.Screen options={{ title }} />
      <LikesListScreen
        targetType={resolvedType}
        targetId={postId}
        likeCount={resolvedCount}
      />
    </>
  );
}
