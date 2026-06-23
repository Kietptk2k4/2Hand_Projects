const MAX_URL_LOGS = 15;

let hostContextLogged = false;
const loggedUrlKeys = new Set();
let feedProbeDone = false;

export function logDevMediaHostContext(payload) {
  if (!__DEV__ || hostContextLogged) return;
  hostContextLogged = true;
  console.log("[dev-media-host]", payload);
}

export function logMediaUrlRewrite({ devHost, raw, resolved }) {
  if (!__DEV__ || !raw) return;

  const key = `${raw}->${resolved}`;
  if (loggedUrlKeys.has(key) || loggedUrlKeys.size >= MAX_URL_LOGS) return;

  loggedUrlKeys.add(key);
  console.log("[media-url]", {
    devHost,
    raw,
    resolved,
    changed: raw !== resolved,
  });
}

export function logFeedMediaRaw(url) {
  if (!__DEV__ || !url) return;
  console.log("[feed-media-raw]", url);
}

export async function probeMediaUrlOnce(url) {
  if (!__DEV__ || !url || feedProbeDone) return;
  feedProbeDone = true;

  try {
    const response = await fetch(url, { method: "HEAD" });
    console.log("[media-fetch]", url, response.status);
  } catch (error) {
    console.warn("[media-fetch] fail", url, error?.message || String(error));
  }
}

export function logImageLoad(uri) {
  if (!__DEV__ || !uri) return;
  console.log("[image] loaded", uri);
}

export function logImageError(uri, event) {
  if (!__DEV__ || !uri) return;
  console.warn("[image] error", uri, event?.nativeEvent || event);
}

export function logVideoUri(uri) {
  if (!__DEV__ || !uri) return;
  console.log("[video] uri", uri);
}
const LOCAL_UPLOAD_HOSTS = new Set(["localhost", "127.0.0.1"]);

function parseUrlHost(url) {
  const trimmed = String(url || "").trim();
  const match = trimmed.match(/^https?:\/\/([^/]+)/i);
  return match ? match[1] : "";
}

function isLoopbackUploadHost(host) {
  const hostname = String(host || "").split(":")[0].toLowerCase();
  return LOCAL_UPLOAD_HOSTS.has(hostname);
}

function summarizeUploadUrl(url) {
  const host = parseUrlHost(url);
  return {
    host,
    isLoopback: isLoopbackUploadHost(host),
    url: String(url || "").slice(0, 120),
  };
}

export function logPostMediaPick(asset) {
  if (!__DEV__ || !asset) return;

  const mimeType = asset.mimeType || null;
  const fileSize = asset.fileSize ?? null;

  console.log("[post-upload] pick", {
    mimeType,
    pickerType: asset.type || null,
    fileSize,
    fileSizeMissing: fileSize == null || fileSize <= 0,
    uriScheme: String(asset.uri || "").split(":")[0] || null,
    width: asset.width ?? null,
    height: asset.height ?? null,
    duration: asset.duration ?? null,
  });

  if (fileSize == null || fileSize <= 0) {
    console.warn(
      "[post-upload] fileSize missing from ImagePicker - presign may fail with file_size_bytes INVALID_VALUE"
    );
  }
}

export function logPostMediaValidate({ mimeType, fileSizeBytes, mediaKind, errorMessage }) {
  if (!__DEV__) return;

  console.log("[post-upload] validate", {
    mimeType,
    mediaKind,
    fileSizeBytes,
    ok: !errorMessage,
    errorMessage: errorMessage || null,
  });
}

export function logPostMediaPresignRequest({ contentType, fileSizeBytes, mediaKind }) {
  if (!__DEV__) return;

  console.log("[post-upload] presign-request", {
    contentType,
    fileSizeBytes,
    mediaKind,
    fileSizeInvalid: !fileSizeBytes || fileSizeBytes <= 0,
  });
}

export function logPostMediaPresignOk(meta) {
  if (!__DEV__ || !meta) return;

  const upload = summarizeUploadUrl(meta.uploadUrl);
  const media = summarizeUploadUrl(meta.mediaUrl);

  console.log("[post-upload] presign-ok", {
    mediaKind: meta.mediaKind,
    objectKey: meta.objectKey,
    upload,
    media,
    maxFileSizeBytes: meta.maxFileSizeBytes ?? null,
  });

  if (upload.isLoopback) {
    console.warn(
      "[post-upload] presign upload_url uses loopback host - physical device cannot PUT to localhost; set SOCIAL_MINIO_PUBLIC_URL to LAN IP"
    );
  }
}

export function logPostMediaPresignFail(error) {
  if (!__DEV__) return;

  console.warn("[post-upload] presign-fail", {
    code: error?.code ?? null,
    message: error?.message ?? String(error),
    errors: error?.errors ?? null,
  });
}

export function logPostMediaPutStart({ uploadUrl, blobSize, mimeType }) {
  if (!__DEV__) return;

  const upload = summarizeUploadUrl(uploadUrl);

  console.log("[post-upload] put-start", {
    mimeType,
    blobSize,
    upload,
  });

  if (upload.isLoopback) {
    console.warn("[post-upload] PUT target is loopback - expect network failure on real device");
  }
}

export function logPostMediaPutOk({ uploadUrl, status }) {
  if (!__DEV__) return;

  console.log("[post-upload] put-ok", {
    status,
    upload: summarizeUploadUrl(uploadUrl),
  });
}

export function logPostMediaPutFail({ uploadUrl, status, error, responseBody }) {
  if (!__DEV__) return;

  console.warn("[post-upload] put-fail", {
    status: status ?? null,
    upload: summarizeUploadUrl(uploadUrl),
    message: error?.message ?? String(error),
    responseBody: responseBody ?? null,
  });
}

export function logPostMediaUploadError({ stage, slotId, error, extra }) {
  if (!__DEV__) return;

  console.warn("[post-upload] error", {
    stage,
    slotId: slotId ?? null,
    code: error?.code ?? null,
    message: error?.message ?? String(error),
    errors: error?.errors ?? null,
    ...extra,
  });
}

export function logPostMediaUploadDone({ slotId, mediaUrl }) {
  if (!__DEV__) return;

  console.log("[post-upload] done", {
    slotId,
    media: summarizeUploadUrl(mediaUrl),
  });
}
