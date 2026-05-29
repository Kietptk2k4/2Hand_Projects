export const feedKeys = {
  all: ["social", "feed"],
  global: (page, size) => [...feedKeys.all, "global", page, size],
  following: (page, size) => [...feedKeys.all, "following", page, size],
};
