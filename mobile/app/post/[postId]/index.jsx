import { useLocalSearchParams } from "expo-router";
import { PostDetailScreen } from "../../../src/features/social/components/PostDetailScreen";

export default function PostDetailRoute() {
  const { postId, focusComments } = useLocalSearchParams();
  const shouldFocusComments = focusComments === "1" || focusComments === "true";

  return (
    <PostDetailScreen postId={postId} focusComments={shouldFocusComments} />
  );
}
