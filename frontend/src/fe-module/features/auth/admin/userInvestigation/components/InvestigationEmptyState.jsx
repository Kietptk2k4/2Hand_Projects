import { AdminEmptyPanel } from "../../components/ui";
import { INVESTIGATION_EMPTY_USER_MESSAGE } from "../constants/userInvestigationUiStrings.js";

export function InvestigationEmptyState({
  message = INVESTIGATION_EMPTY_USER_MESSAGE,
  hint = "Tìm theo email/UUID phía trên, hoặc chọn một dòng trong danh sách người dùng.",
}) {
  return (
    <AdminEmptyPanel
      icon="person_search"
      message={message}
      hint={hint}
    />
  );
}
