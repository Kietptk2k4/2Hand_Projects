import * as FileSystem from "expo-file-system/legacy";

function extensionForMime(mimeType) {
  if (mimeType === "image/png") return ".png";
  if (mimeType === "image/webp") return ".webp";
  if (mimeType === "video/mp4") return ".mp4";
  return ".jpg";
}

export async function resolvePostMediaFileSize(asset) {
  if (asset?.fileSize && asset.fileSize > 0) {
    return asset.fileSize;
  }

  const info = await FileSystem.getInfoAsync(asset.uri, { size: true });
  if (info.exists && info.size > 0) {
    return info.size;
  }

  return 0;
}

export async function resolveUploadableUri(uri, mimeType) {
  const info = await FileSystem.getInfoAsync(uri, { size: true });
  if (info.exists) {
    return { uri, size: info.size ?? 0 };
  }

  const destination = `${FileSystem.cacheDirectory}post-media-${Date.now()}${extensionForMime(mimeType)}`;
  await FileSystem.copyAsync({ from: uri, to: destination });

  const copied = await FileSystem.getInfoAsync(destination, { size: true });
  if (!copied.exists) {
    throw {
      code: "LOCAL_FILE_READ",
      message: "Không đọc được file từ thiết bị.",
    };
  }

  return { uri: destination, size: copied.size ?? 0 };
}
