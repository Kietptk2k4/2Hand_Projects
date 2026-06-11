import { useLocalSearchParams } from "expo-router";
import { CreatePostScreen } from "../../../src/features/social/components/CreatePostScreen";

export default function PostCreateRoute() {
  const { pickMedia } = useLocalSearchParams();
  const openPickerOnMount = pickMedia === "1" || pickMedia === "true";

  return <CreatePostScreen openPickerOnMount={openPickerOnMount} />;
}
