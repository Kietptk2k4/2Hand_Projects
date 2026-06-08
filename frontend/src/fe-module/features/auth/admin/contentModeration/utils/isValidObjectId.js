const OBJECT_ID_REGEX = /^[a-f0-9]{24}$/i;

export function isValidObjectId(value) {
  return typeof value === "string" && OBJECT_ID_REGEX.test(value);
}
