import { AdminErrorPanel } from "../../../components/ui";

export function RbacRetryPanel({ message, onRetry }) {
  return <AdminErrorPanel message={message} onRetry={onRetry} />;
}
