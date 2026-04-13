import React, { useState } from 'react';
import { Loader2, Eye, EyeOff } from 'lucide-react';
import { Input } from '../../../components/Input';
import { Button } from '../../../components/Button';

interface AuthFormProps {
  onSubmit: (data: any) => void;
  isLoading?: boolean;
  type?: 'login' | 'register';
}

export const AuthForm = ({ onSubmit, isLoading, type = 'register' }: AuthFormProps) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
  e.preventDefault();

  if (type === 'register' && password !== confirmPassword) {
    return;
  }

  onSubmit({ email, password, confirmPassword });
};

  return (
    <form className="space-y-5" onSubmit={handleSubmit}>
      <Input
        id="email"
        type="email"
        label="Email Address"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="name@company.com"
        required
      />

      <div className="relative">
        <Input
          id="password"
          type={showPassword ? 'text' : 'password'}
          label="Password"
          helperText="Min. 8 chars"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="••••••••"
          required
        />
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute right-4 top-[38px] text-slate-400 hover:text-primary transition-colors"
        >
          {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
        </button>
      </div>

      {type === 'register' && (
        <Input
          id="confirm-password"
          type="password"
          label="Confirm Password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          placeholder="••••••••"
          required
          error={confirmPassword && password !== confirmPassword ? 'Passwords do not match' : ''}
        />
      )}

      <Button
        type="submit"
        isLoading={isLoading}
        className="w-full mt-4"
        size="lg"
      >
        {isLoading ? (
          <Loader2 className="animate-spin w-5 h-5" />
        ) : (
          type === 'register' ? 'Create Account' : 'Sign In'
        )}
      </Button>
    </form>
  );
};
