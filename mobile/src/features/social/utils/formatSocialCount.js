export function formatSocialCount(value) {
  if (value === null || value === undefined) return null;
  const num = Number(value) || 0;
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1).replace(/\.0$/, "")}k`;
  }
  return String(num);
}
