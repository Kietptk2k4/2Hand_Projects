export function validateEmail(email) {
  if (!email) return "Vui long nhap email.";
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) return "Email khong dung dinh dang.";
  if (email.length > 255) return "Email toi da 255 ky tu.";
  return "";
}

export function validatePassword(password) {
  if (!password?.trim()) return "Vui long nhap mat khau.";
  return "";
}

export function validateLoginForm(form) {
  const nextErrors = {
    email: validateEmail(form.email?.trim() || ""),
    password: validatePassword(form.password || ""),
  };

  return {
    errors: nextErrors,
    isValid: !nextErrors.email && !nextErrors.password,
  };
}

