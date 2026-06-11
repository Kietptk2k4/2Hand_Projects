import { useLocalSearchParams } from "expo-router";
import { EditPostScreen } from "../../../../src/features/social/components/EditPostScreen";

export default function PostEditRoute() {
  const { postId } = useLocalSearchParams();

  return <EditPostScreen postId={postId} />;
}
