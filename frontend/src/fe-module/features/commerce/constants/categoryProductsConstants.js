export { PAGE_SIZE, DEFAULT_SORT } from "./productListConstants";

export const DEFAULT_INCLUDE_CHILDREN = true;

/** Mock sidebar — khop commerceCategoryData.js (product_count khi include_children=true) */
export const SIDEBAR_CATEGORY_ITEMS = [
  {
    categoryId: "f1000000-0000-4000-8000-000000000001",
    categoryName: "Thoi trang",
    productCount: 12,
    parentId: null,
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000010",
    categoryName: "Nu",
    productCount: 6,
    parentId: "f1000000-0000-4000-8000-000000000001",
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000011",
    categoryName: "Ao nu",
    productCount: 2,
    parentId: "f1000000-0000-4000-8000-000000000010",
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000012",
    categoryName: "Quan & Vay nu",
    productCount: 2,
    parentId: "f1000000-0000-4000-8000-000000000010",
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000013",
    categoryName: "Giay dep nu",
    productCount: 1,
    parentId: "f1000000-0000-4000-8000-000000000010",
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000014",
    categoryName: "Tui & Phu kien nu",
    productCount: 1,
    parentId: "f1000000-0000-4000-8000-000000000010",
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000020",
    categoryName: "Nam",
    productCount: 4,
    parentId: "f1000000-0000-4000-8000-000000000001",
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000021",
    categoryName: "Ao nam",
    productCount: 1,
    parentId: "f1000000-0000-4000-8000-000000000020",
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000022",
    categoryName: "Quan nam",
    productCount: 1,
    parentId: "f1000000-0000-4000-8000-000000000020",
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000023",
    categoryName: "Giay dep nam",
    productCount: 1,
    parentId: "f1000000-0000-4000-8000-000000000020",
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000024",
    categoryName: "Phu kien nam",
    productCount: 1,
    parentId: "f1000000-0000-4000-8000-000000000020",
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000030",
    categoryName: "Unisex / Streetwear",
    productCount: 1,
    parentId: "f1000000-0000-4000-8000-000000000001",
  },
  {
    categoryId: "f1000000-0000-4000-8000-000000000040",
    categoryName: "Vintage & Designer",
    productCount: 1,
    parentId: "f1000000-0000-4000-8000-000000000001",
  },
];

export const CATEGORY_DESCRIPTIONS = {
  "thoi-trang": "Thoi trang second-hand C2C — ao, quan, giay va phu kien tu tu do ca nhan.",
  nu: "Do nu second-hand: ao, quan, vay, giay va tui xach.",
  "ao-nu": "Ao nu second-hand tu cac thuong hieu noi tieng.",
  "quan-vay-nu": "Quan, vay va chan vay nu da qua su dung.",
  "giay-dep-nu": "Giay dep nu second-hand.",
  "tui-phu-kien-nu": "Tui xach va phu kien nu.",
  nam: "Thoi trang nam second-hand.",
  "ao-nam": "Ao nam, thun, so mi second-hand.",
  "quan-nam": "Quan jean, kaki va short nam.",
  "giay-dep-nam": "Giay sneaker va giay da nam.",
  "phu-kien-nam": "Mu, that lung va phu kien nam.",
  "unisex-streetwear": "Streetwear unisex — hoodie, ao phong oversized.",
  "vintage-designer": "Do vintage va thiet ke doc dao.",
};
