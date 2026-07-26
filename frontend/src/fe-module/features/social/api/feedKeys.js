export const feedKeys = {
  all: ["social", "feed"],
  global: (page, size) => [...feedKeys.all, "global", page, size],
  forYou: (page, size) => [...feedKeys.all, "for-you", page, size],
  following: (page, size) => [...feedKeys.all, "following", page, size],
};
