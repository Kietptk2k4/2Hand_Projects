import { useLocalSearchParams } from "expo-router";
import { FollowListScreen } from "../../../src/features/social/components/FollowListScreen";

export default function ProfileFollowingRoute() {
  const { userId } = useLocalSearchParams();
  return <FollowListScreen userId={userId} relationType="following" />;
}
