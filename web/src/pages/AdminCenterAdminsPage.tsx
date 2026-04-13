import { useEffect, useState } from "react";
import {
  UserCog,
  Building2,
  Plus,
  Trash2,
  Loader2,
  X,
  Eye,
  EyeOff,
} from "lucide-react";
import { authApi } from "../api/auth";
import { centersApi } from "../api/centers";
import type { CenterSearchResponse, UserProfileResponse } from "../types";

interface CreateForm {
  fullName: string;
  email: string;
  password: string;
  centerId: number | "";
}

const emptyForm: CreateForm = {
  fullName: "",
  email: "",
  password: "",
  centerId: "",
};

export default function AdminCenterAdminsPage() {
  const [centerAdmins, setCenterAdmins] = useState<UserProfileResponse[]>([]);
  const [centers, setCenters] = useState<CenterSearchResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState<CreateForm>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [removingId, setRemovingId] = useState<number | null>(null);
  const [confirmRemoveId, setConfirmRemoveId] = useState<number | null>(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [adminsRes, centersRes] = await Promise.all([
        authApi.listCenterAdmins(),
        centersApi.list(),
      ]);
      setCenterAdmins(adminsRes.data.data ?? []);
      setCenters(centersRes.data.data ?? []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Group admins by center
  const adminsByCenterId = new Map<number, UserProfileResponse[]>();
  centerAdmins.forEach((a) => {
    if (a.centerId) {
      const existing = adminsByCenterId.get(a.centerId) ?? [];
      adminsByCenterId.set(a.centerId, [...existing, a]);
    }
  });

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.centerId) {
      setError("Please select a center.");
      return;
    }
    setSaving(true);
    setError("");
    try {
      await authApi.createCenterAdmin({
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
        centerId: Number(form.centerId),
      });
      setModalOpen(false);
      setForm(emptyForm);
      await fetchData();
    } catch (err: any) {
      setError(
        err?.response?.data?.message ??
          err?.response?.data?.fieldErrors?.join(", ") ??
          "Failed to create center admin.",
      );
    } finally {
      setSaving(false);
    }
  };

  const handleRemove = async (userId: number) => {
    setRemovingId(userId);
    try {
      await authApi.removeCenterAdmin(userId);
      setConfirmRemoveId(null);
      await fetchData();
    } catch (err: any) {
      alert(err?.response?.data?.message ?? "Failed to remove center admin.");
    } finally {
      setRemovingId(null);
    }
  };

  // Centers without any admin assigned
  const unassignedCenters = centers.filter((c) => !adminsByCenterId.has(c.id));
  // Centers with at least one admin, sorted by center name
  const assignedCenters = centers.filter((c) => adminsByCenterId.has(c.id));

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
              Center Admins
            </h1>
            <p className="text-gray-500 text-sm">
              Assign second-tier admins to diagnostic centers.
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
          <Plus className="w-4 h-4" /> Add Center Admin
        </button>
      </div>

      {/* Remove confirm dialog */}
      {confirmRemoveId !== null && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6">
            <h3 className="font-bold text-gray-900 text-lg mb-2">
              Remove Center Admin?
            </h3>
            <p className="text-gray-500 text-sm mb-6">
              This will revoke their center admin role and unassign them from
              the center. The account will remain active.
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
                Add Center Admin
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
                  placeholder="admin@example.com"
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

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Assign to Center <span className="text-red-500">*</span>
                </label>
                <select
                  className="input-field"
                  value={form.centerId}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      centerId: e.target.value ? Number(e.target.value) : "",
                    })
                  }
                  required
                >
                  <option value="">— Select a center —</option>
                  {centers.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
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
                  Create Admin
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
      ) : (
        <>
          {/* Assigned centers */}
          {assignedCenters.length > 0 && (
            <div className="mb-10">
              <h2 className="text-lg font-bold text-gray-800 mb-4">
                Assigned Centers
                <span className="ml-2 text-sm font-normal text-gray-400">
                  ({assignedCenters.length})
                </span>
              </h2>
              <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {assignedCenters.map((center) => {
                  const admins = adminsByCenterId.get(center.id) ?? [];
                  return (
                    <div key={center.id} className="card flex flex-col gap-3">
                      {/* Center header */}
                      <div className="flex items-center justify-between gap-3">
                        <div className="flex items-center gap-2 min-w-0">
                          <div className="w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center shrink-0">
                            <Building2 className="w-4 h-4 text-blue-500" />
                          </div>
                          <p className="font-bold text-gray-900 text-sm truncate">
                            {center.name}
                          </p>
                        </div>
                        <button
                          onClick={() => {
                            setForm({ ...emptyForm, centerId: center.id });
                            setError("");
                            setShowPassword(false);
                            setModalOpen(true);
                          }}
                          className="shrink-0 flex items-center gap-1 text-xs font-medium text-blue-600 border border-blue-200 rounded-lg px-2.5 py-1.5 hover:bg-blue-50 transition-colors"
                          title="Add another admin to this center"
                        >
                          <Plus className="w-3.5 h-3.5" /> Add
                        </button>
                      </div>

                      {/* Admin list */}
                      <div className="divide-y divide-gray-100 border-t border-gray-100">
                        {admins.map((admin) => (
                          <div
                            key={admin.userId}
                            className="flex items-center justify-between gap-3 pt-3 first:pt-3"
                          >
                            <div className="flex items-center gap-2.5 min-w-0">
                              <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center shrink-0">
                                <span className="text-blue-600 font-bold text-xs">
                                  {admin.fullName.charAt(0).toUpperCase()}
                                </span>
                              </div>
                              <div className="min-w-0">
                                <p className="font-semibold text-gray-900 text-sm truncate">
                                  {admin.fullName}
                                </p>
                                <p className="text-gray-400 text-xs truncate">
                                  {admin.email}
                                </p>
                              </div>
                            </div>
                            <button
                              onClick={() => setConfirmRemoveId(admin.userId)}
                              className="shrink-0 p-1.5 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                              title="Remove this admin"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        ))}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Unassigned centers */}
          <div>
            <h2 className="text-lg font-bold text-gray-800 mb-4">
              Unassigned Centers
              <span className="ml-2 text-sm font-normal text-gray-400">
                ({unassignedCenters.length})
              </span>
            </h2>
            {unassignedCenters.length === 0 ? (
              <div className="card text-center py-10">
                <UserCog className="w-10 h-10 text-gray-200 mx-auto mb-3" />
                <p className="text-gray-400 text-sm">
                  All centers have a center admin assigned.
                </p>
              </div>
            ) : (
              <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {unassignedCenters.map((c) => (
                  <div
                    key={c.id}
                    className="card flex items-center justify-between gap-3"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-9 h-9 bg-gray-50 rounded-lg flex items-center justify-center shrink-0">
                        <Building2 className="w-4 h-4 text-gray-400" />
                      </div>
                      <p className="font-semibold text-gray-900 text-sm truncate">
                        {c.name}
                      </p>
                    </div>
                    <button
                      onClick={() => {
                        setForm({ ...emptyForm, centerId: c.id });
                        setError("");
                        setShowPassword(false);
                        setModalOpen(true);
                      }}
                      className="shrink-0 flex items-center gap-1 text-xs font-medium text-blue-600 border border-blue-200 rounded-lg px-3 py-1.5 hover:bg-blue-50 transition-colors"
                    >
                      <Plus className="w-3.5 h-3.5" /> Assign
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
