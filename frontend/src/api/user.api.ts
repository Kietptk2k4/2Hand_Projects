import axiosClient from './axiosClient';

export interface UserProfile {
  id: string;
  email: string;
  name?: string;
}

export const getProfile = () => {
  return axiosClient.get<UserProfile>('/user/profile');
};

export const updateProfile = (data: Partial<UserProfile>) => {
  return axiosClient.put<UserProfile>('/user/profile', data);
};