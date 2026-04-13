import React from 'react';
import { cn } from '../utils/cn';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, label, error, helperText, id, ...props }, ref) => {
    return (
      <div className="space-y-1.5">
        {label && (
          <div className="flex justify-between items-end">
            <label className="block font-label text-xs font-bold uppercase tracking-wider text-slate-500" htmlFor={id}>
              {label}
            </label>
            {helperText && (
              <span className="text-[10px] text-slate-400 uppercase tracking-widest">
                {helperText}
              </span>
            )}
          </div>
        )}
        <input
          id={id}
          ref={ref}
          className={cn(
            'w-full h-12 px-4 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all placeholder:text-slate-400',
            error && 'border-red-500 focus:ring-red-500/20 focus:border-red-500',
            className
          )}
          {...props}
        />
        {error && (
          <p className="text-red-600 font-label text-[0.6875rem] mt-1">
            {error}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
