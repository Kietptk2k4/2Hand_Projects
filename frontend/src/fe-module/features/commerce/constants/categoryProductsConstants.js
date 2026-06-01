export { PAGE_SIZE, DEFAULT_SORT } from "./productListConstants";

export const DEFAULT_INCLUDE_CHILDREN = true;

/** Mock sidebar — khớp commerceCategoryData.js (product_count khi include_children=true) */
export const SIDEBAR_CATEGORY_ITEMS = [
  {
    categoryId: "c2000000-0000-4000-8000-000000000001",
    categoryName: "Dụng cụ điện",
    productCount: 5,
    parentId: null,
  },
  {
    categoryId: "c2000000-0000-4000-8000-000000000002",
    categoryName: "Máy khoan",
    productCount: 1,
    parentId: "c2000000-0000-4000-8000-000000000001",
  },
  {
    categoryId: "c2000000-0000-4000-8000-000000000003",
    categoryName: "Vật liệu xây dựng",
    productCount: 2,
    parentId: null,
  },
  {
    categoryId: "c2000000-0000-4000-8000-000000000004",
    categoryName: "Thiết bị điện",
    productCount: 3,
    parentId: null,
  },
  {
    categoryId: "c2000000-0000-4000-8000-000000000005",
    categoryName: "Ống nước",
    productCount: 2,
    parentId: null,
  },
  {
    categoryId: "c2000000-0000-4000-8000-000000000006",
    categoryName: "Bảo hộ lao động",
    productCount: 4,
    parentId: null,
  },
];

export const CATEGORY_DESCRIPTIONS = {
  "dung-cu-dien": "Công cụ và thiết bị điện chuyên nghiệp cho thợ và nhà thầu.",
  "may-khoan": "Máy khoan và phụ kiện — danh mục con của dụng cụ điện.",
  "vat-lieu-xay-dung": "Vật liệu xây dựng và hóa chất thi công chất lượng cao.",
  "thiet-bi-dien": "Dây điện, đèn LED và thiết bị điện dân dụng — công nghiệp.",
  "ong-nuoc": "Ống PVC, van và phụ kiện hệ thống nước.",
  "bao-ho-lao-dong": "Mũ bảo hộ, găng tay và thiết bị an toàn lao động.",
};
