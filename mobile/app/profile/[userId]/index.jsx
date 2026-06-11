import { useLocalSearchParams } from "expo-router";
import { ProfileScreen } from "../../../src/features/social/components/ProfileScreen";

export default function ProfileRoute() {
  const { userId } = useLocalSearchParams();
  return <ProfileScreen userId={userId} />;
}
