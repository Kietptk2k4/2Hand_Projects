import { mockCommerceProducts } from "./commerceProductListData";
import {
  MOCK_CATEGORY_BUILDING_ID,
  MOCK_CATEGORY_DRILL_ID,
  MOCK_CATEGORY_ELECTRICAL_ID,
  MOCK_CATEGORY_INACTIVE_ID,
  MOCK_CATEGORY_PLUMBING_ID,
  MOCK_CATEGORY_SAFETY_ID,
  MOCK_CATEGORY_TOOLS_ID,
} from "./commerceCategoryIds";

export {
  MOCK_CATEGORY_BUILDING_ID,
  MOCK_CATEGORY_DRILL_ID,
  MOCK_CATEGORY_ELECTRICAL_ID,
  MOCK_CATEGORY_INACTIVE_ID,
  MOCK_CATEGORY_PLUMBING_ID,
  MOCK_CATEGORY_SAFETY_ID,
  MOCK_CATEGORY_TOOLS_ID,
} from "./commerceCategoryIds";

export const mockCommerceCategories = [
  {
    category_id: MOCK_CATEGORY_TOOLS_ID,
    category_name: "Dụng cụ điện",
    category_slug: "dung-cu-dien",
    parent_id: null,
    is_active: true,
  },
  {
    category_id: MOCK_CATEGORY_DRILL_ID,
    category_name: "Máy khoan",
    category_slug: "may-khoan",
    parent_id: MOCK_CATEGORY_TOOLS_ID,
    is_active: true,
  },
  {
    category_id: MOCK_CATEGORY_BUILDING_ID,
    category_name: "Vật liệu xây dựng",
    category_slug: "vat-lieu-xay-dung",
    parent_id: null,
    is_active: true,
  },
  {
    category_id: MOCK_CATEGORY_ELECTRICAL_ID,
    category_name: "Thiết bị điện",
    category_slug: "thiet-bi-dien",
    parent_id: null,
    is_active: true,
  },
  {
    category_id: MOCK_CATEGORY_PLUMBING_ID,
    category_name: "Ống nước",
    category_slug: "ong-nuoc",
    parent_id: null,
    is_active: true,
  },
  {
    category_id: MOCK_CATEGORY_SAFETY_ID,
    category_name: "Bảo hộ lao động",
    category_slug: "bao-ho-lao-dong",
    parent_id: null,
    is_active: true,
  },
  {
    category_id: MOCK_CATEGORY_INACTIVE_ID,
    category_name: "Danh mục ngừng hoạt động",
    category_slug: "inactive-category",
    parent_id: null,
    is_active: false,
  },
];

const categoryById = new Map(mockCommerceCategories.map((cat) => [cat.category_id, cat]));

export function getCategoryById(categoryId) {
  return categoryById.get(categoryId) || null;
}

export function getDescendantCategoryIds(categoryId) {
  const ids = [categoryId];
  const children = mockCommerceCategories.filter((cat) => cat.parent_id === categoryId);
  children.forEach((child) => {
    ids.push(...getDescendantCategoryIds(child.category_id));
  });
  return ids;
}

export function filterProductsByCategory(products, categoryId, includeChildren) {
  if (includeChildren) {
    const allowed = new Set(getDescendantCategoryIds(categoryId));
    return products.filter((product) => allowed.has(product.category_id));
  }
  return products.filter((product) => product.category_id === categoryId);
}

export function countProductsForCategory(categoryId, includeChildren = true) {
  return filterProductsByCategory(mockCommerceProducts, categoryId, includeChildren).length;
}

export function getSidebarCategories() {
  return mockCommerceCategories
    .filter((cat) => cat.is_active)
    .map((cat) => ({
      categoryId: cat.category_id,
      categoryName: cat.category_name,
      categorySlug: cat.category_slug,
      parentId: cat.parent_id,
      productCount: countProductsForCategory(cat.category_id, true),
    }));
}

export const CATEGORY_NAV_ITEMS = [
  { label: "Dụng cụ điện", categoryId: MOCK_CATEGORY_TOOLS_ID },
  { label: "Vật liệu xây dựng", categoryId: MOCK_CATEGORY_BUILDING_ID },
  { label: "Bảo hộ lao động", categoryId: MOCK_CATEGORY_SAFETY_ID },
  { label: "Ống nước", categoryId: MOCK_CATEGORY_PLUMBING_ID },
  { label: "Thiết bị điện", categoryId: MOCK_CATEGORY_ELECTRICAL_ID },
];
