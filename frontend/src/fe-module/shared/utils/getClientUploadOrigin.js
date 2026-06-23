const LOCAL_HOSTS = new Set(["localhost", "127.0.0.1"]);

/**
 * Dev-only MinIO origin for presigned PUT (must match signature host).
 * Omit on localhost - server presigns with default localhost.
 */
export function getClientUploadOrigin() {
  if (!import.meta.env.DEV) {
    return undefined;
  }

  const host = typeof window !== "undefined" ? window.location.hostname : "";
  if (!host || LOCAL_HOSTS.has(host.toLowerCase())) {
    return undefined;
  }

  return `http://${host}:9000`;
}

export function withClientUploadOrigin(body) {
  const clientUploadOrigin = getClientUploadOrigin();
  if (!clientUploadOrigin) {
    return body;
  }
  return { ...body, client_upload_origin: clientUploadOrigin };
}