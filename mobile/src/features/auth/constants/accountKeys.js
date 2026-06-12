export const accountKeys = {
  all: ["account"],
  me: () => [...accountKeys.all, "me"],
};
