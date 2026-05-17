export function validateEmail(email) {
  if (!email) return "Email is required";
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) return "Email format is invalid";
  if (email.length > 255) return "Email max length is 255";
  return "";
}

export function validatePassword(password) {
  if (!password) return "Password is required";
  if (password.length < 8 || password.length > 32) {
    return "Password must be 8-32 characters";
  }
  return "";
}

