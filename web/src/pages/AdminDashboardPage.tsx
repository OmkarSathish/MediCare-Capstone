import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Building2,
  Microscope,
  Clock,
  ChevronRight,
  Loader2,
  LayoutDashboard,
  UserCog,
} from "lucide-react";
import { adminApi } from "../api/admin";
import { adminAppointmentApi } from "../api/appointments";
import { StatusBadge } from "../components/StatusBadge";
import { useAuth } from "../context/AuthContext";
import type { AdminDashboardResponse, AppointmentResponse } from "../types";

export default function AdminDashboardPage() {
  const { isCenterAdmin, adminCenterId } = useAuth();
  const [stats, setStats] = useState<AdminDashboardResponse | null>(null);
  const [recentAppts, setRecentAppts] = useState<AppointmentResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const apptParams =
      isCenterAdmin && adminCenterId
        ? { centerId: adminCenterId, status: 0 }
        : { status: 0 };

    const fetches = isCenterAdmin
      ? [
          Promise.resolve({ data: { data: null } }), // skip stats for center admin
          adminAppointmentApi.list(apptParams),
        ]
      : [adminApi.getDashboard(), adminAppointmentApi.list(apptParams)];

    Promise.all(fetches)
      .then(([statsRes, apptsRes]) => {
        setStats((statsRes as any).data.data ?? null);
        setRecentAppts((apptsRes as any).data.data ?? []);
      })
      .finally(() => setLoading(false));
  }, [isCenterAdmin, adminCenterId]);

  if (loading)
    return (
      <div className="flex items-center justify-center min-h-96">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <div className="mb-8 flex items-center gap-3">
        <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center">
          <LayoutDashboard className="w-5 h-5 text-white" />
        </div>
        <div>
          <h1 className="text-2xl font-extrabold text-gray-900">
            {isCenterAdmin ? "Center Admin Dashboard" : "Admin Dashboard"}
          </h1>
          <p className="text-gray-500 text-sm">
            {isCenterAdmin
              ? "Manage appointments and tests for your center"
              : "Manage the healthcare platform"}
          </p>
        </div>
      </div>

      {/* Stat cards */}
      <div className="grid sm:grid-cols-3 gap-5 mb-10">
        {(isCenterAdmin
          ? [
              {
                label: "Pending Appointments",
                value: recentAppts.length,
                icon: Clock,
                color: "bg-yellow-50 text-yellow-600",
                link: "/admin/appointments",
              },
            ]
          : [
              {
                label: "Diagnostic Centers",
                value: stats?.totalCenters ?? 0,
                icon: Building2,
                color: "bg-blue-50 text-blue-600",
                link: "/admin/centers",
              },
              {
                label: "Diagnostic Tests",
                value: stats?.totalTests ?? 0,
                icon: Microscope,
                color: "bg-indigo-50 text-indigo-600",
                link: "/admin/tests",
              },
            ]
        ).map(({ label, value, icon: Icon, color, link }) => (
          <Link
            key={label}
            to={link}
            className="card hover:shadow-md transition-all hover:border-blue-200 group"
          >
            <div className="flex items-center justify-between mb-4">
              <div
                className={`w-12 h-12 ${color} rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform`}
              >
                <Icon className="w-6 h-6" />
              </div>
              <ChevronRight className="w-5 h-5 text-gray-300 group-hover:text-blue-600 transition-colors" />
            </div>
            <div className="text-3xl font-extrabold text-gray-900">
              {value.toLocaleString()}
            </div>
            <div className="text-gray-500 text-sm mt-1">{label}</div>
          </Link>
        ))}
      </div>

      {/* Admin quick actions */}
      <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-10">
        {(isCenterAdmin
          ? [
              {
                label: "Manage Appointments",
                desc: "Approve or reject pending appointments",
                href: "/admin/appointments",
                icon: Clock,
              },
              {
                label: "Center Tests",
                desc: "Manage tests offered at your center",
                href: "/admin/tests",
                icon: Microscope,
              },
            ]
          : [
              {
                label: "Manage Centers",
                desc: "Add, edit, or deactivate diagnostic centers",
                href: "/admin/centers",
                icon: Building2,
              },
              {
                label: "Manage Tests",
                desc: "Add and edit new tests",
                href: "/admin/tests",
                icon: Microscope,
              },
              {
                label: "Center Admins",
                desc: "Assign admins to diagnostic centers",
                href: "/admin/center-admins",
                icon: UserCog,
              },
            ]
        ).map(({ label, desc, href, icon: Icon }) => (
          <Link
            key={label}
            to={href}
            className="flex items-center gap-4 card hover:shadow-md transition-all hover:border-blue-200 group"
          >
            <div className="w-11 h-11 bg-blue-50 rounded-xl flex items-center justify-center shrink-0 group-hover:bg-blue-600 transition-colors">
              <Icon className="w-5 h-5 text-blue-600 group-hover:text-white transition-colors" />
            </div>
            <div>
              <p className="font-semibold text-gray-900 text-sm">{label}</p>
              <p className="text-gray-400 text-xs">{desc}</p>
            </div>
            <ChevronRight className="w-4 h-4 text-gray-300 ml-auto group-hover:text-blue-600 transition-colors" />
          </Link>
        ))}
      </div>

      {/* Pending appointments table */}
      <div className="card">
        <div className="flex items-center justify-between mb-5">
          <h2 className="font-bold text-gray-900 text-lg flex items-center gap-2">
            <Clock className="w-5 h-5 text-yellow-500" /> Pending Appointments
          </h2>
          <Link
            to="/admin/appointments"
            className="text-blue-600 text-sm hover:underline"
          >
            View all →
          </Link>
        </div>

        {recentAppts.length === 0 ? (
          <div className="text-center py-10">
            <Clock className="w-10 h-10 text-gray-200 mx-auto mb-3" />
            <p className="text-gray-400 text-sm">No pending appointments.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-400 text-xs uppercase tracking-wide border-b border-gray-100">
                  <th className="pb-3 pr-4">ID</th>
                  <th className="pb-3 pr-4">Patient</th>
                  <th className="pb-3 pr-4">Center</th>
                  <th className="pb-3 pr-4">Date</th>
                  <th className="pb-3 pr-4">Status</th>
                  <th className="pb-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {recentAppts.slice(0, 8).map((a) => (
                  <tr key={a.id} className="hover:bg-gray-50 transition-colors">
                    <td className="py-3 pr-4 font-mono text-gray-400">
                      #{a.id}
                    </td>
                    <td className="py-3 pr-4 font-medium text-gray-900">
                      {a.patientName}
                    </td>
                    <td className="py-3 pr-4 text-gray-500">{a.centerName}</td>
                    <td className="py-3 pr-4 text-gray-500">
                      {new Date(a.appointmentDate).toLocaleDateString("en-US", {
                        month: "short",
                        day: "numeric",
                        year: "numeric",
                      })}
                    </td>
                    <td className="py-3 pr-4">
                      <StatusBadge status={a.approvalStatus} />
                    </td>
                    <td className="py-3">
                      <Link
                        to={`/admin/appointments?review=${a.id}`}
                        className="text-blue-600 hover:underline text-xs"
                      >
                        Review →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
