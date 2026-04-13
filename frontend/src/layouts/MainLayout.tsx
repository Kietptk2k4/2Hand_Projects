import React from 'react';

interface MainLayoutProps {
  children: React.ReactNode;
  header?: React.ReactNode;
  footer?: React.ReactNode;
}

export const MainLayout = ({ children, header, footer }: MainLayoutProps) => {
  return (
    <div className="min-h-screen flex flex-col bg-surface overflow-x-hidden">
      {header}
      <main className="flex-grow flex flex-col items-center justify-center px-6 pt-24 pb-12 relative">
        {children}
      </main>
      {footer}
    </div>
  );
};
