import { useLocalSearchParams } from "expo-router";
import { HashtagFeedScreen } from "../../../src/features/social/components/HashtagFeedScreen";

export default function HashtagRoute() {
  const { hashtag } = useLocalSearchParams();
  return <HashtagFeedScreen hashtag={hashtag} />;
}
