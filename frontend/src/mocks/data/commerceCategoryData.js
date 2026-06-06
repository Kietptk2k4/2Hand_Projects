import { mockCommerceProducts } from "./commerceProductListData";
import {
  MOCK_CATEGORY_FASHION_ROOT_ID,
  MOCK_CATEGORY_INACTIVE_ID,
  MOCK_CATEGORY_MEN_ACCESSORIES_ID,
  MOCK_CATEGORY_MEN_BOTTOMS_ID,
  MOCK_CATEGORY_MEN_ID,
  MOCK_CATEGORY_MEN_SHOES_ID,
  MOCK_CATEGORY_MEN_TOPS_ID,
  MOCK_CATEGORY_UNISEX_ID,
  MOCK_CATEGORY_VINTAGE_ID,
  MOCK_CATEGORY_WOMEN_BAGS_ID,
  MOCK_CATEGORY_WOMEN_BOTTOMS_ID,
  MOCK_CATEGORY_WOMEN_ID,
  MOCK_CATEGORY_WOMEN_SHOES_ID,
  MOCK_CATEGORY_WOMEN_TOPS_ID,
} from "./commerceCategoryIds";

export {
  MOCK_CATEGORY_FASHION_ROOT_ID,
  MOCK_CATEGORY_INACTIVE_ID,
  MOCK_CATEGORY_MEN_ACCESSORIES_ID,
  MOCK_CATEGORY_MEN_BOTTOMS_ID,
  MOCK_CATEGORY_MEN_ID,
  MOCK_CATEGORY_MEN_SHOES_ID,
  MOCK_CATEGORY_MEN_TOPS_ID,
  MOCK_CATEGORY_UNISEX_ID,
  MOCK_CATEGORY_VINTAGE_ID,
  MOCK_CATEGORY_WOMEN_BAGS_ID,
  MOCK_CATEGORY_WOMEN_BOTTOMS_ID,
  MOCK_CATEGORY_WOMEN_ID,
  MOCK_CATEGORY_WOMEN_SHOES_ID,
  MOCK_CATEGORY_WOMEN_TOPS_ID,
} from "./commerceCategoryIds";

export const mockCommerceCategories = [
  {
    category_id: MOCK_CATEGORY_FASHION_ROOT_ID,
    category_name: "Thoi trang",
    category_slug: "thoi-trang",
    parent_id: null,
    is_active: true,
    level: 0,
  },
  {
    category_id: MOCK_CATEGORY_WOMEN_ID,
    category_name: "Nu",
    category_slug: "nu",
    parent_id: MOCK_CATEGORY_FASHION_ROOT_ID,
    is_active: true,
    level: 1,
  },
  {
    category_id: MOCK_CATEGORY_WOMEN_TOPS_ID,
    category_name: "Ao nu",
    category_slug: "ao-nu",
    parent_id: MOCK_CATEGORY_WOMEN_ID,
    is_active: true,
    level: 2,
  },
  {
    category_id: MOCK_CATEGORY_WOMEN_BOTTOMS_ID,
    category_name: "Quan & Vay nu",
    category_slug: "quan-vay-nu",
    parent_id: MOCK_CATEGORY_WOMEN_ID,
    is_active: true,
    level: 2,
  },
  {
    category_id: MOCK_CATEGORY_WOMEN_SHOES_ID,
    category_name: "Giay dep nu",
    category_slug: "giay-dep-nu",
    parent_id: MOCK_CATEGORY_WOMEN_ID,
    is_active: true,
    level: 2,
  },
  {
    category_id: MOCK_CATEGORY_WOMEN_BAGS_ID,
    category_name: "Tui & Phu kien nu",
    category_slug: "tui-phu-kien-nu",
    parent_id: MOCK_CATEGORY_WOMEN_ID,
    is_active: true,
    level: 2,
  },
  {
    category_id: MOCK_CATEGORY_MEN_ID,
    category_name: "Nam",
    category_slug: "nam",
    parent_id: MOCK_CATEGORY_FASHION_ROOT_ID,
    is_active: true,
    level: 1,
  },
  {
    category_id: MOCK_CATEGORY_MEN_TOPS_ID,
    category_name: "Ao nam",
    category_slug: "ao-nam",
    parent_id: MOCK_CATEGORY_MEN_ID,
    is_active: true,
    level: 2,
  },
  {
    category_id: MOCK_CATEGORY_MEN_BOTTOMS_ID,
    category_name: "Quan nam",
    category_slug: "quan-nam",
    parent_id: MOCK_CATEGORY_MEN_ID,
    is_active: true,
    level: 2,
  },
  {
    category_id: MOCK_CATEGORY_MEN_SHOES_ID,
    category_name: "Giay dep nam",
    category_slug: "giay-dep-nam",
    parent_id: MOCK_CATEGORY_MEN_ID,
    is_active: true,
    level: 2,
  },
  {
    category_id: MOCK_CATEGORY_MEN_ACCESSORIES_ID,
    category_name: "Phu kien nam",
    category_slug: "phu-kien-nam",
    parent_id: MOCK_CATEGORY_MEN_ID,
    is_active: true,
    level: 2,
  },
  {
    category_id: MOCK_CATEGORY_UNISEX_ID,
    category_name: "Unisex / Streetwear",
    category_slug: "unisex-streetwear",
    parent_id: MOCK_CATEGORY_FASHION_ROOT_ID,
    is_active: true,
    level: 1,
  },
  {
    category_id: MOCK_CATEGORY_VINTAGE_ID,
    category_name: "Vintage & Designer",
    category_slug: "vintage-designer",
    parent_id: MOCK_CATEGORY_FASHION_ROOT_ID,
    is_active: true,
    level: 1,
  },
  {
    category_id: MOCK_CATEGORY_INACTIVE_ID,
    category_name: "Danh muc ngung hoat dong",
    category_slug: "inactive-test",
    parent_id: MOCK_CATEGORY_FASHION_ROOT_ID,
    is_active: false,
    level: 1,
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
  { label: "Nu", categoryId: MOCK_CATEGORY_WOMEN_ID },
  { label: "Nam", categoryId: MOCK_CATEGORY_MEN_ID },
  { label: "Unisex / Streetwear", categoryId: MOCK_CATEGORY_UNISEX_ID },
  { label: "Vintage & Designer", categoryId: MOCK_CATEGORY_VINTAGE_ID },
  { label: "Ao nu", categoryId: MOCK_CATEGORY_WOMEN_TOPS_ID },
];
