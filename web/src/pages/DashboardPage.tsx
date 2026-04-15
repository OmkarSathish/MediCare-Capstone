import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Calendar, User, ChevronRight, Plus, Loader2 } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { useTitle } from "../hooks/useTitle";
import { appointmentApi } from "../api/appointments";
import { patientApi } from "../api/patients";
import { StatusBadge } from "../components/StatusBadge";
import type { AppointmentResponse, PatientProfileResponse } from "../types";

export default function DashboardPage() {
  useTitle("Dashboard");
  const { user, isAdmin } = useAuth();
  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [patient, setPatient] = useState<PatientProfileResponse | null>(null);
  const [loadingAppts, setLoadingAppts] = useState(true);

  useEffect(() => {
    if (!user) return;
    appointmentApi
      .list()
      .then((r) => {
        setAppointments(r.data.data ?? []);
        setLoadingAppts(false);
      })
      .catch(() => setLoadingAppts(false));

    if (!isAdmin) {
      patientApi
        .getProfile(user.email)
        .then((r) => {
          setPatient(r.data.data ?? null);
        })
        .catch(() => {
          /* no profile yet */
        });
    }
  }, [user, isAdmin]);

  const pending = appointments.filter(
    (a) => a.approvalStatus === "PENDING",
  ).length;
  const approved = appointments.filter(
    (a) => a.approvalStatus === "APPROVED",
  ).length;
  const rejected = appointments.filter(
    (a) => a.approvalStatus === "REJECTED",
  ).length;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Welcome */}
      <div className="mb-8">
        <h1 className="text-2xl font-extrabold text-gray-900">
          Welcome back, {user?.fullName.split(" ")[0]} 👋
        </h1>
        <p className="text-gray-500 mt-1">
          Here's an overview of your healthcare activity.
        </p>
      </div>

      {/* No patient profile warning */}
      {!isAdmin && !patient && (
        <div className="mb-6 bg-yellow-50 border border-yellow-200 rounded-2xl p-5 flex items-start gap-4">
          <User className="w-5 h-5 text-yellow-600 mt-0.5 shrink-0" />
          <div>
            <p className="font-semibold text-yellow-800">
              Complete Your Patient Profile
            </p>
            <p className="text-yellow-700 text-sm mt-1">
              You need a patient profile to book appointments.{" "}
              <Link
                to={`/profile/${user?.email}`}
                className="underline font-medium"
              >
                Set it up now →
              </Link>
            </p>
          </div>
        </div>
      )}

      {/* Stats cards */}
      <div className="grid sm:grid-cols-3 gap-5 mb-8">
        {[
          {
            label: "Pending",
            count: pending,
            color: "bg-yellow-50 border-yellow-200",
            text: "text-yellow-700",
          },
          {
            label: "Approved",
            count: approved,
            color: "bg-green-50 border-green-200",
            text: "text-green-700",
          },
          {
            label: "Rejected",
            count: rejected,
            color: "bg-red-50 border-red-200",
            text: "text-red-700",
          },
        ].map(({ label, count, color, text }) => (
          <div key={label} className={`rounded-2xl border ${color} p-6`}>
            <p className={`text-3xl font-extrabold ${text}`}>{count}</p>
            <p className="text-gray-600 text-sm mt-1">{label} Appointments</p>
          </div>
        ))}
      </div>

      {/* Quick actions */}
      <div className="grid sm:grid-cols-2 gap-4 mb-10">
        {!isAdmin && (
          <Link
            to="/book"
            className="flex items-center gap-4 card hover:shadow-md transition-all hover:border-blue-200 group"
          >
            <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center group-hover:bg-blue-600 transition-colors">
              <Plus className="w-5 h-5 text-blue-600 group-hover:text-white transition-colors" />
            </div>
            <div>
              <p className="font-semibold text-gray-900">Book Appointment</p>
              <p className="text-gray-500 text-sm">
                Schedule a new diagnostic test
              </p>
            </div>
            <ChevronRight className="w-5 h-5 text-gray-400 ml-auto" />
          </Link>
        )}

        <Link
          to={`/profile/${user?.email}`}
          className="flex items-center gap-4 card hover:shadow-md transition-all hover:border-blue-200 group"
        >
          <div className="w-12 h-12 bg-indigo-100 rounded-xl flex items-center justify-center group-hover:bg-indigo-600 transition-colors">
            <User className="w-5 h-5 text-indigo-600 group-hover:text-white transition-colors" />
          </div>
          <div>
            <p className="font-semibold text-gray-900">My Profile</p>
            <p className="text-gray-500 text-sm">Manage patient information</p>
          </div>
          <ChevronRight className="w-5 h-5 text-gray-400 ml-auto" />
        </Link>
      </div>

      {/* Recent appointments */}
      <div className="card">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <Calendar className="w-5 h-5 text-blue-600" /> Recent Appointments
          </h2>
          <Link
            to="/appointments"
            className="text-blue-600 text-sm font-medium hover:underline"
          >
            View all →
          </Link>
        </div>

        {loadingAppts ? (
          <div className="flex items-center justify-center py-12 text-gray-400">
            <Loader2 className="w-6 h-6 animate-spin" />
          </div>
        ) : appointments.length === 0 ? (
          <div className="text-center py-12">
            <Calendar className="w-12 h-12 text-gray-200 mx-auto mb-3" />
            <p className="text-gray-500">No appointments yet.</p>
            {!isAdmin && (
              <Link to="/book" className="btn-primary inline-flex mt-4 text-sm">
                Book your first appointment
              </Link>
            )}
          </div>
        ) : (
          <div className="space-y-3">
            {appointments.slice(0, 5).map((appt) => (
              <Link
                key={appt.id}
                to={`/appointments/${appt.id}`}
                className="flex items-center justify-between p-4 bg-gray-50 hover:bg-blue-50 rounded-xl transition-colors group"
              >
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center">
                    <Calendar className="w-4 h-4 text-blue-600" />
                  </div>
                  <div>
                    <p className="font-semibold text-gray-900 text-sm">
                      {appt.centerName}
                    </p>
                    <p className="text-gray-500 text-xs">
                      {new Date(appt.appointmentDate).toLocaleDateString(
                        "en-US",
                        { year: "numeric", month: "short", day: "numeric" },
                      )}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <StatusBadge status={appt.approvalStatus} />
                  <ChevronRight className="w-4 h-4 text-gray-400 group-hover:text-blue-600 transition-colors" />
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
