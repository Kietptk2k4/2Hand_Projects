export const productSearchKeys = {
  all: ['commerce', 'productSearch'],
  list: ({ q, sort, page } = {}) => [...productSearchKeys.all, 'list', { q, sort, page }],
};
