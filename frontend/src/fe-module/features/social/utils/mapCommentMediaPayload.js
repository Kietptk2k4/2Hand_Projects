export function mapCommentMediaPayload(mediaItems = []) {
  return mediaItems
    .filter((item) => item.status === "done" && item.mediaUrl)
    .map((item) => ({
      url: item.mediaUrl,
      type: item.type,
    }));
}