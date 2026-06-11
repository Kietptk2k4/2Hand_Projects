export const ROUTES = {
  login: "/(auth)/login",
  feed: "/(tabs)/feed",
  profile: "/(tabs)/profile",
  shop: "/(tabs)/shop",
  postDetail: (postId, { focusComments = false } = {}) => ({
    pathname: "/post/[postId]",
    params: {
      postId,
      ...(focusComments ? { focusComments: "1" } : {}),
    },
  }),
  userProfile: (userId) => `/user/${userId}`,
  saved: "/saved",
  search: "/search",
  suggestions: "/suggestions",
  hashtag: (tag) => `/tags/${encodeURIComponent(tag)}`,
};
