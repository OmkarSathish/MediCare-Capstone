import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Microscope,
  Clock,
  ChevronRight,
  Loader2,
  LayoutDashboard,
  UserCog,
  Users,
  CalendarCheck,
  Building2,
  CheckCircle,
  XCircle,
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
import { useAuth } from "../context/AuthContext";
import type {
  AdminDashboardResponse,
  CenterAdminDashboardResponse,
} from "../types";

const STATUS_COLORS = ["#f59e0b", "#22c55e", "#ef4444", "#6b7280"];
const BAR_COLOR = "#3b82f6";
const LINE_COLOR = "#6366f1";

export default function AdminDashboardPage() {
  const { isCenterAdmin, isStaffAdmin, adminCenterId } = useAuth();
  const [stats, setStats] = useState<AdminDashboardResponse | null>(null);
  const [centerStats, setCenterStats] =
    useState<CenterAdminDashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isCenterAdmin || isStaffAdmin) {
      adminApi
        .getCenterDashboard()
        .then((r) => setCenterStats(r.data.data ?? null))
        .catch(() => setError("Failed to load dashboard data."))
        .finally(() => setLoading(false));
    } else {
      adminApi
        .getDashboard()
        .then((r) => setStats(r.data.data ?? null))
        .catch(() => setError("Failed to load dashboard data."))
        .finally(() => setLoading(false));
    }
  }, [isCenterAdmin, isStaffAdmin, adminCenterId]);

  if (loading)
    return (
      <div className="flex items-center justify-center min-h-96">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );

  if (error)
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="rounded-xl bg-red-50 border border-red-200 text-red-700 px-6 py-4 text-sm">
          {error}
        </div>
      </div>
    );

  // ── CENTER ADMIN / STAFF VIEW ─────────────────────────────────────────────
  if (isCenterAdmin || isStaffAdmin) {
    const cs = centerStats;

    const centerStatusData = [
      { name: "Pending", value: cs?.pendingAppointments ?? 0 },
      { name: "Approved", value: cs?.approvedAppointments ?? 0 },
      { name: "Rejected", value: cs?.rejectedAppointments ?? 0 },
      { name: "Cancelled", value: cs?.cancelledAppointments ?? 0 },
    ];

    const centerMonthData = Object.entries(cs?.appointmentsByMonth ?? {}).map(
      ([month, count]) => ({ month, count }),
    );

    const centerTestData = Object.entries(cs?.topTests ?? {}).map(
      ([name, count]) => ({ name, count }),
    );

    const centerHeadline = [
      {
        label: "Total Appointments",
        value: cs?.totalAppointments ?? 0,
        icon: CalendarCheck,
        color: "bg-blue-50 text-blue-600",
      },
      {
        label: "Pending",
        value: cs?.pendingAppointments ?? 0,
        icon: Clock,
        color: "bg-yellow-50 text-yellow-600",
      },
      {
        label: "Approved",
        value: cs?.approvedAppointments ?? 0,
        icon: CheckCircle,
        color: "bg-green-50 text-green-600",
      },
      {
        label: "Rejected",
        value: cs?.rejectedAppointments ?? 0,
        icon: XCircle,
        color: "bg-red-50 text-red-600",
      },
      {
        label: "Assigned Tests",
        value: cs?.assignedTests ?? 0,
        icon: Microscope,
        color: "bg-violet-50 text-violet-600",
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
              {cs?.centerName ?? "Center Admin Dashboard"}
            </h1>
            <p className="text-gray-500 text-sm">
              Center analytics and management
            </p>
          </div>
        </div>

        {/* Headline stats */}
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4 mb-10">
          {centerHeadline.map(({ label, value, icon: Icon, color }) => (
            <div key={label} className="card">
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
          ))}
        </div>

        {/* Charts row: Monthly trend + Status donut */}
        <div className="grid lg:grid-cols-3 gap-6 mb-6">
          {/* Monthly line chart */}
          <div className="card lg:col-span-2">
            <h2 className="font-bold text-gray-900 mb-5">
              Appointments by Month
            </h2>
            {centerMonthData.length === 0 ? (
              <div className="flex items-center justify-center h-48 text-gray-300 text-sm">
                No data yet
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <LineChart
                  data={centerMonthData}
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

          {/* Status donut */}
          <div className="card">
            <h2 className="font-bold text-gray-900 mb-5">Status Breakdown</h2>
            {(cs?.totalAppointments ?? 0) === 0 ? (
              <div className="flex items-center justify-center h-48 text-gray-300 text-sm">
                No data yet
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <PieChart>
                  <Pie
                    data={centerStatusData}
                    cx="50%"
                    cy="45%"
                    innerRadius={55}
                    outerRadius={80}
                    paddingAngle={3}
                    dataKey="value"
                  >
                    {centerStatusData.map((_, i) => (
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

        {/* Top tests bar chart + quick actions */}
        <div className="grid lg:grid-cols-2 gap-6">
          <div className="card">
            <h2 className="font-bold text-gray-900 mb-5">
              Most Booked Tests at Your Center
            </h2>
            {centerTestData.length === 0 ? (
              <div className="flex items-center justify-center h-48 text-gray-300 text-sm">
                No data yet
              </div>
            ) : (
              (() => {
                const max = Math.max(
                  ...centerTestData.map((d) => Number(d.count)),
                );
                return (
                  <div className="space-y-3 max-h-80 overflow-y-auto pr-1">
                    {centerTestData.map((d, i) => (
                      <div
                        key={d.name}
                        className="flex items-center gap-3 min-w-0"
                      >
                        {/* Rank badge */}
                        <span
                          className={`shrink-0 w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold
                          ${i === 0 ? "bg-blue-600 text-white" : i === 1 ? "bg-blue-100 text-blue-700" : i === 2 ? "bg-gray-100 text-gray-600" : "bg-gray-50 text-gray-400"}`}
                        >
                          {i + 1}
                        </span>
                        {/* Name */}
                        <span
                          className="w-36 shrink-0 text-xs text-gray-700 font-medium truncate"
                          title={d.name}
                        >
                          {d.name}
                        </span>
                        {/* Progress bar */}
                        <div className="flex-1 bg-gray-100 rounded-full h-2 min-w-0">
                          <div
                            className="h-2 rounded-full bg-blue-500 transition-all"
                            style={{
                              width: `${(Number(d.count) / max) * 100}%`,
                            }}
                          />
                        </div>
                        {/* Count */}
                        <span className="shrink-0 text-xs font-semibold text-gray-700 w-6 text-right">
                          {d.count}
                        </span>
                      </div>
                    ))}
                  </div>
                );
              })()
            )}
          </div>

          {/* Quick actions */}
          <div className="card flex flex-col justify-center gap-3">
            <h2 className="font-bold text-gray-900 mb-2">Quick Actions</h2>
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
                className="flex items-center gap-4 p-3 rounded-xl border border-gray-100 hover:border-blue-200 hover:shadow-sm transition-all group"
              >
                <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center shrink-0 group-hover:bg-blue-600 transition-colors">
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
        {/* Appointments per center — ranked list */}
        <div className="card">
          <h2 className="font-bold text-gray-900 mb-5">
            Appointments per Center
          </h2>
          {centerChartData.length === 0 ? (
            <div className="flex items-center justify-center h-48 text-gray-300 text-sm">
              No data yet
            </div>
          ) : (
            (() => {
              const max = Math.max(
                ...centerChartData.map((d) => d.count as number),
              );
              return (
                <div className="space-y-3 max-h-80 overflow-y-auto pr-1">
                  {centerChartData.map((d, i) => (
                    <div
                      key={d.name}
                      className="flex items-center gap-3 min-w-0"
                    >
                      {/* Rank badge */}
                      <span
                        className={`shrink-0 w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold
                      ${i === 0 ? "bg-blue-600 text-white" : i === 1 ? "bg-blue-100 text-blue-700" : i === 2 ? "bg-gray-100 text-gray-600" : "bg-gray-50 text-gray-400"}`}
                      >
                        {i + 1}
                      </span>
                      {/* Name */}
                      <span
                        className="w-36 shrink-0 text-xs text-gray-700 font-medium truncate"
                        title={d.name}
                      >
                        {d.name}
                      </span>
                      {/* Progress bar */}
                      <div className="flex-1 bg-gray-100 rounded-full h-2 min-w-0">
                        <div
                          className="h-2 rounded-full bg-blue-500 transition-all"
                          style={{
                            width: `${((d.count as number) / max) * 100}%`,
                          }}
                        />
                      </div>
                      {/* Count */}
                      <span className="shrink-0 text-xs font-semibold text-gray-700 w-6 text-right">
                        {d.count}
                      </span>
                    </div>
                  ))}
                </div>
              );
            })()
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
