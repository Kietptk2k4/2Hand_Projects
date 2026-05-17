import { useState } from "react";
import { AuthCard } from "../components/AuthCard";
import { FormField } from "../components/FormField";
import { validateEmail, validatePassword } from "../schemas/authSchemas";

export function LoginPage() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});

  const onChange = (key) => (event) => {
    setForm((prev) => ({ ...prev, [key]: event.target.value }));
  };

  const onSubmit = (event) => {
    event.preventDefault();
    const nextErrors = {
      email: validateEmail(form.email),
      password: validatePassword(form.password),
    };
    setErrors(nextErrors);
  };

  return (
    <AuthCard title="Dang nhap" subtitle="Dang nhap de tiep tuc su dung 2Hands.">
      <form onSubmit={onSubmit} className="space-y-4">
        <FormField
          id="login-email"
          label="Email"
          value={form.email}
          onChange={onChange("email")}
          placeholder="user@example.com"
          error={errors.email}
          required
        />
        <FormField
          id="login-password"
          label="Mat khau"
          type="password"
          value={form.password}
          onChange={onChange("password")}
          placeholder="Nhap mat khau"
          error={errors.password}
          required
        />
        <button
          type="submit"
          className="w-full rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:opacity-90"
        >
          Dang nhap
        </button>
      </form>
    </AuthCard>
  );
}

