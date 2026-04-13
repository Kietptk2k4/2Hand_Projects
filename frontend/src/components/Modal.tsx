import React from 'react';
import { X } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { cn } from '../utils/cn';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  children: React.ReactNode;
  title?: string;
  description?: string;
  className?: string;
}

export const Modal = ({
  isOpen,
  onClose,
  children,
  title,
  description,
  className,
}: ModalProps) => {
  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm"
          />

          {/* Modal Content */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            className={cn(
              'relative w-full max-w-[440px] bg-white rounded-3xl shadow-2xl overflow-hidden',
              className
            )}
          >
            <button
              onClick={onClose}
              className="absolute right-6 top-6 text-slate-400 hover:text-slate-900 transition-colors z-10"
            >
              <X size={24} />
            </button>

            <div className="p-8 sm:p-10">
              {(title || description) && (
                <div className="mb-8">
                  {title && <h2 className="font-headline text-3xl tracking-tight mb-2">{title}</h2>}
                  {description && <p className="text-on-surface-variant text-sm">{description}</p>}
                </div>
              )}
              {children}
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
