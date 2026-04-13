import axios, { AxiosInstance } from 'axios';

const axiosClient: AxiosInstance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export default axiosClient;
