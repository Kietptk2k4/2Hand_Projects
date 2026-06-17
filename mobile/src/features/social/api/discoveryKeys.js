export const discoveryKeys = {
  all: ["social", "discovery"],
  savedPosts: ["social", "discovery", "saved-posts"],
  searchPosts: (q) => ["social", "discovery", "search-posts", q],
  hashtagPosts: (tag) => ["social", "discovery", "hashtag-posts", tag],
  feedSuggestions: ["social", "discovery", "feed-suggestions"],
  suggestedUsers: ["social", "discovery", "suggested-users"],
};
