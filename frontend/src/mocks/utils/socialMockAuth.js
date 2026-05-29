import { mockUsers } from "../data/authData";

const OBJECT_ID_REGEX = /^[a-f0-9]{24}$/i;

export function isValidObjectId(value) {
  return typeof value === "string" && OBJECT_ID_REGEX.test(value);
}

export function getUserByToken(request) {
  const authHeader = request.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) return null;
  const token = authHeader.replace("Bearer ", "");
  if (token.includes("expired-access")) return null;
  return mockUsers.find((item) => token.includes(item.id)) || null;
}
