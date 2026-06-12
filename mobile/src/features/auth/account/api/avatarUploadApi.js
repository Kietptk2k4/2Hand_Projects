export async function uploadAvatarFile(uploadUrl, { uri, mimeType }) {
  const fileResponse = await fetch(uri);
  const blob = await fileResponse.blob();

  const response = await fetch(uploadUrl, {
    method: "PUT",
    headers: { "Content-Type": mimeType },
    body: blob,
  });

  if (!response.ok) {
    throw {
      code: response.status,
      message: "Upload ảnh thất bại. Vui lòng thử lại.",
    };
  }
}