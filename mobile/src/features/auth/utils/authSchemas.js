export function validateEmail(email) {
  if (!email) return "Vui long nhap email.";
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) return "Email khong dung dinh dang.";
  if (email.length > 255) return "Email toi da 255 ky tu.";
  return "";
}

export function validatePassword(password) {
  if (!password?.trim()) return "Vui long nhap mat khau.";
  if (password.length < 8 || password.length > 32) {
    return "Mat khau phai tu 8 den 32 ky tu.";
  }
  const hasLower = /[a-z]/.test(password);
  const hasUpper = /[A-Z]/.test(password);
  const hasNumber = /[0-9]/.test(password);
  if (!hasLower || !hasUpper || !hasNumber) {
    return "Mat khau can it nhat 1 chu hoa, 1 chu thuong va 1 so.";
  }
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

export function validateConfirmPassword(password, confirmPassword) {
  if (!confirmPassword?.trim()) return "Vui long xac nhan mat khau.";
  if (confirmPassword !== password) return "Xac nhan mat khau khong khop.";
  return "";
}

export function validateRegisterForm(form) {
  const nextErrors = {
    email: validateEmail(form.email?.trim() || ""),
    password: validatePassword(form.password || ""),
    confirm_password: validateConfirmPassword(form.password || "", form.confirm_password || ""),
  };

  return {
    errors: nextErrors,
    isValid: !nextErrors.email && !nextErrors.password && !nextErrors.confirm_password,
  };
}

export function getPasswordChecklistState(password) {
  return {
    length: password.length >= 8 && password.length <= 32,
    uppercase: /[A-Z]/.test(password),
    lowercase: /[a-z]/.test(password),
    number: /[0-9]/.test(password),
  };
}

export function validateCurrentPassword(password) {
  if (!password?.trim()) return "Vui long nhap mat khau hien tai.";
  return "";
}

export function validateNewPassword(newPassword, currentPassword) {
  const baseError = validatePassword(newPassword);
  if (baseError) return baseError;
  if (newPassword === currentPassword) {
    return "Mat khau moi phai khac mat khau hien tai.";
  }
  return "";
}

export function validateConfirmNewPassword(newPassword, confirmNewPassword) {
  if (!confirmNewPassword?.trim()) return "Vui long xac nhan mat khau moi.";
  if (confirmNewPassword !== newPassword) return "Xac nhan mat khau moi khong khop.";
  return "";
}

export function validateChangePasswordForm(form) {
  const nextErrors = {
    current_password: validateCurrentPassword(form.current_password || ""),
    new_password: validateNewPassword(form.new_password || "", form.current_password || ""),
    confirm_new_password: validateConfirmNewPassword(
      form.new_password || "",
      form.confirm_new_password || ""
    ),
  };

  return {
    errors: nextErrors,
    isValid:
      !nextErrors.current_password && !nextErrors.new_password && !nextErrors.confirm_new_password,
  };
}

export function validateForgotPasswordForm(form) {
  const nextErrors = {
    email: validateEmail(form.email?.trim() || ""),
  };

  return {
    errors: nextErrors,
    isValid: !nextErrors.email,
  };
}

const VERIFY_OTP_PATTERN = /^\d{6}$/;

export function validateVerifyToken(token) {
  const normalized = token?.trim() || "";
  if (!normalized) return "Vui long nhap ma OTP.";
  if (!VERIFY_OTP_PATTERN.test(normalized)) return "Ma OTP 6 chu so khong hop le.";
  return "";
}

export function validateVerifyEmailForm(form) {
  const nextErrors = {
    token: validateVerifyToken(form.token),
  };

  return {
    errors: nextErrors,
    isValid: !nextErrors.token,
  };
}

export function normalizeOtpInput(value) {
  return (value || "").replace(/\D/g, "").slice(0, 6);
}
