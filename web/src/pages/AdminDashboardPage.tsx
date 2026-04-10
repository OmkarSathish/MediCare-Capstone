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
  Users,
  CalendarCheck,
} from "lucide-react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
  LineChart,
  Line,
  CartesianGrid,
} from "recharts";
import { adminApi } from "../api/admin";
import { adminAppointmentApi } from "../api/appointments";
import { StatusBadge } from "../components/StatusBadge";
import { useAuth } from "../context/AuthContext";
import type { AdminDashboardResponse, AppointmentResponse } from "../types";

const STATUS_COLORS = ["#f59e0b", "#22c55e", "#ef4444", "#6b7280"];
const BAR_COLOR = "#3b82f6";
const LINE_COLOR = "#6366f1";

export default function AdminDashboardPage() {
  const { isCenterAdmin, adminCenterId } = useAuth();
  const [stats, setStats] = useState<AdminDashboardResponse | null>(null);
  const [recentAppts, setRecentAppts] = useState<AppointmentResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (isCenterAdmin) {
      const params = adminCenterId
        ? { centerId: adminCenterId, status: 0 }
        : { status: 0 };
      adminAppointmentApi
        .list(params)
        .then((r) => setRecentAppts(r.data.data ?? []))
        .finally(() => setLoading(false));
    } else {
      adminApi
        .getDashboard()
        .then((r) => setStats(r.data.data ?? null))
        .finally(() => setLoading(false));
    }
  }, [isCenterAdmin, adminCenterId]);

  if (loading)
    return (
      <div className="flex items-center justify-center min-h-96">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );

  // ── CENTER ADMIN VIEW ──────────────────────────────────────────────────────
  if (isCenterAdmin) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="mb-8 flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center">
            <LayoutDashboard className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-2xl font-extrabold text-gray-900">
              Center Admin Dashboard
            </h1>
            <p className="text-gray-500 text-sm">
              Manage appointments and tests for your center
            </p>
          </div>
        </div>

        {/* Stat card */}
        <div className="grid sm:grid-cols-3 gap-5 mb-10">
          <Link
            to="/admin/appointments"
            className="card hover:shadow-md transition-all hover:border-blue-200 group"
          >
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-yellow-50 text-yellow-600 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
                <Clock className="w-6 h-6" />
              </div>
              <ChevronRight className="w-5 h-5 text-gray-300 group-hover:text-blue-600 transition-colors" />
            </div>
            <div className="text-3xl font-extrabold text-gray-900">
              {recentAppts.length.toLocaleString()}
            </div>
            <div className="text-gray-500 text-sm mt-1">
              Pending Appointments
            </div>
          </Link>
        </div>

        {/* Quick actions */}
        <div className="grid sm:grid-cols-2 gap-4 mb-10">
          {[
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
          ].map(({ label, desc, href, icon: Icon }) => (
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

        {/* Pending table */}
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
                    <th className="pb-3 pr-4">Date</th>
                    <th className="pb-3 pr-4">Status</th>
                    <th className="pb-3"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {recentAppts.slice(0, 8).map((a) => (
                    <tr
                      key={a.id}
                      className="hover:bg-gray-50 transition-colors"
                    >
                      <td className="py-3 pr-4 font-mono text-gray-400">
                        #{a.id}
                      </td>
                      <td className="py-3 pr-4 font-medium text-gray-900">
                        {a.patientName}
                      </td>
                      <td className="py-3 pr-4 text-gray-500">
                        {new Date(a.appointmentDate).toLocaleDateString(
                          "en-US",
                          { month: "short", day: "numeric", year: "numeric" },
                        )}
                      </td>
                      <td className="py-3 pr-4">
                        <StatusBadge status={a.approvalStatus} />
                      </td>
                      <td className="py-3">
                        <Link
                          to={`/appointments/${a.id}`}
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

  // ── PRIMARY ADMIN VIEW ─────────────────────────────────────────────────────

  // Prepare chart data
  const statusData = [
    { name: "Pending", value: stats?.pendingAppointments ?? 0 },
    { name: "Approved", value: stats?.approvedAppointments ?? 0 },
    { name: "Rejected", value: stats?.rejectedAppointments ?? 0 },
    { name: "Cancelled", value: stats?.cancelledAppointments ?? 0 },
  ];

  const centerChartData = Object.entries(stats?.appointmentsByCenter ?? {}).map(
    ([name, count]) => ({ name, count }),
  );

  const monthChartData = Object.entries(stats?.appointmentsByMonth ?? {}).map(
    ([month, count]) => ({ month, count }),
  );

  const testChartData = Object.entries(stats?.topTests ?? {}).map(
    ([name, count]) => ({ name, count }),
  );

  const headlineStats = [
    {
      label: "Total Appointments",
      value: stats?.totalAppointments ?? 0,
      icon: CalendarCheck,
      color: "bg-blue-50 text-blue-600",
      link: null,
    },
    {
      label: "Diagnostic Centers",
      value: stats?.totalCenters ?? 0,
      icon: Building2,
      color: "bg-indigo-50 text-indigo-600",
      link: "/admin/centers",
    },
    {
      label: "Diagnostic Tests",
      value: stats?.totalTests ?? 0,
      icon: Microscope,
      color: "bg-violet-50 text-violet-600",
      link: "/admin/tests",
    },
    {
      label: "Registered Patients",
      value: stats?.totalPatients ?? 0,
      icon: Users,
      color: "bg-green-50 text-green-600",
      link: null,
    },
    {
      label: "Center Admins",
      value: stats?.totalCenterAdmins ?? 0,
      icon: UserCog,
      color: "bg-orange-50 text-orange-600",
      link: "/admin/center-admins",
    },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Header */}
      <div className="mb-8 flex items-center gap-3">
        <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center">
          <LayoutDashboard className="w-5 h-5 text-white" />
        </div>
        <div>
          <h1 className="text-2xl font-extrabold text-gray-900">
            Admin Dashboard
          </h1>
          <p className="text-gray-500 text-sm">
            Platform-wide analytics and management
          </p>
        </div>
      </div>

      {/* Headline stats */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4 mb-10">
        {headlineStats.map(({ label, value, icon: Icon, color, link }) => {
          const inner = (
            <div className="card h-full">
              <div
                className={`w-10 h-10 ${color} rounded-xl flex items-center justify-center mb-3`}
              >
                <Icon className="w-5 h-5" />
              </div>
              <div className="text-2xl font-extrabold text-gray-900">
                {value.toLocaleString()}
              </div>
              <div className="text-gray-500 text-xs mt-1">{label}</div>
            </div>
          );
          return link ? (
            <Link
              key={label}
              to={link}
              className="hover:opacity-80 transition-opacity"
            >
              {inner}
            </Link>
          ) : (
            <div key={label}>{inner}</div>
          );
        })}
      </div>

      {/* Charts row 1: Monthly trend + Status breakdown */}
      <div className="grid lg:grid-cols-3 gap-6 mb-6">
        {/* Monthly appointments — line chart (2/3 width) */}
        <div className="card lg:col-span-2">
          <h2 className="font-bold text-gray-900 mb-5">
            Appointments by Month
          </h2>
          {monthChartData.length === 0 ? (
            <div className="flex items-center justify-center h-48 text-gray-300 text-sm">
              No data yet
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <LineChart
                data={monthChartData}
                margin={{ top: 5, right: 10, left: -20, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
                <XAxis
                  dataKey="month"
                  tick={{ fontSize: 11, fill: "#9ca3af" }}
                />
                <YAxis
                  tick={{ fontSize: 11, fill: "#9ca3af" }}
                  allowDecimals={false}
                />
                <Tooltip
                  contentStyle={{
                    borderRadius: "12px",
                    border: "1px solid #e5e7eb",
                    fontSize: 12,
                  }}
                  formatter={(v) => [v, "Appointments"]}
                />
                <Line
                  type="monotone"
                  dataKey="count"
                  stroke={LINE_COLOR}
                  strokeWidth={2.5}
                  dot={{ r: 4, fill: LINE_COLOR }}
                  activeDot={{ r: 6 }}
                />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Status breakdown — donut (1/3 width) */}
        <div className="card">
          <h2 className="font-bold text-gray-900 mb-5">Status Breakdown</h2>
          {stats?.totalAppointments === 0 ? (
            <div className="flex items-center justify-center h-48 text-gray-300 text-sm">
              No data yet
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie
                  data={statusData}
                  cx="50%"
                  cy="45%"
                  innerRadius={55}
                  outerRadius={80}
                  paddingAngle={3}
                  dataKey="value"
                >
                  {statusData.map((_, i) => (
                    <Cell key={i} fill={STATUS_COLORS[i]} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    borderRadius: "12px",
                    border: "1px solid #e5e7eb",
                    fontSize: 12,
                  }}
                />
                <Legend
                  iconType="circle"
                  iconSize={8}
                  wrapperStyle={{ fontSize: 12 }}
                />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* Charts row 2: By center + Top tests */}
      <div className="grid lg:grid-cols-2 gap-6">
        {/* Appointments per center — horizontal bar */}
        <div className="card">
          <h2 className="font-bold text-gray-900 mb-5">
            Appointments per Center
          </h2>
          {centerChartData.length === 0 ? (
            <div className="flex items-center justify-center h-48 text-gray-300 text-sm">
              No data yet
            </div>
          ) : (
            <ResponsiveContainer
              width="100%"
              height={Math.max(180, centerChartData.length * 44)}
            >
              <BarChart
                data={centerChartData}
                layout="vertical"
                margin={{ top: 0, right: 20, left: 10, bottom: 0 }}
              >
                <XAxis
                  type="number"
                  tick={{ fontSize: 11, fill: "#9ca3af" }}
                  allowDecimals={false}
                />
                <YAxis
                  type="category"
                  dataKey="name"
                  width={140}
                  tick={{ fontSize: 11, fill: "#374151" }}
                />
                <Tooltip
                  contentStyle={{
                    borderRadius: "12px",
                    border: "1px solid #e5e7eb",
                    fontSize: 12,
                  }}
                  formatter={(v) => [v, "Appointments"]}
                />
                <Bar dataKey="count" fill={BAR_COLOR} radius={[0, 6, 6, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Top 5 tests — vertical bar */}
        <div className="card">
          <h2 className="font-bold text-gray-900 mb-5">Most Booked Tests</h2>
          {testChartData.length === 0 ? (
            <div className="flex items-center justify-center h-48 text-gray-300 text-sm">
              No data yet
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart
                data={testChartData}
                margin={{ top: 5, right: 10, left: -20, bottom: 40 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
                <XAxis
                  dataKey="name"
                  tick={{ fontSize: 10, fill: "#374151" }}
                  angle={-30}
                  textAnchor="end"
                  interval={0}
                />
                <YAxis
                  tick={{ fontSize: 11, fill: "#9ca3af" }}
                  allowDecimals={false}
                />
                <Tooltip
                  contentStyle={{
                    borderRadius: "12px",
                    border: "1px solid #e5e7eb",
                    fontSize: 12,
                  }}
                  formatter={(v) => [v, "Bookings"]}
                />
                <Bar dataKey="count" fill="#6366f1" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    </div>
  );
}
