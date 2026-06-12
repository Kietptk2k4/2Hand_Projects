import * as ImagePicker from "expo-image-picker";
import { AVATAR_ALLOWED_TYPES, AVATAR_MAX_BYTES } from "../../utils/accountSchemas";

export const AVATAR_TYPE_ERROR = "Định dạng không được hỗ trợ. Chỉ JPG, PNG, WEBP.";
export const AVATAR_SIZE_ERROR = "Tệp vượt quá 5MB.";
export const AVATAR_PERMISSION_ERROR = "Cần quyền truy cập thư viện ảnh để chọn avatar.";

function normalizeMimeType(asset) {
  if (asset.mimeType) return asset.mimeType;
  return "image/jpeg";
}

async function resolveFileSize(asset) {
  if (asset.fileSize && asset.fileSize > 0) {
    return asset.fileSize;
  }
  const response = await fetch(asset.uri);
  const blob = await response.blob();
  return blob.size;
}

export function validateAvatarAsset(asset, mimeType, fileSizeBytes) {
  if (!AVATAR_ALLOWED_TYPES.includes(mimeType)) {
    return AVATAR_TYPE_ERROR;
  }
  if (fileSizeBytes > AVATAR_MAX_BYTES) {
    return AVATAR_SIZE_ERROR;
  }
  return "";
}

export async function pickAvatarImage() {
  const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
  if (!permission.granted) {
    return { canceled: true, errorMessage: AVATAR_PERMISSION_ERROR };
  }

  const result = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    allowsEditing: true,
    aspect: [1, 1],
    quality: 0.9,
  });

  if (result.canceled || !result.assets?.length) {
    return { canceled: true };
  }

  const asset = result.assets[0];
  const mimeType = normalizeMimeType(asset);
  const fileSizeBytes = await resolveFileSize(asset);
  const validationError = validateAvatarAsset(asset, mimeType, fileSizeBytes);

  if (validationError) {
    return { canceled: true, errorMessage: validationError };
  }

  return {
    canceled: false,
    asset: {
      uri: asset.uri,
      mimeType,
      fileSizeBytes,
    },
  };
}