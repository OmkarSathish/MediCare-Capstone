import { useEffect, useState } from "react";
import { UserCog, Plus, Trash2, Loader2, X, Eye, EyeOff } from "lucide-react";
import { useTitle } from "../hooks/useTitle";
import { authApi } from "../api/auth";
import type { UserProfileResponse } from "../types";

interface CreateForm {
  fullName: string;
  email: string;
  password: string;
}

const emptyForm: CreateForm = { fullName: "", email: "", password: "" };

export default function AdminStaffPage() {
  useTitle("Manage Staff");
  const [staff, setStaff] = useState<UserProfileResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState<CreateForm>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [removingId, setRemovingId] = useState<number | null>(null);
  const [confirmRemoveId, setConfirmRemoveId] = useState<number | null>(null);

  const fetchStaff = async () => {
    setLoading(true);
    try {
      const res = await authApi.listCenterStaff();
      setStaff(res.data.data ?? []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStaff();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      await authApi.createCenterStaff({
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
      });
      setModalOpen(false);
      setForm(emptyForm);
      await fetchStaff();
    } catch (err: any) {
      setError(
        err?.response?.data?.message ??
          err?.response?.data?.fieldErrors?.join(", ") ??
          "Failed to create staff admin.",
      );
    } finally {
      setSaving(false);
    }
  };

  const handleRemove = async (userId: number) => {
    setRemovingId(userId);
    try {
      await authApi.removeCenterStaff(userId);
      setConfirmRemoveId(null);
      await fetchStaff();
    } catch (err: any) {
      alert(err?.response?.data?.message ?? "Failed to remove staff admin.");
    } finally {
      setRemovingId(null);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center">
            <UserCog className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-2xl font-extrabold text-gray-900">
              Staff Admins
            </h1>
            <p className="text-gray-500 text-sm">
              Manage secondary admins for your center. Staff can add tests and
              manage appointments.
            </p>
          </div>
        </div>
        <button
          onClick={() => {
            setForm(emptyForm);
            setError("");
            setShowPassword(false);
            setModalOpen(true);
          }}
          className="btn-primary inline-flex items-center gap-2"
        >
          <Plus className="w-4 h-4" /> Add Staff Admin
        </button>
      </div>

      {/* Confirm remove dialog */}
      {confirmRemoveId !== null && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6">
            <h3 className="font-bold text-gray-900 text-lg mb-2">
              Remove Staff Admin?
            </h3>
            <p className="text-gray-500 text-sm mb-6">
              This will revoke their staff role and unassign them from your
              center. The account will remain active.
            </p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setConfirmRemoveId(null)}
                className="btn-outline text-sm"
              >
                Cancel
              </button>
              <button
                onClick={() => handleRemove(confirmRemoveId)}
                disabled={removingId === confirmRemoveId}
                className="flex items-center gap-2 bg-red-600 hover:bg-red-700 text-white font-semibold py-2 px-5 rounded-full text-sm transition-colors disabled:opacity-60"
              >
                {removingId === confirmRemoveId ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  <Trash2 className="w-4 h-4" />
                )}
                Remove
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Create modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <div className="flex items-center justify-between mb-5">
              <h3 className="font-bold text-gray-900 text-lg">
                Add Staff Admin
              </h3>
              <button
                onClick={() => setModalOpen(false)}
                className="p-1 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {error && (
              <div className="mb-4 bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm">
                {error}
              </div>
            )}

            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Full Name <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="e.g. Jane Smith"
                  value={form.fullName}
                  onChange={(e) =>
                    setForm({ ...form, fullName: e.target.value })
                  }
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Email <span className="text-red-500">*</span>
                </label>
                <input
                  type="email"
                  className="input-field"
                  placeholder="staff@example.com"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Password <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? "text" : "password"}
                    className="input-field pr-10"
                    placeholder="Min. 8 characters"
                    value={form.password}
                    onChange={(e) =>
                      setForm({ ...form, password: e.target.value })
                    }
                    required
                    minLength={8}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showPassword ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              <div className="flex gap-3 pt-2 justify-end">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="btn-outline text-sm"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="btn-primary flex items-center gap-2 text-sm disabled:opacity-60"
                >
                  {saving ? (
                    <Loader2 className="w-4 h-4 animate-spin" />
                  ) : (
                    <Plus className="w-4 h-4" />
                  )}
                  Create Staff Admin
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Content */}
      {loading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
        </div>
      ) : staff.length === 0 ? (
        <div className="card text-center py-16">
          <UserCog className="w-12 h-12 text-gray-200 mx-auto mb-4" />
          <p className="text-gray-500 font-medium">No staff admins yet.</p>
          <p className="text-gray-400 text-sm mt-1">
            Add staff members who can handle appointments and add tests for your
            center.
          </p>
        </div>
      ) : (
        <div>
          <h2 className="text-lg font-bold text-gray-800 mb-4">
            Your Staff
            <span className="ml-2 text-sm font-normal text-gray-400">
              ({staff.length})
            </span>
          </h2>
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {staff.map((member) => (
              <div key={member.userId} className="card">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center shrink-0">
                      <span className="text-indigo-600 font-bold text-sm">
                        {member.fullName.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div className="min-w-0">
                      <p className="font-semibold text-gray-900 text-sm truncate">
                        {member.fullName}
                      </p>
                      <p className="text-gray-400 text-xs truncate">
                        {member.email}
                      </p>
                    </div>
                  </div>
                  <button
                    onClick={() => setConfirmRemoveId(member.userId)}
                    className="shrink-0 p-1.5 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Remove staff admin"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
                <div className="mt-3 pt-3 border-t border-gray-100">
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-50 text-indigo-700">
                    Staff Admin
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
