export function shopStatusBadgeVariant(status) {
  switch (status) {
    case "ACTIVE":
      return "success";
    case "SUSPENDED":
      return "danger";
    case "CLOSED":
      return "neutral";
    default:
      return "neutral";
  }
}

export function reviewStatusBadgeVariant(status) {
  switch (status) {
    case "VISIBLE":
      return "success";
    case "HIDDEN":
      return "danger";
    default:
      return "neutral";
  }
}

export function productStatusBadgeVariant(status) {
  switch (status) {
    case "ACTIVE":
      return "success";
    case "OUT_OF_STOCK":
      return "warning";
    case "REMOVED":
      return "danger";
    case "PAUSED":
      return "warning";
    default:
      return "neutral";
  }
}
