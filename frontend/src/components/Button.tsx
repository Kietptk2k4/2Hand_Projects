import React from 'react';
import { cn } from '../utils/cn';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg' | 'xl';
  isLoading?: boolean;
  children?: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  className,
  variant = 'primary',
  size = 'md',
  isLoading,
  children,
  disabled,
  ...props
}) => {
  const variants = {
    primary: 'bg-primary text-white shadow-lg shadow-primary/20 hover:bg-primary/90 active:scale-[0.98]',
    secondary: 'bg-secondary text-white shadow-lg shadow-secondary/20 hover:bg-secondary/90 active:scale-[0.98]',
    outline: 'bg-white text-slate-900 border border-slate-200 hover:bg-slate-50 active:scale-[0.98]',
    ghost: 'bg-transparent text-slate-500 hover:text-primary transition-colors duration-300',
  };

  const sizes = {
    sm: 'h-8 px-3 text-xs',
    md: 'h-10 px-4 text-sm',
    lg: 'h-12 px-6 text-base',
    xl: 'h-14 px-8 text-sm uppercase tracking-widest',
  };

  return (
    <button
      className={cn(
        'inline-flex items-center justify-center rounded-xl font-bold transition-all disabled:opacity-70 disabled:cursor-not-allowed',
        variants[variant],
        sizes[size],
        className
      )}
      disabled={disabled || isLoading}
      {...props}
    >
      {children}
    </button>
  );
};
