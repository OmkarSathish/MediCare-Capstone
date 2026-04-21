import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { useTitle } from "../hooks/useTitle";
import {
  ChevronLeft,
  ChevronRight,
  CheckCircle2,
  Loader2,
  Plus,
} from "lucide-react";
import { appointmentApi } from "../api/appointments";
import { useAuth } from "../context/AuthContext";
import type { AppointmentResponse, ApprovalStatus } from "../types";

const DAYS_OF_WEEK = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

const STATUS_EVENT: Record<
  ApprovalStatus,
  { bg: string; text: string; dot: string }
> = {
  APPROVED: {
    bg: "bg-green-100",
    text: "text-green-800",
    dot: "bg-green-500",
  },
  PENDING: {
    bg: "bg-amber-100",
    text: "text-amber-800",
    dot: "bg-amber-400",
  },
  REJECTED: { bg: "bg-red-100", text: "text-red-800", dot: "bg-red-500" },
  CANCELLED: {
    bg: "bg-gray-100",
    text: "text-gray-500",
    dot: "bg-gray-400",
  },
};

function toYMD(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

export default function AppointmentsPage() {
  useTitle("My Appointments");
  const { isAdmin } = useAuth();
  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [hoveredDate, setHoveredDate] = useState<string | null>(null);
  const hideTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const showTooltip = (date: string) => {
    if (hideTimer.current) clearTimeout(hideTimer.current);
    setHoveredDate(date);
  };

  const hideTooltip = () => {
    hideTimer.current = setTimeout(() => setHoveredDate(null), 1200);
  };
  const [currentMonth, setCurrentMonth] = useState(() => {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  });

  useEffect(() => {
    appointmentApi
      .list()
      .then((r) => setAppointments(r.data.data ?? []))
      .finally(() => setLoading(false));
  }, []);

  // Group appointments by YYYY-MM-DD
  const byDate: Record<string, AppointmentResponse[]> = {};
  for (const appt of appointments) {
    const key = appt.appointmentDate.split("T")[0];
    if (!byDate[key]) byDate[key] = [];
    byDate[key].push(appt);
  }

  const year = currentMonth.getFullYear();
  const month = currentMonth.getMonth();
  const firstDayOfWeek = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  // Build grid cells: null = padding blank
  const cells: (number | null)[] = [
    ...Array<null>(firstDayOfWeek).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ];
  while (cells.length % 7 !== 0) cells.push(null);

  const todayStr = toYMD(new Date());
  const monthLabel = currentMonth.toLocaleDateString("en-US", {
    month: "long",
    year: "numeric",
  });

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Header */}
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

      {loading ? (
        <div className="flex items-center justify-center py-20">
          <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
        </div>
      ) : (
        <>
          {/* ── Calendar ── */}
          <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
            {/* Toolbar */}
            <div className="flex items-center gap-3 px-5 py-4 border-b border-gray-200">
              <button
                onClick={() =>
                  setCurrentMonth(
                    new Date(
                      new Date().getFullYear(),
                      new Date().getMonth(),
                      1,
                    ),
                  )
                }
                className="px-3 py-1.5 text-sm font-medium text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
              >
                Today
              </button>
              <div className="flex items-center gap-1">
                <button
                  onClick={() => setCurrentMonth(new Date(year, month - 1, 1))}
                  className="p-1.5 rounded-lg hover:bg-gray-100 transition-colors text-gray-500"
                >
                  <ChevronLeft className="w-4 h-4" />
                </button>
                <button
                  onClick={() => setCurrentMonth(new Date(year, month + 1, 1))}
                  className="p-1.5 rounded-lg hover:bg-gray-100 transition-colors text-gray-500"
                >
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
              <h2 className="text-lg font-bold text-gray-900">{monthLabel}</h2>
            </div>

            {/* Day-of-week headers */}
            <div className="grid grid-cols-7 border-b border-gray-200">
              {DAYS_OF_WEEK.map((d) => (
                <div
                  key={d}
                  className="py-2 text-center text-xs font-semibold uppercase tracking-wide text-gray-400 border-r border-gray-100 last:border-r-0"
                >
                  {d}
                </div>
              ))}
            </div>

            {/* Calendar grid */}
            <div className="grid grid-cols-7">
              {cells.map((day, idx) => {
                if (day === null) {
                  return (
                    <div
                      key={`blank-${idx}`}
                      className="min-h-28 bg-gray-50/60 border-r border-b border-gray-100 last:border-r-0"
                    />
                  );
                }

                const m = String(month + 1).padStart(2, "0");
                const d = String(day).padStart(2, "0");
                const dateStr = `${year}-${m}-${d}`;
                const dayAppts = byDate[dateStr] ?? [];
                const hasAppts = dayAppts.length > 0;
                const isToday = dateStr === todayStr;
                const isPastDate = dateStr < todayStr;
                const isHovered = hoveredDate === dateStr;
                const isLastCol = (idx + 1) % 7 === 0;
                const isLastRow = idx >= cells.length - 7;

                return (
                  <div
                    key={dateStr}
                    className={`relative min-h-28 p-1.5 flex flex-col transition-colors
                      ${isLastCol ? "" : "border-r border-gray-100"}
                      ${isLastRow ? "" : "border-b border-gray-100"}
                      ${isToday ? "bg-blue-50/70" : isPastDate ? "bg-gray-50/40" : "bg-white"}
                      ${isHovered ? (isToday ? "bg-blue-50" : isPastDate ? "bg-gray-100/50" : "bg-slate-50") : ""}
                    `}
                    onMouseEnter={() => showTooltip(dateStr)}
                    onMouseLeave={hideTooltip}
                  >
                    {/* Day number */}
                    <div className="mb-1">
                      <span
                        className={`inline-flex w-7 h-7 items-center justify-center rounded-full text-sm font-semibold
                          ${isToday ? "bg-blue-600 text-white" : isPastDate ? "text-gray-400" : "text-gray-700 hover:bg-gray-100"}`}
                      >
                        {day}
                      </span>
                    </div>

                    {/* Event pills — always visible */}
                    <div className="flex flex-col gap-0.5">
                      {dayAppts.map((appt) => {
                        const s = STATUS_EVENT[appt.approvalStatus];
                        return (
                          <Link
                            key={appt.id}
                            to={`/appointments/${appt.id}`}
                            className={`flex items-center gap-1 px-1.5 py-0.5 rounded text-[11px] font-medium truncate ${s.bg} ${s.text} hover:opacity-80 transition-opacity`}
                          >
                            <span
                              className={`w-1.5 h-1.5 rounded-full shrink-0 ${s.dot}`}
                            />
                            <span className="truncate">{appt.centerName}</span>
                          </Link>
                        );
                      })}
                    </div>

                    {/* Book button on hover — empty bookable dates */}
                    {!hasAppts &&
                      isHovered &&
                      !isAdmin &&
                      dateStr >= todayStr && (
                        <div className="mt-auto pt-1">
                          <Link
                            to={`/book?date=${dateStr}`}
                            className="flex items-center justify-center gap-0.5 w-full text-[10px] font-semibold text-blue-600 hover:bg-blue-100 rounded py-0.5 transition-colors"
                          >
                            <Plus className="w-3 h-3" /> Book
                          </Link>
                        </div>
                      )}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Upcoming Appointments — approved only */}
          {(() => {
            const upcoming = appointments
              .filter(
                (a) =>
                  a.approvalStatus === "APPROVED" &&
                  a.appointmentDate.split("T")[0] >= todayStr,
              )
              .sort((a, b) =>
                a.appointmentDate.localeCompare(b.appointmentDate),
              );
            if (upcoming.length === 0) return null;
            return (
              <div className="mt-10">
                <h2 className="text-xl font-bold text-gray-900 mb-6">
                  Upcoming Appointments
                </h2>
                <div className="relative">
                  {/* Vertical line */}
                  <div className="absolute left-5 top-0 bottom-0 w-0.5 bg-blue-100" />
                  <div className="space-y-6">
                    {upcoming.map((appt, i) => {
                      const date = new Date(appt.appointmentDate);
                      const isToday =
                        appt.appointmentDate.split("T")[0] === todayStr;
                      return (
                        <div
                          key={appt.id}
                          className="relative flex items-start gap-5 pl-14"
                        >
                          {/* Timeline dot */}
                          <div
                            className={`absolute left-3.5 top-8 w-3 h-3 rounded-full border-2 border-white ring-2 ${
                              i === 0
                                ? "bg-blue-600 ring-blue-600"
                                : "bg-green-500 ring-green-400"
                            }`}
                          />
                          <Link
                            to={`/appointments/${appt.id}`}
                            className="flex-1 card p-4 hover:shadow-md hover:border-blue-200 transition-all group overflow-hidden"
                          >
                            {/* Today ribbon */}
                            {isToday && (
                              <div className="absolute top-3 right-[-28px] rotate-45 bg-blue-600 text-white text-[10px] font-bold px-8 py-0.5 shadow-sm">
                                Today
                              </div>
                            )}
                            <div className="flex items-start justify-between gap-3">
                              <div>
                                <p className="font-bold text-gray-900 group-hover:text-blue-600 transition-colors">
                                  {appt.centerName}
                                </p>
                                <p className="text-sm text-gray-500 mt-0.5">
                                  {date.toLocaleDateString("en-US", {
                                    weekday: "long",
                                    year: "numeric",
                                    month: "long",
                                    day: "numeric",
                                  })}
                                </p>
                                {appt.specialRequests && (
                                  <p className="text-xs text-amber-600 mt-1 italic">
                                    📋 {appt.specialRequests}
                                  </p>
                                )}
                              </div>
                              <span className="inline-flex items-center gap-1 text-xs font-semibold text-green-700 bg-green-50 border border-green-200 px-2 py-1 rounded-lg shrink-0">
                                <CheckCircle2 className="w-3.5 h-3.5" />{" "}
                                Approved
                              </span>
                            </div>
                          </Link>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            );
          })()}
        </>
      )}
    </div>
  );
}
