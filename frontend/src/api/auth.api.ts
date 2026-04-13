import axiosClient from "./axiosClient";

export interface LoginData{
  email: string;
  password: string;
}

export interface RegisterData{
  email: string;
  password: string;
  confirmPassword: string;
}

export const login = (data:LoginData) =>{
  return axiosClient.post('/auth/login',data);
}

export const register = (data:RegisterData) =>{
  return axiosClient.post('/auth/register',data);
}
