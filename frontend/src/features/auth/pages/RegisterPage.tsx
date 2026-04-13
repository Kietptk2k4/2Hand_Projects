import React, { useState } from 'react';
import { AlertTriangle, CheckCircle2 } from 'lucide-react';
import { motion } from 'framer-motion';
import { AuthForm } from '../components/AuthForm';
import { register } from '../../../api/auth.api';


export const RegisterPage = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('This email is already associated with an account.');
  const [success, setSuccess] = useState(false);

  const handleRegister = async (data: any) => {
    try {
      setIsLoading(true);
      setError('');
      setSuccess(false);

      await register({
        email: data.email,
        password: data.password,
        confirmPassword: data.confirmPassword,
      });

      setSuccess(true);
    } catch (err: any) {
      const message =
        err?.code === 'ERR_NETWORK'
          ? 'Khong ket noi duoc Auth Service. Hay kiem tra backend co dang chay o localhost:8080 khong.'
          :
        err?.response?.data?.message ||
        err?.message ||
        'Register failed';
      setError(message);
    } finally {
      setIsLoading(false);
    }
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
            Register success, please verify your email
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

      <AuthForm type="register" onSubmit={handleRegister} isLoading={isLoading} />
    </div>
  );
};
