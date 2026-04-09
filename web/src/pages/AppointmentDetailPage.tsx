import { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import {
  Calendar,
  Building2,
  User,
  Microscope,
  ArrowLeft,
  Clock,
  Loader2,
  X,
  CheckCircle,
  AlertTriangle,
} from "lucide-react";
import { appointmentApi } from "../api/appointments";
import { StatusBadge } from "../components/StatusBadge";
import type { AppointmentDetailResponse } from "../types";

export default function AppointmentDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [appt, setAppt] = useState<AppointmentDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!id) return;
    appointmentApi
      .getById(Number(id))
      .then((r) => setAppt(r.data.data ?? null))
      .catch(() => setError("Appointment not found."))
      .finally(() => setLoading(false));
  }, [id]);

  const handleCancel = async () => {
    if (!id) return;
    setCancelling(true);
    setShowCancelModal(false);
    try {
      await appointmentApi.cancel(Number(id));
      navigate("/appointments");
    } catch {
      setError("Failed to cancel appointment.");
    } finally {
      setCancelling(false);
    }
  };

  if (loading)
    return (
      <div className="flex items-center justify-center min-h-96">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );

  if (error || !appt)
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center">
        <p className="text-red-500">{error || "Not found."}</p>
        <Link to="/appointments" className="btn-primary inline-flex mt-4">
          Back to Appointments
        </Link>
      </div>
    );

  const totalCost = appt.diagnosticTests.reduce((s, t) => s + t.testPrice, 0);

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10">
      {/* Cancel confirmation modal */}
      {showCancelModal && (
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
                onClick={() => setShowCancelModal(false)}
                className="px-4 py-2 text-sm font-medium text-gray-600 border border-gray-200 rounded-xl hover:bg-gray-50 transition-colors"
              >
                Keep Appointment
              </button>
              <button
                onClick={handleCancel}
                className="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-xl hover:bg-red-700 transition-colors"
              >
                Yes, Cancel
              </button>
            </div>
          </div>
        </div>
      )}
      <Link
        to="/appointments"
        className="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-blue-600 mb-6"
      >
        <ArrowLeft className="w-4 h-4" /> Back to Appointments
      </Link>

      {/* Header */}
      <div className="card mb-5">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <div className="flex items-center gap-3 mb-2">
              <h1 className="text-2xl font-extrabold text-gray-900">
                Appointment #{appt.id}
              </h1>
              <StatusBadge status={appt.approvalStatus} />
            </div>
            <p className="text-gray-500 text-sm flex items-center gap-1.5">
              <Calendar className="w-4 h-4" />
              {new Date(appt.appointmentDate).toLocaleDateString("en-US", {
                weekday: "long",
                year: "numeric",
                month: "long",
                day: "numeric",
              })}
            </p>
            {appt.remarks && (
              <p className="mt-2 text-sm text-gray-500 italic bg-gray-50 rounded-lg px-3 py-2">
                Remarks: "{appt.remarks}"
              </p>
            )}
          </div>
          {(appt.approvalStatus === "PENDING" ||
            appt.approvalStatus === "APPROVED") && (
            <button
              onClick={() => setShowCancelModal(true)}
              disabled={cancelling}
              className="flex items-center gap-2 text-sm font-medium text-red-600 border border-red-200 rounded-xl px-4 py-2 hover:bg-red-50 transition-colors disabled:opacity-60"
            >
              {cancelling ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <X className="w-4 h-4" />
              )}
              Cancel Appointment
            </button>
          )}
        </div>
      </div>

      <div className="grid md:grid-cols-2 gap-5 mb-5">
        {/* Center */}
        <div className="card">
          <h2 className="font-bold text-gray-900 mb-3 flex items-center gap-2">
            <Building2 className="w-4 h-4 text-blue-600" /> Diagnostic Center
          </h2>
          <p className="font-semibold text-gray-800">{appt.center.name}</p>
          <p className="text-sm text-gray-500 mt-0.5">{appt.center.address}</p>
          <Link
            to={`/centers/${appt.center.id}`}
            className="text-blue-600 text-sm mt-2 inline-block hover:underline"
          >
            View center →
          </Link>
        </div>

        {/* Patient */}
        <div className="card">
          <h2 className="font-bold text-gray-900 mb-3 flex items-center gap-2">
            <User className="w-4 h-4 text-blue-600" /> Patient
          </h2>
          <p className="font-semibold text-gray-800">{appt.patient.name}</p>
          <div className="text-sm text-gray-500 space-y-0.5 mt-1">
            <p>
              Age: {appt.patient.age} • Gender: {appt.patient.gender || "N/A"}
            </p>
            {appt.patient.phoneNo && <p>Phone: {appt.patient.phoneNo}</p>}
          </div>
        </div>
      </div>

      {/* Tests */}
      <div className="card mb-5">
        <h2 className="font-bold text-gray-900 mb-4 flex items-center gap-2">
          <Microscope className="w-4 h-4 text-blue-600" /> Diagnostic Tests
        </h2>
        <div className="space-y-2">
          {appt.diagnosticTests.map((t) => (
            <div
              key={t.id}
              className="flex items-center justify-between p-3 bg-gray-50 rounded-xl"
            >
              <div className="flex items-center gap-2">
                <CheckCircle className="w-4 h-4 text-green-500" />
                <span className="text-sm font-medium text-gray-800">
                  {t.testName}
                </span>
              </div>
              <span className="text-blue-600 font-semibold text-sm">
                ₹{t.testPrice.toFixed(2)}
              </span>
            </div>
          ))}
          <div className="flex items-center justify-between p-3 bg-blue-50 rounded-xl font-bold">
            <span className="text-gray-900">Total Amount</span>
            <span className="text-blue-600">₹{totalCost.toFixed(2)}</span>
          </div>
        </div>
      </div>

      {/* Status history */}
      {appt.statusHistory?.length > 0 && (
        <div className="card">
          <h2 className="font-bold text-gray-900 mb-4 flex items-center gap-2">
            <Clock className="w-4 h-4 text-blue-600" /> Status History
          </h2>
          <div className="space-y-4">
            {appt.statusHistory.map((h, i) => (
              <div key={i} className="flex gap-4">
                <div className="flex flex-col items-center">
                  <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center shrink-0">
                    <Clock className="w-4 h-4 text-blue-600" />
                  </div>
                  {i < appt.statusHistory.length - 1 && (
                    <div className="w-0.5 h-full bg-gray-100 mt-1" />
                  )}
                </div>
                <div className="pb-4">
                  <div className="flex items-center gap-2 mb-1">
                    {h.previousStatus && (
                      <>
                        <StatusBadge status={h.previousStatus} />
                        <span className="text-gray-400 text-xs">→</span>
                      </>
                    )}
                    <StatusBadge status={h.newStatus} />
                  </div>
                  <p className="text-xs text-gray-500">By {h.changedBy}</p>
                  <p className="text-xs text-gray-400">
                    {new Date(h.changedAt).toLocaleString()}
                  </p>
                  {h.comments && (
                    <p className="text-sm text-gray-600 mt-1 italic">
                      "{h.comments}"
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
