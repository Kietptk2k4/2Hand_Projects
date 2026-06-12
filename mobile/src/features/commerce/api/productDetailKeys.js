export const productDetailKeys = {
  all: ["commerce", "productDetail"],
  detail: (productId) => [...productDetailKeys.all, "detail", productId],
};
