import { Navigate, useSearchParams } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { buildAdminSearchParams } from "../../auth/admin/adminUrlParams.js";

/** Redirect legacy URL → tab trong /admin */
export function CommerceAdminReviewModerationPage() {
  const [searchParams] = useSearchParams();

  const next = buildAdminSearchParams({
    section: "contentModeration",
    tab: "review-moderation",
    preserve: searchParams,
  });

  return <Navigate to={`${APP_ROUTES.admin}?${next.toString()}`} replace />;
}
