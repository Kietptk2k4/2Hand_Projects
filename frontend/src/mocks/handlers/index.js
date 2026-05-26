import { adminInvestigationHandlers } from "./adminInvestigationHandlers";
import { adminRbacHandlers } from "./adminRbacHandlers";
import { authHandlers } from "./authHandlers";

export const handlers = [...authHandlers, ...adminRbacHandlers, ...adminInvestigationHandlers];

