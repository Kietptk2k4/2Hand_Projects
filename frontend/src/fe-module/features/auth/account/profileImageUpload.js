export async function uploadProfileImage(file, { requestUploadUrl, persistImageUrl, getPublicUrl, onProgress }) {
  const uploadMeta = await requestUploadUrl({
    content_type: file.type,
    file_size_bytes: file.size,
  });

  onProgress?.(40);

  const putResponse = await fetch(uploadMeta.upload_url, {
    method: "PUT",
    headers: { "Content-Type": file.type },
    body: file,
  });

  if (!putResponse.ok) {
    throw new Error("Upload ảnh thất bại. Vui lòng thử lại.");
  }

  onProgress?.(75);
  await persistImageUrl(getPublicUrl(uploadMeta));
  onProgress?.(100);
}
