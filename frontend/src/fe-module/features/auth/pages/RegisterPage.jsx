import { useState } from "react";
import { AuthCard } from "../components/AuthCard";
import { FormField } from "../components/FormField";
import { validateEmail, validatePassword } from "../schemas/authSchemas";

export function RegisterPage() {
  const [form, setForm] = useState({
    email: "",
    password: "",
    confirm_password: "",
  });
  const [errors, setErrors] = useState({});

  const onChange = (key) => (event) => {
    setForm((prev) => ({ ...prev, [key]: event.target.value }));
  };

  const onSubmit = (event) => {
    event.preventDefault();
    const nextErrors = {
      email: validateEmail(form.email),
      password: validatePassword(form.password),
      confirm_password:
        form.confirm_password !== form.password ? "Confirm password does not match" : "",
    };
    setErrors(nextErrors);
  };

  return (
    <AuthCard title="Dang ky" subtitle="Tao tai khoan moi tren 2Hands.">
      <form onSubmit={onSubmit} className="space-y-4">
        <FormField
          id="register-email"
          label="Email"
          value={form.email}
          onChange={onChange("email")}
          placeholder="user@example.com"
          error={errors.email}
          required
        />
        <FormField
          id="register-password"
          label="Mat khau"
          type="password"
          value={form.password}
          onChange={onChange("password")}
          placeholder="Nhap mat khau"
          error={errors.password}
          required
        />
        <FormField
          id="register-confirm-password"
          label="Xac nhan mat khau"
          type="password"
          value={form.confirm_password}
          onChange={onChange("confirm_password")}
          placeholder="Nhap lai mat khau"
          error={errors.confirm_password}
          required
        />
        <button
          type="submit"
          className="w-full rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:opacity-90"
        >
          Dang ky
        </button>
      </form>
    </AuthCard>
  );
}

