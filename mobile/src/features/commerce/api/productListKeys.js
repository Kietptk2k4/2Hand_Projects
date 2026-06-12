export const productListKeys = {
  all: ["commerce", "productList"],
  list: ({ page, limit, sort } = {}) => [...productListKeys.all, "list", { page, limit, sort }],
};
