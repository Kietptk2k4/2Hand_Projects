export const productReviewsKeys = {
  all: ["commerce", "productReviews"],
  list: ({ productId, page, limit, sort, rating } = {}) => [
    ...productReviewsKeys.all,
    "list",
    { productId, page, limit, sort, rating },
  ],
  myReview: (productId) => [...productReviewsKeys.all, "myReview", productId],
};
