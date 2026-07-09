export function getUserStatusVariant(status) {
  switch (status) {
    case "ACTIVE":
      return "success";
    case "SUSPENDED":
      return "warning";
    case "BANNED":
      return "danger";
    case "PENDING_VERIFICATION":
      return "neutral";
    case "DELETED":
      return "neutral";
    default:
      return "neutral";
  }
}

export function getEnforcementActionVariant(actionType) {
  switch (actionType) {
    case "BAN":
      return "danger";
    case "SUSPEND":
      return "danger";
    case "RESTRICT":
      return "warning";
    case "WARNING":
      return "warning";
    default:
      return "neutral";
  }
}

export function getEnforcementStatusVariant(status) {
  switch (status) {
    case "ACTIVE":
      return "success";
    case "REVOKED":
      return "neutral";
    case "EXPIRED":
      return "neutral";
    default:
      return "neutral";
  }
}

export function getLoginSuccessVariant(success) {
  return success ? "success" : "danger";
}

export function getSessionStatusVariant(status) {
  switch (status) {
    case "ACTIVE":
      return "success";
    case "LOGGED_OUT":
      return "neutral";
    case "REVOKED":
      return "danger";
    case "EXPIRED":
      return "warning";
    default:
      return "neutral";
  }
}
