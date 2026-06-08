export function readImageDimensions(file) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const image = new Image();
    image.onload = () => {
      URL.revokeObjectURL(url);
      resolve({ width: image.naturalWidth, height: image.naturalHeight });
    };
    image.onerror = () => {
      URL.revokeObjectURL(url);
      reject(new Error("Cannot read image dimensions"));
    };
    image.src = url;
  });
}

export function readVideoDimensions(file) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const video = document.createElement("video");
    video.preload = "metadata";
    video.onloadedmetadata = () => {
      URL.revokeObjectURL(url);
      resolve({ width: video.videoWidth, height: video.videoHeight });
    };
    video.onerror = () => {
      URL.revokeObjectURL(url);
      reject(new Error("Cannot read video dimensions"));
    };
    video.src = url;
  });
}

export async function readFileMediaDimensions(file) {
  if (!file) {
    return { width: null, height: null };
  }
  if (file.type?.startsWith("video/")) {
    return readVideoDimensions(file);
  }
  return readImageDimensions(file);
}
