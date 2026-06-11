import { Redirect, useLocalSearchParams } from "expo-router";

export default function UserProfileRedirect() {
  const { userId } = useLocalSearchParams();
  return <Redirect href={`/profile/${userId}`} />;
}
