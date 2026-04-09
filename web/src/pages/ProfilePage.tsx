import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  User,
  Edit2,
  Check,
  X,
  Loader2,
  FileText,
  TestTube,
} from "lucide-react";
import { patientApi } from "../api/patients";
import { useAuth } from "../context/AuthContext";
import type {
  PatientProfileResponse,
  PatientProfileRequest,
  TestResultResponse,
} from "../types";

export default function ProfilePage() {
  const { username } = useParams<{ username: string }>();
  const { user } = useAuth();
  const [profile, setProfile] = useState<PatientProfileResponse | null>(null);
  const [results, setResults] = useState<TestResultResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [resultsLoading, setResultsLoading] = useState(false);
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
          // Load test results
          setResultsLoading(true);
          patientApi
            .getResults(targetUsername)
            .then((r2) => setResults(r2.data.data ?? []))
            .finally(() => setResultsLoading(false));
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

      {/* Test Results */}
      {profile && (
        <div className="card">
          <h2 className="font-bold text-gray-900 mb-4 flex items-center gap-2">
            <TestTube className="w-5 h-5 text-blue-600" /> Test Results
          </h2>
          {resultsLoading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="w-6 h-6 animate-spin text-blue-600" />
            </div>
          ) : results.length === 0 ? (
            <div className="text-center py-10">
              <FileText className="w-12 h-12 text-gray-200 mx-auto mb-3" />
              <p className="text-gray-400 text-sm">No test results yet.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {results.map((result) => (
                <div key={result.id} className="p-4 bg-gray-50 rounded-xl">
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-xs text-gray-400 font-medium uppercase tracking-wide">
                      Appointment #{result.appointmentId}
                    </p>
                  </div>
                  <div className="grid sm:grid-cols-2 gap-3">
                    <div>
                      <p className="text-xs text-gray-400">Reading</p>
                      <p className="font-medium text-gray-800 text-sm">
                        {result.testReading}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-400">Medical Condition</p>
                      <p className="font-medium text-gray-800 text-sm">
                        {result.medicalCondition}
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
