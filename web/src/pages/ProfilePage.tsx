import { useEffect, useState, Fragment, useRef } from "react";
import { useParams, Link } from "react-router-dom";
import { User, Edit2, Check, X, Loader2, CalendarClock } from "lucide-react";
import { patientApi } from "../api/patients";
import { appointmentApi } from "../api/appointments";
import { useAuth } from "../context/AuthContext";
import type {
  PatientProfileResponse,
  PatientProfileRequest,
  AppointmentResponse,
} from "../types";

/* ── Horizontal timeline strip ─────────────────────────────────────── */
const COL_W = 180; // px per date column
const AXIS_Y = 140;

function TimelineStrip({
  appointments,
}: {
  appointments: AppointmentResponse[];
}) {
  const scrollRef = useRef<HTMLDivElement>(null);

  // Group by date string
  const grouped = new Map<string, AppointmentResponse[]>();
  for (const a of appointments) {
    const key = a.appointmentDate.slice(0, 10); // YYYY-MM-DD
    if (!grouped.has(key)) grouped.set(key, []);
    grouped.get(key)!.push(a);
  }
  const dateKeys = [...grouped.keys()].sort();

  // find column index closest to today for auto-scroll
  const todayStr = new Date().toISOString().slice(0, 10);
  let todayColIdx = dateKeys.findIndex((d) => d >= todayStr);
  if (todayColIdx === -1) todayColIdx = dateKeys.length - 1;

  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    const target = todayColIdx * COL_W - el.clientWidth / 2 + COL_W / 2;
    el.scrollLeft = Math.max(0, target);
  }, [appointments.length]);

  const totalW = dateKeys.length * COL_W;

  // "today" marker position
  const todayExact = dateKeys.indexOf(todayStr);

  return (
    <div
      ref={scrollRef}
      className="overflow-x-auto -mx-6 px-6"
      style={{ scrollBehavior: "smooth" }}
    >
      <div className="relative" style={{ width: totalW, minHeight: 280 }}>
        {/* Horizontal axis */}
        <div
          className="absolute left-0 h-px bg-gray-200"
          style={{ top: AXIS_Y, width: totalW }}
        />

        {/* Today marker */}
        {todayExact >= 0 && (
          <div
            className="absolute flex flex-col items-center"
            style={{ left: todayExact * COL_W + COL_W / 2, top: 0 }}
          >
            <span className="text-[10px] font-semibold text-blue-600 bg-blue-50 border border-blue-200 rounded-full px-2 py-0.5 -translate-x-1/2 mb-1">
              Today
            </span>
            <div
              className="w-px bg-blue-300 -translate-x-1/2"
              style={{ height: AXIS_Y - 20 }}
            />
          </div>
        )}

        {dateKeys.map((dateKey, colIdx) => {
          const colAppts = grouped.get(dateKey)!;
          const centerX = colIdx * COL_W + COL_W / 2;
          const isPast = dateKey < todayStr;

          return (
            <Fragment key={dateKey}>
              {/* Axis tick */}
              <div
                className="absolute w-px h-2.5 bg-gray-300"
                style={{ left: centerX, top: AXIS_Y }}
              />

              {/* Date label */}
              <p
                className={`absolute text-xs whitespace-nowrap ${isPast ? "text-gray-300" : "text-gray-500"}`}
                style={{
                  left: centerX,
                  top: AXIS_Y + 14,
                  transform: "translateX(-50%)",
                }}
              >
                {new Date(dateKey + "T00:00").toLocaleDateString(undefined, {
                  month: "short",
                  day: "numeric",
                })}
              </p>

              {/* Appointment blocks — stacked */}
              {colAppts.map((appt, stackIdx) => {
                const BLOCK_H = 52;
                const STACK_GAP = 6;
                const blockTop = 8 + stackIdx * (BLOCK_H + STACK_GAP);
                const lineStart = blockTop + BLOCK_H;
                const blockBg =
                  appt.approvalStatus === "APPROVED"
                    ? "bg-gray-900 text-white"
                    : appt.approvalStatus === "REJECTED"
                      ? "bg-red-500 text-white"
                      : "bg-amber-400 text-gray-900";
                const statusDot =
                  appt.approvalStatus === "APPROVED"
                    ? "text-green-400"
                    : appt.approvalStatus === "REJECTED"
                      ? "text-red-200"
                      : "text-amber-800";

                return (
                  <Fragment key={appt.id}>
                    {/* Block */}
                    <Link
                      to={`/appointments/${appt.id}`}
                      className={`absolute rounded-xl px-3 py-2.5 shadow-sm hover:opacity-90 transition-opacity ${blockBg} ${isPast ? "opacity-60" : ""}`}
                      style={{
                        left: centerX,
                        top: blockTop,
                        width: 148,
                        height: BLOCK_H,
                        transform: "translateX(-50%)",
                      }}
                    >
                      <p className="text-xs font-semibold truncate leading-tight">
                        {appt.centerName}
                      </p>
                      <p
                        className={`text-xs mt-1.5 flex items-center gap-1 ${statusDot}`}
                      >
                        <span className="w-1.5 h-1.5 rounded-full bg-current inline-block" />
                        {appt.approvalStatus}
                      </p>
                    </Link>

                    {/* Dashed connector */}
                    {lineStart < AXIS_Y && (
                      <div
                        className="absolute border-l border-dashed border-gray-300"
                        style={{
                          left: centerX,
                          top: lineStart,
                          height: AXIS_Y - lineStart,
                        }}
                      />
                    )}
                  </Fragment>
                );
              })}

              {/* Detail snippet below axis (first appointment of this date) */}
              <div
                className="absolute"
                style={{
                  left: centerX,
                  top: AXIS_Y + 34,
                  width: 148,
                  transform: "translateX(-50%)",
                }}
              >
                {colAppts.map((appt) => (
                  <p
                    key={appt.id}
                    className={`text-xs ${isPast ? "text-gray-300" : "text-gray-500"} truncate`}
                  >
                    #{appt.id} · {appt.centerName}
                  </p>
                ))}
                <Link
                  to={`/appointments/${colAppts[0].id}`}
                  className="text-xs text-blue-600 hover:underline"
                >
                  View details →
                </Link>
              </div>
            </Fragment>
          );
        })}
      </div>
    </div>
  );
}

