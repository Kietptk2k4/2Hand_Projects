export function apiSuccess(code, message, data = null) {
  return {
    code,
    success: true,
    message,
    data,
    errors: null,
    timestamp: new Date().toISOString()
  };
}

export function apiError(code, message, errors = null) {
  return {
    code,
    success: false,
    message,
    data: null,
    errors,
    timestamp: new Date().toISOString()
  };
}

