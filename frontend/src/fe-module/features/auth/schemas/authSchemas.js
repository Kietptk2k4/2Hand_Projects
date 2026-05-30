export function validateEmail(email) {
  if (!email) return "Vui lòng nhập email.";
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) return "Email không đúng định dạng.";
  if (email.length > 255) return "Email tối đa 255 ký tự.";
  return "";
}

export function validatePassword(password) {
  if (!password?.trim()) return "Vui lòng nhập mật khẩu.";
  if (password.length < 8 || password.length > 32) {
    return "Mật khẩu phải từ 8 đến 32 ký tự.";
  }
  const hasLower = /[a-z]/.test(password);
  const hasUpper = /[A-Z]/.test(password);
  const hasNumber = /[0-9]/.test(password);
  if (!hasLower || !hasUpper || !hasNumber) {
    return "Mật khẩu cần ít nhất 1 chữ hoa, 1 chữ thường và 1 số.";
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
  if (!confirmPassword?.trim()) return "Vui lòng xác nhận mật khẩu.";
  if (confirmPassword !== password) return "Xác nhận mật khẩu không khớp.";
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
  if (!password?.trim()) return "Vui lòng nhập mật khẩu hiện tại.";
  return "";
}

export function validateNewPassword(newPassword, currentPassword) {
  const baseError = validatePassword(newPassword);
  if (baseError) return baseError;
  if (newPassword === currentPassword) {
    return "Mật khẩu mới phải khác mật khẩu hiện tại.";
  }
  return "";
}

export function validateConfirmNewPassword(newPassword, confirmNewPassword) {
  if (!confirmNewPassword?.trim()) return "Vui lòng xác nhận mật khẩu mới.";
  if (confirmNewPassword !== newPassword) return "Xác nhận mật khẩu mới không khớp.";
  return "";
}

export function validateChangePasswordForm(form) {
  const nextErrors = {
    current_password: validateCurrentPassword(form.current_password || ""),
    new_password: validateNewPassword(form.new_password || "", form.current_password || ""),
    confirm_new_password: validateConfirmNewPassword(form.new_password || "", form.confirm_new_password || ""),
  };

  return {
    errors: nextErrors,
    isValid: !nextErrors.current_password && !nextErrors.new_password && !nextErrors.confirm_new_password,
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

export function validateVerifyToken(token) {
  const normalized = token?.trim() || "";
  if (!normalized) return "Vui lòng nhập mã xác thực.";
  if (normalized.length < 6) return "Mã xác thực không hợp lệ.";
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
