import { SELLER_ACTIVE_BRANDS } from "../../fe-module/features/commerce/constants/sellerProductBrands";
import { mockCommerceCategories } from "./commerceCategoryData";

function slugify(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, "")
    .trim()
    .replace(/\s+/g, "-")
    .replace(/-+/g, "-");
}

function buildCategoryPath(categoryId, parentId, categories) {
  if (!parentId) return `/${categoryId}/`;
  const parent = categories.find((item) => item.category_id === parentId);
  const parentPath = parent?.path || buildCategoryPath(parentId, parent?.parent_id ?? null, categories);
  return `${parentPath}${categoryId}/`;
}

function toAdminCategoryRow(cat, categories) {
  const path = cat.path || buildCategoryPath(cat.category_id, cat.parent_id, categories);
  return {
    id: cat.category_id,
    name: cat.category_name,
    slug: cat.category_slug,
    parent_id: cat.parent_id,
    level: cat.level ?? 0,
    path,
    is_active: cat.is_active !== false,
    product_count: cat.product_count ?? 0,
    created_at: cat.created_at || new Date().toISOString(),
    updated_at: cat.updated_at || new Date().toISOString(),
  };
}

let adminCategories = mockCommerceCategories.map((cat) => ({
  ...cat,
  path: buildCategoryPath(cat.category_id, cat.parent_id, mockCommerceCategories),
}));

let adminBrands = SELLER_ACTIVE_BRANDS.map((brand) => ({
  id: brand.id,
  name: brand.name,
  slug: slugify(brand.name === "Khác" ? "khac" : brand.name),
  is_active: true,
  product_count: 0,
  created_at: new Date().toISOString(),
  updated_at: new Date().toISOString(),
}));

export function listAdminCatalogCategories({ isActive, q } = {}) {
  let rows = adminCategories.map((cat) => toAdminCategoryRow(cat, adminCategories));
  if (isActive === true) rows = rows.filter((row) => row.is_active);
  if (isActive === false) rows = rows.filter((row) => !row.is_active);
  if (q) {
    const needle = q.toLowerCase();
    rows = rows.filter(
      (row) => row.name.toLowerCase().includes(needle) || row.slug.toLowerCase().includes(needle),
    );
  }
  return rows;
}

export function createAdminCatalogCategory(body) {
  const id = crypto.randomUUID();
  const slug = body.slug?.trim() || slugify(body.name);
  if (adminCategories.some((cat) => cat.category_slug === slug)) {
    return { error: "COMMERCE-409-CATALOG-SLUG", message: "Catalog slug already exists" };
  }
  const parent = body.parent_id
    ? adminCategories.find((cat) => cat.category_id === body.parent_id)
    : null;
  const level = parent ? (parent.level ?? 0) + 1 : 0;
  const path = buildCategoryPath(id, body.parent_id ?? null, adminCategories);
  const row = {
    category_id: id,
    category_name: body.name.trim(),
    category_slug: slug,
    parent_id: body.parent_id ?? null,
    level,
    path,
    is_active: true,
    product_count: 0,
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
  };
  adminCategories = [...adminCategories, row];
  return { data: toAdminCategoryRow(row, adminCategories) };
}

export function updateAdminCatalogCategory(categoryId, body) {
  const index = adminCategories.findIndex((cat) => cat.category_id === categoryId);
  if (index < 0) return { error: "COMMERCE-404-CATEGORY", message: "Product category not found" };
  const slug = body.slug?.trim() || slugify(body.name);
  if (adminCategories.some((cat) => cat.category_slug === slug && cat.category_id !== categoryId)) {
    return { error: "COMMERCE-409-CATALOG-SLUG", message: "Catalog slug already exists" };
  }
  const parent = body.parent_id
    ? adminCategories.find((cat) => cat.category_id === body.parent_id)
    : null;
  const level = parent ? (parent.level ?? 0) + 1 : 0;
  const path = buildCategoryPath(categoryId, body.parent_id ?? null, adminCategories);
  const updated = {
    ...adminCategories[index],
    category_name: body.name.trim(),
    category_slug: slug,
    parent_id: body.parent_id ?? null,
    level,
    path,
    updated_at: new Date().toISOString(),
  };
  adminCategories = adminCategories.map((cat, i) => (i === index ? updated : cat));
  return { data: toAdminCategoryRow(updated, adminCategories) };
}

