import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  Building2,
  Microscope,
  Calendar,
  CheckCircle,
  ChevronRight,
  ChevronLeft,
  Loader2,
  Search,
} from "lucide-react";
import { centersApi } from "../api/centers";
import { appointmentApi } from "../api/appointments";
import type {
  CenterSearchResponse,
  CenterTestOfferingResponse,
} from "../types";

import { useAuth } from "../context/AuthContext";

type Step = 1 | 2 | 3 | 4;

export default function BookAppointmentPage() {
  const { isAdmin } = useAuth();
  const navigate = useNavigate();

  if (isAdmin) {
    navigate("/admin", { replace: true });
    return null;
  }
  const [params] = useSearchParams();
  const preFillCenterId = params.get("centerId");
  const preFillTestId = params.get("testId");

  const [step, setStep] = useState<Step>(1);

  // Step 1 - Center selection
  const [centers, setCenters] = useState<CenterSearchResponse[]>([]);
  const [centerSearch, setCenterSearch] = useState("");
  const [selectedCenter, setSelectedCenter] =
    useState<CenterSearchResponse | null>(null);
  const [centersLoading, setCentersLoading] = useState(false);

  // Step 2 - Test selection
  const [centerTests, setCenterTests] = useState<CenterTestOfferingResponse[]>(
    [],
  );
  const [selectedTestIds, setSelectedTestIds] = useState<number[]>([]);
  const [testsLoading, setTestsLoading] = useState(false);

  // Step 3 - Date
  const [appointmentDate, setAppointmentDate] = useState("");

  // Submit
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  // Load centers on mount — if coming from a test page, only show centers that offer that test
  useEffect(() => {
    setCentersLoading(true);
    const fetchPromise = preFillTestId
      ? centersApi.getByTestId(Number(preFillTestId))
      : centersApi.list();
    fetchPromise
      .then((r) => {
        const list = r.data.data ?? [];
        setCenters(list);
        if (preFillCenterId) {
          const found = list.find((c) => c.id === Number(preFillCenterId));
          if (found) {
            setSelectedCenter(found);
            setStep(2);
          }
        }
      })
      .finally(() => setCentersLoading(false));
  }, [preFillCenterId, preFillTestId]);

  // Load tests when center selected
  useEffect(() => {
    if (!selectedCenter) return;
    setTestsLoading(true);
    centersApi
      .getTests(selectedCenter.id)
      .then((r) => {
        const tests = r.data.data ?? [];
        setCenterTests(tests);
        if (preFillTestId) {
          const found = tests.find((t) => t.testId === Number(preFillTestId));
          if (found) setSelectedTestIds([found.testId]);
        }
      })
      .finally(() => setTestsLoading(false));
  }, [selectedCenter, preFillTestId]);

  const filteredCenters = centers.filter(
    (c) =>
      c.name.toLowerCase().includes(centerSearch.toLowerCase()) ||
      c.address.toLowerCase().includes(centerSearch.toLowerCase()),
  );

  const toggleTest = (testId: number) => {
    setSelectedTestIds((prev) =>
      prev.includes(testId)
        ? prev.filter((id) => id !== testId)
        : [...prev, testId],
    );
  };

  const totalCost = centerTests
    .filter((t) => selectedTestIds.includes(t.testId))
    .reduce((sum, t) => sum + t.testPrice, 0);

  const handleSubmit = async () => {
    if (!selectedCenter || !selectedTestIds.length || !appointmentDate) return;
    setSubmitting(true);
    setSubmitError("");
    try {
      const res = await appointmentApi.book({
        centerId: selectedCenter.id,
        testIds: selectedTestIds,
        appointmentDate,
      });
      navigate(`/appointments/${res.data.data?.id}`);
    } catch (err: any) {
      setSubmitError(
        err?.response?.data?.message ?? "Failed to book appointment.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  const minDate = new Date();
  minDate.setDate(minDate.getDate() + 1);
  const minDateStr = minDate.toISOString().split("T")[0];

  const stepLabels = [
    "Select Center",
    "Select Tests",
    "Choose Date",
    "Confirm",
  ];

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-10">
      <h1 className="text-3xl font-extrabold text-gray-900 mb-2">
        Book Appointment
      </h1>
      <p className="text-gray-500 mb-8">
        Follow the steps below to schedule your diagnostic tests.
      </p>

      {/* Step indicator */}
      <div className="flex items-center mb-10">
        {stepLabels.map((label, i) => {
          const s = (i + 1) as Step;
          const active = step === s;
          const done = step > s;
          return (
            <div key={s} className="flex items-center flex-1">
              <div className="flex flex-col items-center">
                <div
                  className={`w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold transition-colors
                  ${done ? "bg-green-500 text-white" : active ? "bg-blue-600 text-white" : "bg-gray-100 text-gray-400"}`}
                >
                  {done ? <CheckCircle className="w-5 h-5" /> : s}
                </div>
                <span
                  className={`text-xs mt-1 font-medium ${active ? "text-blue-600" : done ? "text-green-500" : "text-gray-400"}`}
                >
                  {label}
                </span>
              </div>
              {i < stepLabels.length - 1 && (
                <div
                  className={`flex-1 h-0.5 mx-2 mb-4 ${done ? "bg-green-500" : "bg-gray-100"}`}
                />
              )}
            </div>
          );
        })}
      </div>

      {/* ── Step 1: Center ── */}
      {step === 1 && (
        <div className="card">
          <h2 className="text-lg font-bold text-gray-900 mb-5 flex items-center gap-2">
            <Building2 className="w-5 h-5 text-blue-600" /> Choose a Diagnostic
            Center
          </h2>
          <div className="relative mb-4">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              className="input-field pl-10"
              placeholder="Search centers…"
              value={centerSearch}
              onChange={(e) => setCenterSearch(e.target.value)}
            />
          </div>
          {centersLoading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="w-6 h-6 animate-spin text-blue-600" />
            </div>
          ) : (
            <div className="space-y-2 max-h-80 overflow-y-auto pr-1">
              {filteredCenters.map((c) => (
                <button
                  key={c.id}
                  onClick={() => setSelectedCenter(c)}
                  className={`w-full text-left flex items-center gap-3 p-4 rounded-xl border transition-all
                    ${selectedCenter?.id === c.id ? "border-blue-600 bg-blue-50" : "border-gray-100 hover:border-blue-200 hover:bg-gray-50"}`}
                >
                  <div
                    className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0
                    ${selectedCenter?.id === c.id ? "bg-blue-600" : "bg-gray-100"}`}
                  >
                    <Building2
                      className={`w-4 h-4 ${selectedCenter?.id === c.id ? "text-white" : "text-gray-500"}`}
                    />
                  </div>
                  <div>
                    <p className="font-semibold text-gray-900 text-sm">
                      {c.name}
                    </p>
                    <p className="text-xs text-gray-500">{c.address}</p>
                  </div>
                  {selectedCenter?.id === c.id && (
                    <CheckCircle className="w-5 h-5 text-blue-600 ml-auto" />
                  )}
                </button>
              ))}
            </div>
          )}
          <div className="mt-6 flex justify-end">
            <button
              disabled={!selectedCenter}
              onClick={() => setStep(2)}
              className="btn-primary inline-flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Next <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* ── Step 2: Tests ── */}
      {step === 2 && (
        <div className="card">
          <h2 className="text-lg font-bold text-gray-900 mb-1 flex items-center gap-2">
            <Microscope className="w-5 h-5 text-blue-600" /> Select Tests
          </h2>
          <p className="text-gray-400 text-sm mb-5">
            at {selectedCenter?.name}
          </p>

          {testsLoading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="w-6 h-6 animate-spin text-blue-600" />
            </div>
          ) : !centerTests.length ? (
            <p className="text-gray-400 text-sm text-center py-8">
              No tests available at this center.
            </p>
          ) : (
            <div className="space-y-2 max-h-80 overflow-y-auto pr-1">
              {centerTests.map((t) => {
                const selected = selectedTestIds.includes(t.testId);
                return (
                  <button
                    key={t.testId}
                    onClick={() => toggleTest(t.testId)}
                    className={`w-full text-left flex items-center gap-3 p-4 rounded-xl border transition-all
                      ${selected ? "border-blue-600 bg-blue-50" : "border-gray-100 hover:border-blue-200 hover:bg-gray-50"}`}
                  >
                    <div
                      className={`w-5 h-5 rounded border-2 flex items-center justify-center shrink-0 transition-colors
                      ${selected ? "bg-blue-600 border-blue-600" : "border-gray-300"}`}
                    >
                      {selected && (
                        <CheckCircle className="w-3.5 h-3.5 text-white" />
                      )}
                    </div>
                    <div className="flex-1">
                      <p className="font-semibold text-gray-900 text-sm">
                        {t.testName}
                      </p>
                    </div>
                    <span className="text-blue-600 font-bold text-sm shrink-0">
                      ₹{t.testPrice.toFixed(2)}
                    </span>
                  </button>
                );
              })}
            </div>
          )}

          {selectedTestIds.length > 0 && (
            <div className="mt-4 p-3 bg-blue-50 rounded-xl flex items-center justify-between">
              <span className="text-sm text-blue-700 font-medium">
                {selectedTestIds.length} test(s) selected
              </span>
              <span className="text-blue-700 font-bold">
                Total: ₹{totalCost.toFixed(2)}
              </span>
            </div>
          )}

          <div className="mt-6 flex justify-between">
            <button
              onClick={() => setStep(1)}
              className="btn-outline flex items-center gap-2"
            >
              <ChevronLeft className="w-4 h-4" /> Back
            </button>
            <button
              disabled={!selectedTestIds.length}
              onClick={() => setStep(3)}
              className="btn-primary inline-flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Next <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* ── Step 3: Date ── */}
      {step === 3 && (
        <div className="card">
          <h2 className="text-lg font-bold text-gray-900 mb-5 flex items-center gap-2">
            <Calendar className="w-5 h-5 text-blue-600" /> Choose Appointment
            Date
          </h2>
          <input
            type="date"
            className="input-field"
            min={minDateStr}
            value={appointmentDate}
            onChange={(e) => setAppointmentDate(e.target.value)}
          />
          <p className="text-xs text-gray-400 mt-2">
            Appointments can be booked starting from tomorrow.
          </p>

          <div className="mt-6 flex justify-between">
            <button
              onClick={() => setStep(2)}
              className="btn-outline flex items-center gap-2"
            >
              <ChevronLeft className="w-4 h-4" /> Back
            </button>
            <button
              disabled={!appointmentDate}
              onClick={() => setStep(4)}
              className="btn-primary inline-flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Next <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* ── Step 4: Confirm ── */}
      {step === 4 && (
        <div className="card">
          <h2 className="text-lg font-bold text-gray-900 mb-5 flex items-center gap-2">
            <CheckCircle className="w-5 h-5 text-blue-600" /> Confirm Booking
          </h2>

          <div className="space-y-4">
            <div className="p-4 bg-gray-50 rounded-xl">
              <p className="text-xs text-gray-400 mb-1">Center</p>
              <p className="font-semibold text-gray-900">
                {selectedCenter?.name}
              </p>
              <p className="text-sm text-gray-500">{selectedCenter?.address}</p>
            </div>

            <div className="p-4 bg-gray-50 rounded-xl">
              <p className="text-xs text-gray-400 mb-2">Selected Tests</p>
              <div className="space-y-1.5">
                {centerTests
                  .filter((t) => selectedTestIds.includes(t.testId))
                  .map((t) => (
                    <div
                      key={t.testId}
                      className="flex items-center justify-between text-sm"
                    >
                      <span className="flex items-center gap-2">
                        <CheckCircle className="w-3.5 h-3.5 text-green-500" />
                        {t.testName}
                      </span>
                      <span className="text-blue-600 font-medium">
                        ₹{t.testPrice.toFixed(2)}
                      </span>
                    </div>
                  ))}
                <div className="flex items-center justify-between text-sm font-bold pt-2 border-t border-gray-200 mt-2">
                  <span>Total</span>
                  <span className="text-blue-600">₹{totalCost.toFixed(2)}</span>
                </div>
              </div>
            </div>

            <div className="p-4 bg-gray-50 rounded-xl">
              <p className="text-xs text-gray-400 mb-1">Date</p>
              <p className="font-semibold text-gray-900">
                {new Date(appointmentDate).toLocaleDateString("en-US", {
                  weekday: "long",
                  year: "numeric",
                  month: "long",
                  day: "numeric",
                })}
              </p>
            </div>
          </div>

          {submitError && (
            <div className="mt-4 bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm">
              {submitError}
            </div>
          )}

          <div className="mt-6 flex justify-between">
            <button
              onClick={() => setStep(3)}
              className="btn-outline flex items-center gap-2"
            >
              <ChevronLeft className="w-4 h-4" /> Back
            </button>
            <button
              onClick={handleSubmit}
              disabled={submitting}
              className="btn-primary flex items-center gap-2 disabled:opacity-60"
            >
              {submitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" /> Booking…
                </>
              ) : (
                "Confirm Booking"
              )}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
