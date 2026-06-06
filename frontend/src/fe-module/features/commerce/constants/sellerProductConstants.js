export const PRODUCT_STATUSES = ["DRAFT", "ACTIVE", "OUT_OF_STOCK", "PAUSED", "ARCHIVED"];

export const PRODUCT_CONDITIONS = [
  { value: "LIKE_NEW", label: "Như mới" },
  { value: "GOOD", label: "Tốt" },
  { value: "FAIR", label: "Khá" },
  { value: "USED", label: "Đã qua sử dụng" },
];

export const PRODUCT_TYPE_OPTIONS = [{ value: "PHYSICAL", label: "Hàng hóa vật lý" }];

export const TITLE_MAX = 500;
export const PAGE_SIZE = 10;

export const STATUS_TABS = [
  { id: "all", label: "Tất cả", status: null },
  { id: "active", label: "Đang bán", status: "ACTIVE" },
  { id: "out_of_stock", label: "Hết hàng", status: "OUT_OF_STOCK" },
  { id: "draft", label: "Bản nháp", status: "DRAFT" },
  { id: "paused", label: "Tạm dừng", status: "PAUSED" },
  { id: "archived", label: "Đã lưu trữ", status: "ARCHIVED" },
];

export const STATUS_LABELS = {
  ACTIVE: "Đang bán",
  OUT_OF_STOCK: "Hết hàng",
  DRAFT: "Bản nháp",
  PAUSED: "Tạm dừng",
  ARCHIVED: "Đã lưu trữ",
};

export const STATUS_BADGE_CLASS = {
  ACTIVE: "bg-primary-container text-on-primary-container",
  OUT_OF_STOCK: "bg-error-container text-on-error-container",
  DRAFT: "bg-surface-container-high text-on-surface-variant",
  PAUSED: "border border-outline-variant bg-surface-container-low text-on-surface",
  ARCHIVED: "bg-surface-container-high text-outline",
};

export const WIZARD_STEPS = [
  { id: 1, label: "Thông tin", icon: "info" },
  { id: 2, label: "Giá & Kho", icon: "payments" },
  { id: 3, label: "Hình ảnh & Thuộc tính", icon: "image" },
  { id: 4, label: "Xem lại", icon: "check_circle" },
];

export const ATTRIBUTE_NAME_MAX = 255;
export const ATTRIBUTE_VALUE_MAX = 500;
export const EMPTY_PRODUCT_ATTRIBUTES = [];

export const WIZARD_SESSION_KEY = "commerce-seller-product-wizard-id";

export const READ_ONLY_STATUSES = ["ARCHIVED"];

export const EMPTY_CREATE_PRODUCT_FORM = {
  productType: "PHYSICAL",
  categoryId: "",
  condition: "GOOD",
  title: "",
  description: "",
  weightGram: "",
  price: "",
  salePrice: "",
  saleStartAt: "",
  saleEndAt: "",
  stockQuantity: "",
  lowStockThreshold: "0",
};

export const SELLER_PRODUCT_ERROR_MESSAGES = {
  "COMMERCE-400": "Vui lòng kiểm tra thông tin sản phẩm.",
  "COMMERCE-400-VALIDATION": "Vui lòng kiểm tra thông tin sản phẩm.",
  "COMMERCE-400-MEDIA-URL": "Cần ít nhất một ảnh sản phẩm hợp lệ.",
  "COMMERCE-404-CATEGORY": "Danh mục không tồn tại hoặc đã ngừng hoạt động.",
  "COMMERCE-404-BRAND": "Thương hiệu không tồn tại hoặc đã ngừng hoạt động.",
  "COMMERCE-404-PRODUCT": "Không tìm thấy sản phẩm.",
  "COMMERCE-409-SELLER-SHOP": "Bạn chưa có cửa hàng. Hãy tạo shop trước.",
  "COMMERCE-409-SHOP-STATUS": "Cửa hàng không ở trạng thái hoạt động.",
  "COMMERCE-409-PRICE": "Chưa thiết lập giá bán. Vui lòng thêm giá trước khi đăng bán.",
  "COMMERCE-409-PRODUCT-STATUS": "Trạng thái sản phẩm không cho phép thao tác này.",
  "COMMERCE-409-PRODUCT-REMOVED": "Sản phẩm không khả dụng.",
};

export function mapSellerProductApiError(error) {
  const code = String(error?.code ?? "");
  return SELLER_PRODUCT_ERROR_MESSAGES[code] || error?.message || "Có lỗi xảy ra. Vui lòng thử lại.";
}

export const CONFIRM_ACTIONS = {
  publish: {
    title: "Đăng bán sản phẩm",
    message: "Bạn có chắc muốn đăng bán sản phẩm này?",
    confirmLabel: "Đăng bán",
  },
  pause: {
    title: "Tạm dừng sản phẩm",
    message: "Người mua sẽ không thể đặt hàng sản phẩm này cho đến khi bạn mở bán lại.",
    confirmLabel: "Tạm dừng",
  },
  archive: {
    title: "Lưu trữ sản phẩm",
    message: "Sản phẩm sẽ ẩn khỏi cửa hàng và không còn trong kết quả tìm kiếm.",
    confirmLabel: "Lưu trữ",
  },
};
