import React from 'react';

interface AuthLayoutProps {
  children: React.ReactNode;
}

export const AuthLayout = ({ children }: AuthLayoutProps) => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 p-6">
      <div className="w-full max-w-[440px] bg-white p-8 rounded-3xl shadow-xl">
        {children}
      </div>
    </div>
  );
};
