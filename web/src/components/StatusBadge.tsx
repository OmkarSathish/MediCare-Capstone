import type { ApprovalStatus } from "../types";

export function StatusBadge({ status }: { status: ApprovalStatus }) {
  const map: Record<ApprovalStatus, string> = {
    PENDING: "badge-pending",
    APPROVED: "badge-approved",
    REJECTED: "badge-rejected",
  };
  return <span className={map[status]}>{status}</span>;
}
