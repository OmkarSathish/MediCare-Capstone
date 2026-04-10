import { useEffect, useState } from "react";
import {
  Microscope,
  Plus,
  Search,
  Edit2,
  Trash2,
  Loader2,
  X,
  Check,
  Tag,
  Minus,
} from "lucide-react";
import { testsApi } from "../api/tests";
import { centersApi } from "../api/centers";
import { useAuth } from "../context/AuthContext";
import type { CenterTestOfferingResponse, TestResponse } from "../types";

interface TestForm {
  testName: string;
  testPrice: string;
  normalValue: string;
  units: string;
}

const emptyForm: TestForm = {
  testName: "",
  testPrice: "",
  normalValue: "",
  units: "",
};

export default function AdminTestsPage() {
  const { isCenterAdmin, adminCenterId } = useAuth();

  // ── Center admin state ─────────────────────────────────────────────────────
  const [centerTests, setCenterTests] = useState<CenterTestOfferingResponse[]>(
    [],
  );
  const [allCatalogTests, setAllCatalogTests] = useState<TestResponse[]>([]);
  const [centerLoading, setCenterLoading] = useState(true);
  const [centerSearch, setCenterSearch] = useState("");
  const [togglingId, setTogglingId] = useState<number | null>(null);

  // ── Primary admin state ────────────────────────────────────────────────────
  const [tests, setTests] = useState<TestResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTest, setEditingTest] = useState<TestResponse | null>(null);
  const [form, setForm] = useState<TestForm>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchTests = async () => {
    setLoading(true);
    try {
      const res = await testsApi.list();
      setTests(res.data.data ?? []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTests();
  }, []);

  // ── Center admin fetch ─────────────────────────────────────────────────────
  const fetchCenterTests = async () => {
    if (!adminCenterId) return;
    setCenterLoading(true);
    try {
      const [centerRes, catalogRes] = await Promise.all([
        centersApi.getTests(adminCenterId),
        testsApi.list(),
      ]);
      setCenterTests(centerRes.data.data ?? []);
      setAllCatalogTests(catalogRes.data.data ?? []);
    } finally {
      setCenterLoading(false);
    }
  };

  useEffect(() => {
    if (isCenterAdmin) fetchCenterTests();
  }, [isCenterAdmin, adminCenterId]);

  const openCreate = () => {
    setEditingTest(null);
    setForm(emptyForm);
    setError("");
    setModalOpen(true);
  };

  const openEdit = (test: TestResponse) => {
    setEditingTest(test);
    setForm({
      testName: test.testName,
      testPrice: String(test.testPrice),
      normalValue: test.normalValue ?? "",
      units: test.units ?? "",
    });
    setError("");
    setModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    const price = parseFloat(form.testPrice);
    if (isNaN(price) || price <= 0) {
      setError("Price must be a positive number.");
      return;
    }
    setSaving(true);
    setError("");
    try {
      if (editingTest) {
        await testsApi.update(editingTest.id, {
          testName: form.testName.trim(),
          testPrice: price,
          normalValue: form.normalValue.trim() || undefined,
          units: form.units.trim() || undefined,
        });
      } else {
        await testsApi.create({
          testName: form.testName.trim(),
          testPrice: price,
          normalValue: form.normalValue.trim() || undefined,
          units: form.units.trim() || undefined,
        });
      }
      setModalOpen(false);
      fetchTests();
    } catch (err: any) {
      setError(
        err?.response?.data?.message ??
          err?.response?.data?.fieldErrors?.join(", ") ??
          "Failed to save test.",
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (deleteId === null) return;
    setDeleting(true);
    try {
      await testsApi.delete(deleteId);
      setDeleteId(null);
      fetchTests();
    } catch (err: any) {
      alert(err?.response?.data?.message ?? "Failed to deactivate test.");
    } finally {
      setDeleting(false);
    }
  };

  const filtered = tests.filter((t) =>
    t.testName.toLowerCase().includes(search.toLowerCase()),
  );

  // ── Center admin view (early return) ──────────────────────────────────────
  if (isCenterAdmin) {
    const assignedIds = new Set(centerTests.map((t) => t.testId));
    const available = allCatalogTests.filter(
      (t) =>
        !assignedIds.has(t.id) &&
        t.status === "ACTIVE" &&
        t.testName.toLowerCase().includes(centerSearch.toLowerCase()),
    );
    const filteredCenter = centerTests.filter((t) =>
      t.testName.toLowerCase().includes(centerSearch.toLowerCase()),
    );

    const handleAdd = async (testId: number) => {
      if (!adminCenterId) return;
      setTogglingId(testId);
      try {
        await centersApi.addTest(adminCenterId, testId);
        await fetchCenterTests();
      } catch (err: any) {
        alert(err?.response?.data?.message ?? "Failed to add test.");
      } finally {
        setTogglingId(null);
      }
    };

    const handleRemove = async (testId: number) => {
      if (!adminCenterId) return;
      setTogglingId(testId);
      try {
        await centersApi.removeTest(adminCenterId, testId);
        await fetchCenterTests();
      } catch (err: any) {
        alert(err?.response?.data?.message ?? "Failed to remove test.");
      } finally {
        setTogglingId(null);
      }
    };

    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-extrabold text-gray-900">
              Center Tests
            </h1>
            <p className="text-gray-500 mt-1">
              Manage the tests offered at your diagnostic center.
            </p>
          </div>
        </div>

        <div className="relative max-w-sm mb-8">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            className="input-field pl-9 py-2 text-sm"
            placeholder="Search tests…"
            value={centerSearch}
            onChange={(e) => setCenterSearch(e.target.value)}
          />
        </div>

        {centerLoading ? (
          <div className="flex justify-center py-20">
            <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
          </div>
        ) : (
          <>
            {/* Assigned tests */}
            <h2 className="text-lg font-bold text-gray-800 mb-3">
              Assigned to your center
              <span className="ml-2 text-sm font-normal text-gray-400">
                ({filteredCenter.length})
              </span>
            </h2>
            {filteredCenter.length === 0 ? (
              <div className="card text-center py-10 mb-8">
                <Microscope className="w-10 h-10 text-gray-200 mx-auto mb-3" />
                <p className="text-gray-400 text-sm">No tests assigned yet.</p>
              </div>
            ) : (
              <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
                {filteredCenter.map((t) => (
                  <div
                    key={t.testId}
                    className="card flex items-center justify-between gap-3"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-9 h-9 bg-indigo-50 rounded-lg flex items-center justify-center shrink-0">
                        <Microscope className="w-4 h-4 text-indigo-600" />
                      </div>
                      <div className="min-w-0">
                        <p className="font-semibold text-gray-900 text-sm truncate">
                          {t.testName}
                        </p>
                        <p className="text-blue-600 text-xs font-medium">
                          ₹{t.testPrice.toFixed(2)}
                        </p>
                      </div>
                    </div>
                    <button
                      onClick={() => handleRemove(t.testId)}
                      disabled={togglingId === t.testId}
                      className="shrink-0 flex items-center gap-1 text-xs font-medium text-red-500 border border-red-100 rounded-lg px-3 py-1.5 hover:bg-red-50 transition-colors disabled:opacity-50"
                    >
                      {togglingId === t.testId ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      ) : (
                        <Minus className="w-3.5 h-3.5" />
                      )}
                      Remove
                    </button>
                  </div>
                ))}
              </div>
            )}

            {/* Available to add */}
            <h2 className="text-lg font-bold text-gray-800 mb-3">
              Available to add
              <span className="ml-2 text-sm font-normal text-gray-400">
                ({available.length})
              </span>
            </h2>
            {available.length === 0 ? (
              <div className="card text-center py-10">
                <Check className="w-10 h-10 text-gray-200 mx-auto mb-3" />
                <p className="text-gray-400 text-sm">
                  All active tests are already assigned.
                </p>
              </div>
            ) : (
              <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {available.map((t) => (
                  <div
                    key={t.id}
                    className="card flex items-center justify-between gap-3"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-9 h-9 bg-gray-50 rounded-lg flex items-center justify-center shrink-0">
                        <Microscope className="w-4 h-4 text-gray-400" />
                      </div>
                      <div className="min-w-0">
                        <p className="font-semibold text-gray-900 text-sm truncate">
                          {t.testName}
                        </p>
                        <p className="text-blue-600 text-xs font-medium">
                          ₹{t.testPrice.toFixed(2)}
                        </p>
                      </div>
                    </div>
                    <button
                      onClick={() => handleAdd(t.id)}
                      disabled={togglingId === t.id}
                      className="shrink-0 flex items-center gap-1 text-xs font-medium text-green-600 border border-green-200 rounded-lg px-3 py-1.5 hover:bg-green-50 transition-colors disabled:opacity-50"
                    >
                      {togglingId === t.id ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      ) : (
                        <Plus className="w-3.5 h-3.5" />
                      )}
                      Add
                    </button>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900">
            Manage Tests
          </h1>
          <p className="text-gray-500 mt-1">
            Add, edit, or deactivate diagnostic tests.
          </p>
        </div>
        <button
          onClick={openCreate}
          className="btn-primary inline-flex items-center gap-2"
        >
          <Plus className="w-4 h-4" /> Add Test
        </button>
      </div>

      {/* Search */}
      <div className="relative max-w-sm mb-6">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          type="text"
          className="input-field pl-9 py-2 text-sm"
          placeholder="Search tests…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* Delete confirm dialog */}
      {deleteId !== null && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6">
            <h3 className="font-bold text-gray-900 text-lg mb-2">
              Deactivate Test?
            </h3>
            <p className="text-gray-500 text-sm mb-6">
              This will soft-delete the test. It will no longer appear in
              bookings.
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
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <div className="flex items-center justify-between mb-5">
              <h3 className="font-bold text-gray-900 text-lg">
                {editingTest ? "Edit Test" : "Add New Test"}
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
                  Test Name <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="e.g. Complete Blood Count"
                  value={form.testName}
                  onChange={(e) =>
                    setForm({ ...form, testName: e.target.value })
                  }
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Price (₹) <span className="text-red-500">*</span>
                </label>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  className="input-field"
                  placeholder="e.g. 350.00"
                  value={form.testPrice}
                  onChange={(e) =>
                    setForm({ ...form, testPrice: e.target.value })
                  }
                  required
                />
              </div>

              <div className="grid sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">
                    Normal Value
                  </label>
                  <input
                    type="text"
                    className="input-field"
                    placeholder="e.g. 4.5–11.0"
                    value={form.normalValue}
                    onChange={(e) =>
                      setForm({ ...form, normalValue: e.target.value })
                    }
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">
                    Units
                  </label>
                  <input
                    type="text"
                    className="input-field"
                    placeholder="e.g. ×10⁹/L"
                    value={form.units}
                    onChange={(e) =>
                      setForm({ ...form, units: e.target.value })
                    }
                  />
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
                    <Check className="w-4 h-4" />
                  )}
                  {editingTest ? "Save Changes" : "Create Test"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Tests grid */}
      {loading ? (
        <div className="flex items-center justify-center py-24">
          <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-20 card">
          <Microscope className="w-14 h-14 text-gray-200 mx-auto mb-4" />
          <p className="text-gray-500 text-lg">No tests found.</p>
          <button
            onClick={openCreate}
            className="btn-primary inline-flex mt-4 items-center gap-2"
          >
            <Plus className="w-4 h-4" /> Add First Test
          </button>
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {filtered.map((test) => (
            <div key={test.id} className="card hover:shadow-md transition-all">
              {/* Header */}
              <div className="flex items-start gap-3 mb-4">
                <div className="w-11 h-11 bg-indigo-50 rounded-xl flex items-center justify-center shrink-0">
                  <Microscope className="w-5 h-5 text-indigo-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="font-bold text-gray-900 truncate">
                    {test.testName}
                  </h3>
                  <span
                    className={`inline-block text-xs font-medium px-2 py-0.5 rounded-full mt-0.5 ${
                      test.status === "ACTIVE"
                        ? "bg-green-100 text-green-700"
                        : "bg-gray-100 text-gray-500"
                    }`}
                  >
                    {test.status}
                  </span>
                </div>
              </div>

              {/* Details */}
              <div className="space-y-2 text-sm mb-4">
                <div className="flex justify-between">
                  <span className="text-gray-400">Price</span>
                  <span className="font-bold text-blue-600">
                    ₹{test.testPrice.toFixed(2)}
                  </span>
                </div>
                {test.normalValue && (
                  <div className="flex justify-between">
                    <span className="text-gray-400">Normal range</span>
                    <span className="text-gray-700 font-medium">
                      {test.normalValue}
                      {test.units ? ` ${test.units}` : ""}
                    </span>
                  </div>
                )}
                {test.categoryName && (
                  <div className="flex items-center gap-1.5 pt-1">
                    <Tag className="w-3.5 h-3.5 text-gray-400" />
                    <span className="text-gray-500 text-xs">
                      {test.categoryName}
                    </span>
                  </div>
                )}
              </div>

              {/* Actions */}
              <div className="flex items-center gap-2 pt-3 border-t border-gray-100">
                <button
                  onClick={() => openEdit(test)}
                  className="flex-1 flex items-center justify-center gap-1.5 text-xs font-medium text-gray-600 border border-gray-200 rounded-lg px-3 py-1.5 hover:bg-gray-50 transition-colors"
                >
                  <Edit2 className="w-3.5 h-3.5" /> Edit
                </button>
                <button
                  onClick={() => setDeleteId(test.id)}
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
