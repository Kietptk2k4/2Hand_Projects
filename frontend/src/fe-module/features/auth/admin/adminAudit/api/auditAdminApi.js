import { getUsersForRbac } from "../../../api/authApi.js";

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function isAuditAdminUuid(value) {
  return UUID_RE.test(String(value || "").trim());
}

function normalizeRbacUser(row) {
  if (!row) return null;
  return {
    id: row.id,
    email: row.email,
    displayName: row.display_name || "",
    status: row.status,
    roleCodes: row.role_codes || [],
  };
}

export function formatAuditAdminSummary(admin) {
  if (!admin) return "";
  const name = admin.displayName?.trim();
  if (name && name !== admin.email) {
    return `${admin.email} — ${name}`;
  }
  return admin.email || admin.id || "";
}

export async function searchAuditAdmins(query, { size = 20 } = {}) {
  const trimmed = String(query || "").trim();
  if (!trimmed) {
    return [];
  }

  const data = await getUsersForRbac({
    q: trimmed,
    sort: "email",
    page: 1,
    size,
  });

  return (data?.items || []).map(normalizeRbacUser).filter(Boolean);
}

export async function lookupAuditAdminById(adminId) {
  const trimmed = String(adminId || "").trim();
  if (!isAuditAdminUuid(trimmed)) return null;

  const data = await getUsersForRbac({
    q: trimmed,
    sort: "email",
    page: 1,
    size: 5,
  });

  const match =
    (data?.items || []).find((item) => item.id === trimmed) || data?.items?.[0] || null;
  return normalizeRbacUser(match);
}
