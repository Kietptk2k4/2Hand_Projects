import { adminInvestigationHandlers } from "./adminInvestigationHandlers";
import { adminRbacHandlers } from "./adminRbacHandlers";
import { authHandlers } from "./authHandlers";
import { socialFeedHandlers } from "./socialFeedHandlers";
import { socialCreatePostHandlers } from "./socialCreatePostHandlers";
import { socialEditPostHandlers } from "./socialEditPostHandlers";
import { socialProfileHandlers } from "./socialProfileHandlers";
import { socialRelationsHandlers } from "./socialRelationsHandlers";
import { socialSavedPostsHandlers } from "./socialSavedPostsHandlers";
import { socialSearchPostsHandlers } from "./socialSearchPostsHandlers";
import { socialSearchHashtagHandlers } from "./socialSearchHashtagHandlers";
import { socialPostHandlers } from "./socialPostHandlers";

export const handlers = [
  ...authHandlers,
  ...adminRbacHandlers,
  ...adminInvestigationHandlers,
  ...socialFeedHandlers,
  ...socialSavedPostsHandlers,
  ...socialSearchPostsHandlers,
  ...socialSearchHashtagHandlers,
  ...socialPostHandlers,
  ...socialCreatePostHandlers,
  ...socialEditPostHandlers,
  ...socialProfileHandlers,
  ...socialRelationsHandlers,
];

