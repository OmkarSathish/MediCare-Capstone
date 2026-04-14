import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Calendar,
  ChevronRight,
  Loader2,
  Plus,
  X,
  AlertTriangle,
} from "lucide-react";
import { appointmentApi } from "../api/appointments";
import { useAuth } from "../context/AuthContext";
import { StatusBadge } from "../components/StatusBadge";
import type { AppointmentResponse } from "../types";

export default function AppointmentsPage() {
  const { isAdmin } = useAuth();
  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<
    "ALL" | "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED"
  >("ALL");
  const [cancellingId, setCancellingId] = useState<number | null>(null);
  const [confirmId, setConfirmId] = useState<number | null>(null);

  const fetchAppointments = () => {
    appointmentApi
      .list()
      .then((r) => setAppointments(r.data.data ?? []))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  const handleCancel = async (e: React.MouseEvent, id: number) => {
    e.preventDefault();
    setConfirmId(id);
  };

  const confirmCancel = async () => {
    if (confirmId === null) return;
    setCancellingId(confirmId);
    setConfirmId(null);
    try {
      await appointmentApi.cancel(confirmId);
      fetchAppointments();
    } finally {
      setCancellingId(null);
    }
  };

  const filtered =
    filter === "ALL"
      ? appointments
      : appointments.filter((a) => a.approvalStatus === filter);

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Cancel confirmation modal */}
      {confirmId !== null && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 bg-red-100 rounded-xl flex items-center justify-center shrink-0">
                <AlertTriangle className="w-5 h-5 text-red-600" />
              </div>
              <h3 className="text-lg font-bold text-gray-900">
                Cancel Appointment
              </h3>
            </div>
            <p className="text-gray-500 text-sm mb-6">
              Are you sure you want to cancel this appointment? This action
              cannot be undone.
            </p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setConfirmId(null)}
                className="px-4 py-2 text-sm font-medium text-gray-600 border border-gray-200 rounded-xl hover:bg-gray-50 transition-colors"
              >
                Keep Appointment
              </button>
              <button
                onClick={confirmCancel}
                className="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-xl hover:bg-red-700 transition-colors"
              >
                Yes, Cancel
              </button>
            </div>
          </div>
        </div>
      )}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900">
            My Appointments
          </h1>
          <p className="text-gray-500 mt-1">
            {appointments.length} total appointment
            {appointments.length !== 1 ? "s" : ""}
          </p>
        </div>
        {!isAdmin && (
          <Link
            to="/book"
            className="btn-primary inline-flex items-center gap-2"
          >
            <Plus className="w-4 h-4" /> New Appointment
          </Link>
        )}
      </div>

      {/* Filter tabs */}
      <div className="flex gap-2 mb-6 bg-gray-100 rounded-xl p-1 w-fit">
        {(["ALL", "PENDING", "APPROVED", "REJECTED", "CANCELLED"] as const).map(
          (f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all
              ${filter === f ? "bg-white text-blue-600 shadow-sm" : "text-gray-500 hover:text-gray-700"}`}
            >
              {f}
            </button>
          ),
        )}
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-20">
          <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-20 card">
          <Calendar className="w-14 h-14 text-gray-200 mx-auto mb-4" />
          <p className="text-gray-500 text-lg">
            No {filter !== "ALL" ? filter.toLowerCase() : ""} appointments.
          </p>
          {!isAdmin && (
            <Link
              to="/book"
              className="btn-primary inline-flex mt-5 items-center gap-2"
            >
              <Plus className="w-4 h-4" /> Book Now
            </Link>
          )}
        </div>
      ) : (
        <div className="space-y-3">
          {filtered.map((appt) => (
            <Link
              key={appt.id}
              to={`/appointments/${appt.id}`}
              className="flex items-center justify-between p-5 card hover:shadow-md transition-all hover:border-blue-200 group"
            >
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center shrink-0">
                  <Calendar className="w-5 h-5 text-blue-600" />
                </div>
                <div>
                  <p className="font-bold text-gray-900">{appt.centerName}</p>
                  <p className="text-sm text-gray-500">
                    {new Date(appt.appointmentDate).toLocaleDateString(
                      "en-US",
                      { year: "numeric", month: "long", day: "numeric" },
                    )}
                  </p>
                  {appt.specialRequests && (
                    <p className="text-xs text-amber-600 mt-0.5 italic">
                      📋 {appt.specialRequests}
                    </p>
                  )}
                </div>
              </div>
              <div className="flex items-center gap-3">
                <StatusBadge status={appt.approvalStatus} />
                {!isAdmin &&
                  (appt.approvalStatus === "PENDING" ||
                    appt.approvalStatus === "APPROVED") && (
                    <button
                      onClick={(e) => handleCancel(e, appt.id)}
                      disabled={cancellingId === appt.id}
                      className="flex items-center gap-1 text-xs font-medium text-red-600 border border-red-200 rounded-lg px-2.5 py-1.5 hover:bg-red-50 transition-colors disabled:opacity-60"
                    >
                      {cancellingId === appt.id ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      ) : (
                        <X className="w-3.5 h-3.5" />
                      )}
                      Cancel
                    </button>
                  )}
                <ChevronRight className="w-4 h-4 text-gray-400 group-hover:text-blue-600 transition-colors" />
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
