/**
 * Sprint 2 API enrichments (not blocking FE):
 * - parent_name, children_count, is_leaf on list items
 * - GET /categories/{id}, pagination, block deactivate when product_count > 0
 */

export const CATEGORY_STATUS_OPTIONS = [
  { value: "", label: "Tất cả" },
  { value: "active", label: "Hoạt động" },
  { value: "inactive", label: "Vô hiệu" },
];

export function computeCategoryHeroMetrics(items = []) {
  const list = Array.isArray(items) ? items : [];
  const activeCount = list.filter((item) => item.active).length;
  const inactiveCount = list.length - activeCount;
  const totalProducts = list.reduce((sum, item) => sum + (Number(item.productCount) || 0), 0);

  return {
    total: list.length,
    activeCount,
    inactiveCount,
    totalProducts,
  };
}

export function getCategoryEmptyMessage(statusFilter, query) {
  if (query?.trim()) {
    return `Không có danh mục khớp "${query.trim()}".`;
  }
  if (statusFilter === "active") {
    return "Không có danh mục đang hoạt động.";
  }
  if (statusFilter === "inactive") {
    return "Không có danh mục vô hiệu.";
  }
  return "Chưa có danh mục nào.";
}

export function buildCategoryIndex(items = []) {
  return new Map(items.map((item) => [item.id, item]));
}

export function getCategoryBreadcrumb(item, index) {
  if (!item?.path || !index?.size) return "";
  const segments = String(item.path)
    .split("/")
    .filter(Boolean);
  if (segments.length <= 1) return "";

  const names = segments
    .slice(0, -1)
    .map((id) => index.get(id)?.name)
    .filter(Boolean);

  return names.join(" / ");
}

export function countActiveChildren(categoryId, items = []) {
  return items.filter((item) => item.parentId === categoryId && item.active).length;
}

export function getDescendantIds(categoryId, items = []) {
  const childrenByParent = new Map();
  for (const item of items) {
    if (!item.parentId) continue;
    const list = childrenByParent.get(item.parentId) || [];
    list.push(item.id);
    childrenByParent.set(item.parentId, list);
  }

  const descendants = new Set();
  const stack = [...(childrenByParent.get(categoryId) || [])];
  while (stack.length) {
    const current = stack.pop();
    if (!current || descendants.has(current)) continue;
    descendants.add(current);
    stack.push(...(childrenByParent.get(current) || []));
  }
  return descendants;
}

export function buildCategoryTree(items = []) {
  const sorted = [...items].sort(
    (a, b) => a.level - b.level || a.name.localeCompare(b.name, "vi"),
  );
  const nodes = new Map(sorted.map((item) => [item.id, { ...item, children: [] }]));
  const roots = [];

  for (const item of sorted) {
    const node = nodes.get(item.id);
    if (item.parentId && nodes.has(item.parentId)) {
      nodes.get(item.parentId).children.push(node);
    } else {
      roots.push(node);
    }
  }

  return roots;
}

export function getDefaultExpandedIds(items = [], maxLevel = 1) {
  return new Set(items.filter((item) => item.level <= maxLevel).map((item) => item.id));
}

export function flattenVisibleCategoryTree(nodes = [], expandedIds = new Set(), depth = 0) {
  const rows = [];
  for (const node of nodes) {
    rows.push({ ...node, depth, hasChildren: node.children?.length > 0 });
    if (node.children?.length && expandedIds.has(node.id)) {
      rows.push(...flattenVisibleCategoryTree(node.children, expandedIds, depth + 1));
    }
  }
  return rows;
}

export function filterParentOptions(allItems = [], editingId = null) {
  const descendants = editingId ? getDescendantIds(editingId, allItems) : new Set();
  return allItems
    .filter((item) => item.active && item.id !== editingId && !descendants.has(item.id))
    .sort((a, b) => a.level - b.level || a.name.localeCompare(b.name, "vi"));
}

export function statusFilterToIsActive(statusFilter) {
  if (statusFilter === "active") return true;
  if (statusFilter === "inactive") return false;
  return undefined;
}
