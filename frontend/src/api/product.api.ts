import axiosClient from './axiosClient';

export const getProducts = () => {
  return axiosClient.get('/products');
};

export const getProductById = (id: string|number) => {
  return axiosClient.get(`/products/${id}`);
};
