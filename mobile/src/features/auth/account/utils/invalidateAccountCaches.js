import { accountKeys } from "../../constants/accountKeys";
import { profileKeys } from "../../../social/api/profileKeys";
import { applyAccountProfilePatch } from "./syncAccountProfileCaches";

export async function invalidateAccountCaches(
  queryClient,
  userId,
  { profilePatch } = {}
) {
  if (userId && profilePatch) {
    applyAccountProfilePatch(queryClient, userId, profilePatch);
  }

  const tasks = [
    queryClient.invalidateQueries({ queryKey: accountKeys.me() }),
  ];

  if (userId) {
    tasks.push(
      queryClient.invalidateQueries({ queryKey: profileKeys.detail(userId) }),
      queryClient.invalidateQueries({ queryKey: profileKeys.publicDetails(userId) })
    );
  }

  await Promise.all(tasks);
}
