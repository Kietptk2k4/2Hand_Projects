import { useEffect } from "react";
import { router } from "expo-router";
import { ROUTES } from "../../src/shared/constants/routes";

export default function ChangePasswordRedirectRoute() {
  useEffect(() => {
    router.replace(ROUTES.accountPassword);
  }, []);

  return null;
}