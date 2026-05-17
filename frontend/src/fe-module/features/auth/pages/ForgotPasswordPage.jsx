import { useState } from "react";
import { AuthCard } from "../components/AuthCard";
import { FormField } from "../components/FormField";
import { validateEmail } from "../schemas/authSchemas";

export function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");

  const onSubmit = (event) => {
    event.preventDefault();
    setError(validateEmail(email));
  };

  return (
    <AuthCard title="Quen mat khau" subtitle="Nhap email de nhan huong dan dat lai mat khau.">
      <form onSubmit={onSubmit} className="space-y-4">
        <FormField
          id="forgot-password-email"
          label="Email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="user@example.com"
          error={error}
          required
        />
        <button
          type="submit"
          className="w-full rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:opacity-90"
        >
          Send reset link
        </button>
      </form>
    </AuthCard>
  );
}

