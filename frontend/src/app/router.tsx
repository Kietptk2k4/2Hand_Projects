import React from 'react';
import { createBrowserRouter } from 'react-router-dom';
import { LandingPage } from '../features/home/pages/LandingPage';
import { RegisterPage } from '../features/auth/pages/RegisterPage';
import { LoginPage } from '../features/auth/pages/LoginPage';
import { MainLayout } from '../layouts/MainLayout';
import { AuthLayout } from '../layouts/AuthLayout';
import { Button } from '../components/Button';

const Header = () => (
  <header className="fixed top-0 w-full z-50 bg-white/80 backdrop-blur-xl border-b border-slate-200/20">
    <nav className="flex justify-between items-center px-6 py-4 max-w-7xl mx-auto w-full">
      <div className="text-xl font-bold text-slate-900 tracking-tighter font-headline">
        Atelier
      </div>
      <div className="flex items-center gap-6">
        <a href="#" className="text-slate-500 font-label text-[0.6875rem] uppercase tracking-widest hover:text-primary transition-colors duration-300">
          Explore
        </a>
        <Button variant="ghost" size="sm" className="text-primary font-bold uppercase tracking-widest">
          Sign In
        </Button>
      </div>
    </nav>
  </header>
);

const Footer = () => (
  <footer className="w-full border-t border-slate-200/20 bg-slate-50 mt-auto">
    <div className="max-w-7xl mx-auto px-8 py-12 flex flex-col md:flex-row justify-between items-center gap-6">
      <div className="font-label text-[0.6875rem] uppercase tracking-widest text-slate-400">
        © 2024 Digital Atelier. Crafted for excellence.
      </div>
      <div className="flex gap-8">
        <a className="font-label text-[0.6875rem] uppercase tracking-widest text-slate-400 hover:text-slate-900 transition-opacity" href="#">Privacy</a>
        <a className="font-label text-[0.6875rem] uppercase tracking-widest text-slate-400 hover:text-slate-900 transition-opacity" href="#">Terms</a>
        <a className="font-label text-[0.6875rem] uppercase tracking-widest text-slate-400 hover:text-slate-900 transition-opacity" href="#">Support</a>
      </div>
    </div>
  </footer>
);

export const router = createBrowserRouter([
  {
    path: '/',
    element: (
      <MainLayout header={<Header />} footer={<Footer />}>
        <LandingPage />
      </MainLayout>
    ),
  },
  {
    path: '/register',
    element: (
      <AuthLayout>
        <RegisterPage />
      </AuthLayout>
    ),
  },
  {
    path: '/login',
    element: (
      <AuthLayout>
        <LoginPage />
      </AuthLayout>
    ),
  },
]);
