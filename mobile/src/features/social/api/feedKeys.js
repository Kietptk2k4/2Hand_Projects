import { FEED_TABS } from "../constants/feedTabs";

export const feedKeys = {
  all: ["social", "feed"],
  tab: (tab = FEED_TABS.GLOBAL) => [...feedKeys.all, tab],
};
