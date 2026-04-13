import React, { useState } from 'react';
import { AlertTriangle, CheckCircle2 } from 'lucide-react';
import { motion } from 'framer-motion';
import { AuthForm } from '../components/AuthForm';

export const LoginPage = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleLogin = (data: any) => {
    setIsLoading(true);
    setError('');
    
    // Simulate API call
    setTimeout(() => {
      setIsLoading(false);
      setSuccess(true);
    }, 1500);
  };

  return (
    <div className="w-full">
      {success && (
        <motion.div 
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          className="mb-6 p-4 rounded-xl bg-green-50 border border-green-100 flex items-start gap-3"
        >
          <CheckCircle2 className="text-green-600 w-4 h-4 mt-0.5" />
          <p className="text-green-800 text-sm font-medium">
            Login success! Welcome back.
          </p>
        </motion.div>
      )}

      {error && !success && (
        <motion.div 
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          className="mb-6 p-4 rounded-xl bg-red-50 border border-red-100 flex items-start gap-3"
        >
          <AlertTriangle className="text-red-600 w-4 h-4 mt-0.5" />
          <p className="text-red-800 text-sm">
            {error}
          </p>
        </motion.div>
      )}

      <AuthForm type="login" onSubmit={handleLogin} isLoading={isLoading} />
    </div>
  );
};
