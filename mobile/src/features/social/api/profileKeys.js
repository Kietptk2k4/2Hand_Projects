export const profileKeys = {
  all: ["social", "profile"],
  detail: (userId) => [...profileKeys.all, "detail", userId],
  publicDetails: (userId) => [...profileKeys.all, "public-details", userId],
  posts: (userId, statusFilter = "published") => [
    ...profileKeys.all,
    "posts",
    userId,
    statusFilter,
  ],
  relations: (userId, type) => [...profileKeys.all, "relations", userId, type],
};
