import { Navigate } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

/** @deprecated Use /account/password — legacy redirect */
export function ChangePasswordPage() {
  return <Navigate to={APP_ROUTES.accountPassword} replace />;
}
