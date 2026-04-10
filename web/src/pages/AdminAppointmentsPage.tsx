import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Loader2, ChevronRight, Check, X, Search } from "lucide-react";
import { adminAppointmentApi } from "../api/appointments";
import { StatusBadge } from "../components/StatusBadge";
import { useAuth } from "../context/AuthContext";
import type { AppointmentResponse, ApprovalStatus } from "../types";

export default function AdminAppointmentsPage() {
  const { isCenterAdmin, adminCenterId } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<ApprovalStatus | "ALL">("ALL");
  const [search, setSearch] = useState("");
  const [actionId, setActionId] = useState<number | null>(null);
  const [remarks, setRemarks] = useState("");
  const [actionType, setActionType] = useState<"approve" | "reject" | null>(
    null,
  );
  const [processing, setProcessing] = useState(false);

  const statusCodeMap: Record<string, number> = {
    PENDING: 0,
    APPROVED: 1,
    REJECTED: 2,
    CANCELLED: 3,
  };

  const fetchAppointments = async (status: string) => {
    setLoading(true);
    try {
      const params: { status?: number; centerId?: number } =
        status !== "ALL" ? { status: statusCodeMap[status] } : {};
      if (isCenterAdmin && adminCenterId) params.centerId = adminCenterId;
      const res = await adminAppointmentApi.list(params);
      setAppointments(res.data.data ?? []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments(filter);
  }, [filter]);

  // Auto-open review modal if ?review=<id> is in URL
  useEffect(() => {
    const reviewId = searchParams.get("review");
    if (reviewId && appointments.length > 0) {
      const found = appointments.find((a) => a.id === Number(reviewId));
      if (found && found.approvalStatus === "PENDING") {
        setActionId(Number(reviewId));
        setActionType("approve");
        setSearchParams({}, { replace: true });
      }
    }
  }, [searchParams, appointments]);

  const filtered = appointments.filter(
    (a) =>
      a.patientName?.toLowerCase().includes(search.toLowerCase()) ||
      a.centerName?.toLowerCase().includes(search.toLowerCase()),
  );

  const handleAction = async () => {
    if (actionId === null || !actionType) return;
    setProcessing(true);
    try {
      if (actionType === "approve") {
        await adminAppointmentApi.approve(actionId, remarks || undefined);
      } else {
        await adminAppointmentApi.reject(actionId, remarks);
      }
      setActionId(null);
      setRemarks("");
      setActionType(null);
      fetchAppointments(filter);
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">
          Manage Appointments
        </h1>
        <p className="text-gray-500 mt-1">
          {isCenterAdmin
            ? "Appointments at your center — approve or reject requests."
            : "Review, approve, or reject patient appointment requests."}
        </p>
      </div>

      {/* Confirm dialog */}
      {actionId !== null && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <h3 className="font-bold text-gray-900 text-lg mb-1">
              {actionType === "approve"
                ? "Approve Appointment"
                : "Reject Appointment"}
            </h3>
            <p className="text-gray-500 text-sm mb-4">
              {actionType === "approve"
                ? "Add optional remarks for the patient."
                : "Please provide a reason for rejection."}
            </p>
            <textarea
              className="input-field resize-none h-24 mb-4"
              placeholder={
                actionType === "approve"
                  ? "Remarks (optional)…"
                  : "Reason for rejection (required)…"
              }
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
            />
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => {
                  setActionId(null);
                  setRemarks("");
                  setActionType(null);
                }}
                className="btn-outline text-sm"
              >
                Cancel
              </button>
              <button
                onClick={handleAction}
                disabled={
                  processing || (actionType === "reject" && !remarks.trim())
                }
                className={`flex items-center gap-2 font-semibold py-2 px-5 rounded-full text-sm disabled:opacity-60 transition-colors
                  ${actionType === "approve" ? "bg-green-600 hover:bg-green-700 text-white" : "bg-red-600 hover:bg-red-700 text-white"}`}
              >
                {processing ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : actionType === "approve" ? (
                  <Check className="w-4 h-4" />
                ) : (
                  <X className="w-4 h-4" />
                )}
                {actionType === "approve" ? "Approve" : "Reject"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Filters */}
      <div className="flex flex-wrap gap-3 mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            className="input-field pl-9 py-2 text-sm w-64"
            placeholder="Search patient or center…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="flex bg-gray-100 rounded-xl p-1 gap-1">
          {(
            ["ALL", "PENDING", "APPROVED", "REJECTED", "CANCELLED"] as const
          ).map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-4 py-1.5 rounded-lg text-sm font-medium transition-all
                ${filter === f ? "bg-white text-blue-600 shadow-sm" : "text-gray-500 hover:text-gray-700"}`}
            >
              {f}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
        </div>
      ) : (
        <div className="card overflow-hidden p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr className="text-left text-gray-400 text-xs uppercase tracking-wide">
                  <th className="px-5 py-4">ID</th>
                  <th className="px-5 py-4">Patient</th>
                  <th className="px-5 py-4">Center</th>
                  <th className="px-5 py-4">Date</th>
                  <th className="px-5 py-4">Status</th>
                  <th className="px-5 py-4">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {filtered.length === 0 ? (
                  <tr>
                    <td
                      colSpan={6}
                      className="px-5 py-16 text-center text-gray-400"
                    >
                      No appointments found.
                    </td>
                  </tr>
                ) : (
                  filtered.map((a) => (
                    <tr
                      key={a.id}
                      className="hover:bg-gray-50 transition-colors"
                    >
                      <td className="px-5 py-4 font-mono text-gray-400 text-xs">
                        #{a.id}
                      </td>
                      <td className="px-5 py-4 font-medium text-gray-900">
                        {a.patientName}
                      </td>
                      <td className="px-5 py-4 text-gray-600">
                        {a.centerName}
                      </td>
                      <td className="px-5 py-4 text-gray-500">
                        {new Date(a.appointmentDate).toLocaleDateString(
                          "en-US",
                          { month: "short", day: "numeric", year: "numeric" },
                        )}
                      </td>
                      <td className="px-5 py-4">
                        <StatusBadge status={a.approvalStatus} />
                      </td>
                      <td className="px-5 py-4">
                        <div className="flex items-center gap-2">
                          {a.approvalStatus === "PENDING" && (
                            <>
                              <button
                                onClick={() => {
                                  setActionId(a.id);
                                  setActionType("approve");
                                }}
                                className="flex items-center gap-1 text-xs font-medium text-green-600 border border-green-200 rounded-lg px-3 py-1.5 hover:bg-green-50 transition-colors"
                              >
                                <Check className="w-3.5 h-3.5" /> Approve
                              </button>
                              <button
                                onClick={() => {
                                  setActionId(a.id);
                                  setActionType("reject");
                                }}
                                className="flex items-center gap-1 text-xs font-medium text-red-600 border border-red-200 rounded-lg px-3 py-1.5 hover:bg-red-50 transition-colors"
                              >
                                <X className="w-3.5 h-3.5" /> Reject
                              </button>
                            </>
                          )}
                          <Link
                            to={`/appointments/${a.id}`}
                            className="flex items-center gap-1 text-xs text-blue-600 border border-blue-100 rounded-lg px-3 py-1.5 hover:bg-blue-50 transition-colors"
                          >
                            View <ChevronRight className="w-3.5 h-3.5" />
                          </Link>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
