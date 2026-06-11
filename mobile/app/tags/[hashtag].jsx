import { Redirect, useLocalSearchParams } from "expo-router";

export default function TagsHashtagRedirect() {
  const { hashtag } = useLocalSearchParams();
  return <Redirect href={`/hashtag/${hashtag}`} />;
}