export default function ProfilePage() {
  const { username } = useParams<{ username: string }>();
  const { user } = useAuth();
  const [profile, setProfile] = useState<PatientProfileResponse | null>(null);
  const [allAppts, setAllAppts] = useState<AppointmentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [apptLoading, setApptLoading] = useState(false);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [form, setForm] = useState<PatientProfileRequest>({
    name: "",
    phoneNo: "",
    age: 0,
    gender: "",
  });
  const [createMode, setCreateMode] = useState(false);

  const targetUsername = username ?? user?.email ?? "";

  useEffect(() => {
    if (!targetUsername) {
      setLoading(false);
      return;
    }
    patientApi
      .getProfile(targetUsername)
      .then((r) => {
        const p = r.data.data;
        if (p) {
          setProfile(p);
          setForm({
            name: p.name,
            phoneNo: p.phoneNo ?? "",
            age: p.age,
            gender: p.gender ?? "",
          });
          // Load all appointments for timeline
          setApptLoading(true);
          appointmentApi
            .list()
            .then((r2) => {
              const filtered = (r2.data.data ?? []).filter(
                (a) => a.approvalStatus !== "CANCELLED",
              );
              filtered.sort(
                (a, b) =>
                  new Date(a.appointmentDate).getTime() -
                  new Date(b.appointmentDate).getTime(),
              );
              setAllAppts(filtered);
            })
            .finally(() => setApptLoading(false));
        }
      })
      .catch(() => {
        setCreateMode(true);
        setForm((f) => ({
          ...f,
          name: user?.fullName ?? "",
          phoneNo: user?.phone ?? "",
        }));
      })
      .finally(() => setLoading(false));
  }, [targetUsername]);

  const handleSave = async () => {
    setSaving(true);
    setError("");
    try {
      if (createMode) {
        const r = await patientApi.createProfile(form);
        setProfile(r.data.data ?? null);
        setCreateMode(false);
      } else {
        const r = await patientApi.updateProfile(targetUsername, form);
        setProfile(r.data.data ?? null);
      }
      setEditing(false);
    } catch (err: any) {
      setError(err?.response?.data?.message ?? "Failed to save profile.");
    } finally {
      setSaving(false);
    }
  };

  if (loading)
    return (
      <div className="flex items-center justify-center min-h-96">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );

  const isOwner = user?.email === targetUsername;
  const showForm = editing || createMode;

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10">
      <h1 className="text-3xl font-extrabold text-gray-900 mb-8">
        Patient Profile
      </h1>

      {/* Profile Card */}
      <div className="card mb-6">
        {/* Header */}
        <div className="flex items-start justify-between mb-6">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-2xl flex items-center justify-center text-white text-2xl font-bold">
              {profile?.name?.charAt(0)?.toUpperCase() ??
                user?.fullName?.charAt(0)?.toUpperCase() ??
                "?"}
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-900">
                {profile?.name ?? user?.fullName}
              </h2>
              <p className="text-gray-500 text-sm">{user?.email}</p>
            </div>
          </div>
          {isOwner && !createMode && !editing && profile && (
            <button
              onClick={() => setEditing(true)}
              className="flex items-center gap-2 text-sm font-medium text-blue-600 border border-blue-200 rounded-xl px-4 py-2 hover:bg-blue-50 transition-colors"
            >
              <Edit2 className="w-4 h-4" /> Edit
            </button>
          )}
        </div>

        {!showForm && profile ? (
          <div className="grid sm:grid-cols-2 gap-4">
            {[
              { label: "Full Name", value: profile.name },
              { label: "Age", value: `${profile.age} years` },
              { label: "Gender", value: profile.gender || "Not specified" },
              { label: "Phone", value: profile.phoneNo || "Not provided" },
            ].map(({ label, value }) => (
              <div key={label} className="p-4 bg-gray-50 rounded-xl">
                <p className="text-xs text-gray-400 mb-1">{label}</p>
                <p className="font-semibold text-gray-900">{value}</p>
              </div>
            ))}
          </div>
        ) : showForm && isOwner ? (
          <div className="space-y-4">
            {createMode && (
              <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 text-sm text-blue-700 flex items-start gap-2">
                <User className="w-4 h-4 mt-0.5 shrink-0" />
                Create your patient profile to start booking appointments.
              </div>
            )}

            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm">
                {error}
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Full Name
              </label>
              <input
                type="text"
                className="input-field"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                required
              />
            </div>
            <div className="grid sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Age
                </label>
                <input
                  type="number"
                  min={0}
                  max={150}
                  className="input-field"
                  value={form.age}
                  onChange={(e) =>
                    setForm({ ...form, age: Number(e.target.value) })
                  }
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Gender
                </label>
                <select
                  className="input-field"
                  value={form.gender}
                  onChange={(e) => setForm({ ...form, gender: e.target.value })}
                >
                  <option value="">Select gender</option>
                  <option value="Male">Male</option>
                  <option value="Female">Female</option>
                  <option value="Other">Other</option>
                  <option value="Prefer not to say">Prefer not to say</option>
                </select>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Phone Number
              </label>
              <input
                type="tel"
                className="input-field"
                value={form.phoneNo}
                onChange={(e) => setForm({ ...form, phoneNo: e.target.value })}
              />
            </div>

            <div className="flex gap-3 pt-2">
              <button
                onClick={handleSave}
                disabled={saving}
                className="btn-primary flex items-center gap-2 disabled:opacity-60"
              >
                {saving ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  <Check className="w-4 h-4" />
                )}
                {createMode ? "Create Profile" : "Save Changes"}
              </button>
              {!createMode && (
                <button
                  onClick={() => setEditing(false)}
                  className="btn-outline flex items-center gap-2"
                >
                  <X className="w-4 h-4" /> Cancel
                </button>
              )}
            </div>
          </div>
        ) : null}
      </div>

      {/* Appointments Timeline */}
      {profile && isOwner && (
        <div className="card overflow-hidden">
          {/* Header */}
          <div className="flex items-start justify-between mb-5">
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-widest font-medium mb-0.5">
                Journey highlights
              </p>
              <h2 className="font-bold text-gray-900 text-lg flex items-center gap-2">
                <CalendarClock className="w-5 h-5 text-blue-600" /> Timeline
              </h2>
            </div>
            <div className="flex items-center gap-3 text-xs text-gray-500">
              <span className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-green-500 inline-block" />
                Approved
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-amber-400 inline-block" />
                Pending
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-red-500 inline-block" />
                Rejected
              </span>
            </div>
          </div>

          {apptLoading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="w-6 h-6 animate-spin text-blue-600" />
            </div>
          ) : allAppts.length === 0 ? (
            <div className="text-center py-10">
              <CalendarClock className="w-12 h-12 text-gray-200 mx-auto mb-3" />
              <p className="text-gray-400 text-sm">No appointments yet.</p>
            </div>
          ) : (
            <TimelineStrip appointments={allAppts} />
          )}
        </div>
      )}
    </div>
  );
}
