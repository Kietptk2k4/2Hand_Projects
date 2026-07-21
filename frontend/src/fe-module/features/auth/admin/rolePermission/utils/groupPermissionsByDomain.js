const DOMAIN_LABELS = {
  ADMIN: "Quản trị",
  ASSIGN: "RBAC",
  USER: "Người dùng",
  ORDER: "Đơn hàng",
  PAYMENT: "Thanh toán",
  SHIPMENT: "Vận chuyển",
  WEBHOOK: "Webhook",
  REFUND: "Hoàn tiền",
  SYSTEM: "Hệ thống",
};

export function getPermissionDomain(code = "") {
  const segment = String(code).split("_")[0] || "OTHER";
  return segment.toUpperCase();
}

export function getPermissionDomainLabel(domain) {
  return DOMAIN_LABELS[domain] || domain;
}

/**
 * Groups permission items by first code segment (e.g. USER_READ → USER).
 * Returns [{ domain, label, items }] sorted by label, items by code.
 */
export function groupPermissionsByDomain(permissions = []) {
  const map = new Map();

  for (const perm of permissions) {
    const domain = getPermissionDomain(perm.code);
    if (!map.has(domain)) {
      map.set(domain, []);
    }
    map.get(domain).push(perm);
  }

  return [...map.entries()]
    .map(([domain, items]) => ({
      domain,
      label: getPermissionDomainLabel(domain),
      items: [...items].sort((a, b) => String(a.code).localeCompare(String(b.code))),
    }))
    .sort((a, b) => a.label.localeCompare(b.label, "vi"));
}
