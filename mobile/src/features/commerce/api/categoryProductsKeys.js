export const categoryProductsKeys = {
  all: ['commerce', 'categoryProducts'],
  list: ({ categoryId, sort, includeChildren, page } = {}) => [
    ...categoryProductsKeys.all,
    'list',
    { categoryId, sort, includeChildren, page },
  ],
};
