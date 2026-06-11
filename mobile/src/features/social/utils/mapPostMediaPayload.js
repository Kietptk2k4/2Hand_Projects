export function mapPostMediaPayload(item) {
  return {
    url: item.url || item.mediaUrl,
    type: item.type,
  };
}
