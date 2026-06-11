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
  postCreate: ({ pickMedia = false } = {}) => ({
    pathname: "/post/create",
    ...(pickMedia ? { params: { pickMedia: "1" } } : {}),
  }),
  postEdit: (postId) => ({
    pathname: "/post/[postId]/edit",
    params: { postId },
  }),
  userProfile: (userId) => `/profile/${userId}`,
  profileFollowers: (userId) => `/profile/${userId}/followers`,
  profileFollowing: (userId) => `/profile/${userId}/following`,
  saved: "/saved",
  search: "/search",
  suggestions: "/suggestions",
  hashtag: (tag) => `/hashtag/${encodeURIComponent(String(tag).replace(/^#+/, ""))}`,
};