export function setAdminCatalogCategoryActive(categoryId, active) {
  const index = adminCategories.findIndex((cat) => cat.category_id === categoryId);
  if (index < 0) return { error: "COMMERCE-404-CATEGORY", message: "Product category not found" };
  if (!active) {
    const hasChildren = adminCategories.some(
      (cat) => cat.parent_id === categoryId && cat.is_active !== false,
    );
    if (hasChildren) {
      return { error: "COMMERCE-409-CATALOG-IN-USE", message: "Category has active child categories" };
    }
  }
  const updated = {
    ...adminCategories[index],
    is_active: active,
    updated_at: new Date().toISOString(),
  };
  adminCategories = adminCategories.map((cat, i) => (i === index ? updated : cat));
  return { data: toAdminCategoryRow(updated, adminCategories) };
}

export function listAdminCatalogBrands({ isActive, q, page = 1, limit = 20 } = {}) {
  let rows = [...adminBrands];
  if (isActive === true) rows = rows.filter((row) => row.is_active);
  if (isActive === false) rows = rows.filter((row) => !row.is_active);
  if (q) {
    const needle = q.toLowerCase();
    rows = rows.filter(
      (row) => row.name.toLowerCase().includes(needle) || row.slug.toLowerCase().includes(needle),
    );
  }
  const total = rows.length;
  const start = (page - 1) * limit;
  return {
    items: rows.slice(start, start + limit),
    pagination: { page, limit, total_items: total },
  };
}

export function listActiveBrands() {
  return adminBrands
    .filter((brand) => brand.is_active)
    .map((brand) => ({
      brand_id: brand.id,
      brand_name: brand.name,
      brand_slug: brand.slug,
    }));
}

export function createAdminCatalogBrand(body) {
  const id = crypto.randomUUID();
  const slug = body.slug?.trim() || slugify(body.name);
  if (adminBrands.some((brand) => brand.slug === slug)) {
    return { error: "COMMERCE-409-CATALOG-SLUG", message: "Catalog slug already exists" };
  }
  const row = {
    id,
    name: body.name.trim(),
    slug,
    is_active: true,
    product_count: 0,
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
  };
  adminBrands = [...adminBrands, row];
  return { data: row };
}

export function updateAdminCatalogBrand(brandId, body) {
  const index = adminBrands.findIndex((brand) => brand.id === brandId);
  if (index < 0) return { error: "COMMERCE-404-BRAND", message: "Product brand not found" };
  if (adminBrands[index].slug === "khac") {
    return { error: "COMMERCE-409-CATALOG-PROTECTED", message: "This catalog item is protected and cannot be modified" };
  }
  const slug = body.slug?.trim() || slugify(body.name);
  if (adminBrands.some((brand) => brand.slug === slug && brand.id !== brandId)) {
    return { error: "COMMERCE-409-CATALOG-SLUG", message: "Catalog slug already exists" };
  }
  const updated = {
    ...adminBrands[index],
    name: body.name.trim(),
    slug,
    updated_at: new Date().toISOString(),
  };
  adminBrands = adminBrands.map((brand, i) => (i === index ? updated : brand));
  return { data: updated };
}

export function setAdminCatalogBrandActive(brandId, active) {
  const index = adminBrands.findIndex((brand) => brand.id === brandId);
  if (index < 0) return { error: "COMMERCE-404-BRAND", message: "Product brand not found" };
  if (adminBrands[index].slug === "khac") {
    return { error: "COMMERCE-409-CATALOG-PROTECTED", message: "This catalog item is protected and cannot be modified" };
  }
  const updated = {
    ...adminBrands[index],
    is_active: active,
    updated_at: new Date().toISOString(),
  };
  adminBrands = adminBrands.map((brand, i) => (i === index ? updated : brand));
  return { data: updated };
}
