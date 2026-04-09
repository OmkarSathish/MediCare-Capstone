import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Building2,
  Plus,
  Search,
  Edit2,
  Trash2,
  Loader2,
  X,
  Check,
  MapPin,
  Phone,
  Mail,
  Tag,
  ChevronRight,
} from "lucide-react";
import { centersApi } from "../api/centers";
import type { CenterResponse } from "../types";

interface CenterForm {
  name: string;
  address: string;
  contactNo: string;
  contactEmail: string;
  servicesOffered: string;
}

const emptyForm: CenterForm = {
  name: "",
  address: "",
  contactNo: "",
  contactEmail: "",
  servicesOffered: "",
};

export default function AdminCentersPage() {
  const [centers, setCenters] = useState<CenterResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCenter, setEditingCenter] = useState<CenterResponse | null>(
    null,
  );
  const [form, setForm] = useState<CenterForm>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchCenters = async () => {
    setLoading(true);
    try {
      const res = await centersApi.list();
      // getById for each to get full details (list only returns search summaries)
      const detailed = await Promise.all(
        (res.data.data ?? []).map((c) =>
          centersApi.getById(c.id).then((r) => r.data.data!),
        ),
      );
      setCenters(detailed.filter(Boolean));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCenters();
  }, []);

  const openCreate = () => {
    setEditingCenter(null);
    setForm(emptyForm);
    setError("");
    setModalOpen(true);
  };

  const openEdit = (center: CenterResponse) => {
    setEditingCenter(center);
    setForm({
      name: center.name,
      address: center.address,
      contactNo: center.contactNo ?? "",
      contactEmail: center.contactEmail ?? "",
      servicesOffered: (center.servicesOffered ?? []).join(", "),
    });
    setError("");
    setModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError("");
    const payload = {
      name: form.name.trim(),
      address: form.address.trim(),
      contactNo: form.contactNo.trim() || undefined,
      contactEmail: form.contactEmail.trim() || undefined,
      servicesOffered: form.servicesOffered
        ? form.servicesOffered
            .split(",")
            .map((s) => s.trim())
            .filter(Boolean)
        : [],
    };
    try {
      if (editingCenter) {
        await centersApi.update(editingCenter.id, payload);
      } else {
        await centersApi.create(payload);
      }
      setModalOpen(false);
      fetchCenters();
    } catch (err: any) {
      setError(
        err?.response?.data?.message ??
          err?.response?.data?.fieldErrors?.join(", ") ??
          "Failed to save center.",
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (deleteId === null) return;
    setDeleting(true);
    try {
      await centersApi.delete(deleteId);
      setDeleteId(null);
      fetchCenters();
    } catch (err: any) {
      alert(err?.response?.data?.message ?? "Failed to deactivate center.");
    } finally {
      setDeleting(false);
    }
  };

  const filtered = centers.filter(
    (c) =>
      c.name.toLowerCase().includes(search.toLowerCase()) ||
      c.address.toLowerCase().includes(search.toLowerCase()),
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900">
            Manage Centers
          </h1>
          <p className="text-gray-500 mt-1">
            Add, edit, or deactivate diagnostic centers.
          </p>
        </div>
        <button
          onClick={openCreate}
          className="btn-primary inline-flex items-center gap-2"
        >
          <Plus className="w-4 h-4" /> Add Center
        </button>
      </div>

      {/* Search */}
      <div className="relative max-w-sm mb-6">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          type="text"
          className="input-field pl-9 py-2 text-sm"
          placeholder="Search centers…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* Delete confirm dialog */}
      {deleteId !== null && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6">
            <h3 className="font-bold text-gray-900 text-lg mb-2">
              Deactivate Center?
            </h3>
            <p className="text-gray-500 text-sm mb-6">
              This will soft-delete the center. Existing appointments will not
              be affected.
            </p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setDeleteId(null)}
                className="btn-outline text-sm"
              >
                Cancel
              </button>
              <button
                onClick={handleDelete}
                disabled={deleting}
                className="flex items-center gap-2 bg-red-600 hover:bg-red-700 text-white font-semibold py-2 px-5 rounded-full text-sm transition-colors disabled:opacity-60"
              >
                {deleting ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  <Trash2 className="w-4 h-4" />
                )}
                Deactivate
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Create / Edit modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full p-6 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-5">
              <h3 className="font-bold text-gray-900 text-lg">
                {editingCenter ? "Edit Center" : "Add New Center"}
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

            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Center Name <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="e.g. City Diagnostic Lab"
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Address <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="123 Main St, City"
                  value={form.address}
                  onChange={(e) =>
                    setForm({ ...form, address: e.target.value })
                  }
                  required
                />
              </div>

              <div className="grid sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">
                    Phone
                  </label>
                  <input
                    type="tel"
                    className="input-field"
                    placeholder="+1 (555) 000-0000"
                    value={form.contactNo}
                    onChange={(e) =>
                      setForm({ ...form, contactNo: e.target.value })
                    }
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">
                    Email
                  </label>
                  <input
                    type="email"
                    className="input-field"
                    placeholder="center@example.com"
                    value={form.contactEmail}
                    onChange={(e) =>
                      setForm({ ...form, contactEmail: e.target.value })
                    }
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Services Offered
                </label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="Blood Tests, X-Ray, MRI (comma-separated)"
                  value={form.servicesOffered}
                  onChange={(e) =>
                    setForm({ ...form, servicesOffered: e.target.value })
                  }
                />
                <p className="text-xs text-gray-400 mt-1">
                  Separate multiple services with commas.
                </p>
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
                    <Check className="w-4 h-4" />
                  )}
                  {editingCenter ? "Save Changes" : "Create Center"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Centers list */}
      {loading ? (
        <div className="flex items-center justify-center py-24">
          <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-20 card">
          <Building2 className="w-14 h-14 text-gray-200 mx-auto mb-4" />
          <p className="text-gray-500 text-lg">No centers found.</p>
          <button
            onClick={openCreate}
            className="btn-primary inline-flex mt-4 items-center gap-2"
          >
            <Plus className="w-4 h-4" /> Add First Center
          </button>
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {filtered.map((center) => (
            <div
              key={center.id}
              className="card hover:shadow-md transition-all group"
            >
              {/* Card header */}
              <div className="flex items-start gap-3 mb-4">
                <div className="w-11 h-11 bg-blue-50 rounded-xl flex items-center justify-center shrink-0">
                  <Building2 className="w-5 h-5 text-blue-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="font-bold text-gray-900 truncate">
                    {center.name}
                  </h3>
                  <span
                    className={`inline-block text-xs font-medium px-2 py-0.5 rounded-full mt-0.5 ${
                      center.status === "ACTIVE"
                        ? "bg-green-100 text-green-700"
                        : "bg-gray-100 text-gray-500"
                    }`}
                  >
                    {center.status}
                  </span>
                </div>
              </div>

              {/* Details */}
              <div className="space-y-1.5 text-sm text-gray-500 mb-4">
                {center.address && (
                  <div className="flex items-start gap-1.5">
                    <MapPin className="w-3.5 h-3.5 mt-0.5 shrink-0 text-blue-400" />
                    <span className="line-clamp-2">{center.address}</span>
                  </div>
                )}
                {center.contactNo && (
                  <div className="flex items-center gap-1.5">
                    <Phone className="w-3.5 h-3.5 shrink-0 text-blue-400" />
                    <span>{center.contactNo}</span>
                  </div>
                )}
                {center.contactEmail && (
                  <div className="flex items-center gap-1.5">
                    <Mail className="w-3.5 h-3.5 shrink-0 text-blue-400" />
                    <span className="truncate">{center.contactEmail}</span>
                  </div>
                )}
              </div>

              {/* Services */}
              {center.servicesOffered?.length > 0 && (
                <div className="flex flex-wrap gap-1.5 mb-4">
                  {center.servicesOffered.slice(0, 3).map((s) => (
                    <span
                      key={s}
                      className="inline-flex items-center gap-1 text-xs bg-blue-50 text-blue-600 rounded-full px-2.5 py-0.5"
                    >
                      <Tag className="w-2.5 h-2.5" />
                      {s}
                    </span>
                  ))}
                  {center.servicesOffered.length > 3 && (
                    <span className="text-xs text-gray-400">
                      +{center.servicesOffered.length - 3} more
                    </span>
                  )}
                </div>
              )}

              {/* Tests count */}
              <div className="text-xs text-gray-400 mb-4">
                {center.tests?.length ?? 0} test
                {(center.tests?.length ?? 0) !== 1 ? "s" : ""} available
              </div>

              {/* Actions */}
              <div className="flex items-center gap-2 pt-3 border-t border-gray-100">
                <Link
                  to={`/centers/${center.id}`}
                  className="flex-1 text-center text-xs font-medium text-blue-600 border border-blue-100 rounded-lg px-3 py-1.5 hover:bg-blue-50 transition-colors inline-flex items-center justify-center gap-1"
                >
                  View <ChevronRight className="w-3 h-3" />
                </Link>
                <button
                  onClick={() => openEdit(center)}
                  className="flex items-center gap-1 text-xs font-medium text-gray-600 border border-gray-200 rounded-lg px-3 py-1.5 hover:bg-gray-50 transition-colors"
                >
                  <Edit2 className="w-3.5 h-3.5" /> Edit
                </button>
                <button
                  onClick={() => setDeleteId(center.id)}
                  className="flex items-center gap-1 text-xs font-medium text-red-500 border border-red-100 rounded-lg px-3 py-1.5 hover:bg-red-50 transition-colors"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
