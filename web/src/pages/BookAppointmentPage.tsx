import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useTitle } from "../hooks/useTitle";
import {
  Building2,
  Microscope,
  Calendar,
  CheckCircle,
  ChevronRight,
  ChevronLeft,
  Loader2,
  Search,
  X,
  ArrowLeft,
  Shield,
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
  useTitle("Book Appointment");
  const { isAdmin, user } = useAuth();
  const navigate = useNavigate();

  if (isAdmin) {
    navigate("/admin", { replace: true });
    return null;
  }
  const [params] = useSearchParams();
  const preFillCenterId = params.get("centerId");
  const preFillTestId = params.get("testId");
  const preFillDate = params.get("date");

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

  // Step 3 - Date + special requests
  const [appointmentDate, setAppointmentDate] = useState(() => {
    // Pre-fill if a valid future date was passed via ?date=
    if (preFillDate) {
      const minD = new Date();
      if (preFillDate >= minD.toISOString().split("T")[0]) return preFillDate;
    }
    return "";
  });
  const [specialRequests, setSpecialRequests] = useState("");

  // Submit
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  // Payment modal
  const [payModal, setPayModal] = useState(false);
  type PayScreen = "contact" | "upi";
  const [payScreen, setPayScreen] = useState<PayScreen>("contact");
  const [payPhone, setPayPhone] = useState("");

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
        specialRequests: specialRequests.trim() || undefined,
      });
      setPayModal(false);
      navigate(`/appointments/${res.data.data?.id}`);
    } catch (err: any) {
      setPayModal(false);
      setSubmitError(
        err?.response?.data?.message ?? "Failed to book appointment.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  const openPayModal = () => {
    const raw = (user?.phone ?? "")
      .replace(/(\+91[\-\s]?|^0)/, "")
      .replace(/\D/g, "")
      .slice(0, 10);
    setPayPhone(raw);
    setPayScreen("contact");
    setPayModal(true);
  };

  const minDate = new Date();
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
            Appointments can be booked starting from today.
          </p>
          <p className="text-xs text-gray-400 mt-1">
            You can book up to 4 tests across 3 different centers per day.
          </p>

          <div className="mt-5">
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              Special Requests{" "}
              <span className="text-gray-400 font-normal">(optional)</span>
            </label>
            <textarea
              className="input-field resize-none h-24"
              placeholder="e.g. wheelchair access, stretcher, visual assistance, hearing support, crutches…"
              value={specialRequests}
              onChange={(e) => setSpecialRequests(e.target.value)}
              maxLength={500}
            />
            <p className="text-xs text-gray-400 mt-1">
              Let the center know about any accessibility or assistance needs.
            </p>
          </div>

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

            {specialRequests.trim() && (
              <div className="p-4 bg-amber-50 border border-amber-100 rounded-xl">
                <p className="text-xs text-amber-600 font-medium mb-1">
                  Special Requests
                </p>
                <p className="text-sm text-gray-700">
                  {specialRequests.trim()}
                </p>
              </div>
            )}
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
              onClick={openPayModal}
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

      {/* Payment Modal */}
      {payModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div
            className="absolute inset-0 bg-black/50"
            onClick={() => !submitting && setPayModal(false)}
          />
          <div className="relative w-full max-w-sm mx-4 rounded-2xl overflow-hidden shadow-2xl flex flex-col">
            {/* Header */}
            <div className="bg-[#2563EB] px-5 pt-5 pb-6 text-white">
              <div className="flex items-center justify-between mb-4">
                {payScreen === "upi" ? (
                  <button
                    onClick={() => setPayScreen("contact")}
                    className="w-7 h-7 rounded-full bg-white/20 flex items-center justify-center hover:bg-white/30 transition"
                  >
                    <ArrowLeft className="w-4 h-4" />
                  </button>
                ) : (
                  <div className="w-7 h-7" />
                )}
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 bg-white/20 rounded-lg flex items-center justify-center font-bold text-sm">
                    M
                  </div>
                  <div>
                    <p className="font-bold text-sm leading-tight">MediCare</p>
                    <p className="text-[10px] text-green-300 flex items-center gap-1">
                      <Shield className="w-2.5 h-2.5" /> Razorpay Trusted
                      Business
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => !submitting && setPayModal(false)}
                  className="w-7 h-7 rounded-full bg-white/20 flex items-center justify-center hover:bg-white/30 transition"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
              <div className="text-center mt-2">
                <p className="text-white/70 text-xs mb-1">Total Amount</p>
                <p className="text-3xl font-extrabold tracking-tight">
                  ₹{totalCost.toFixed(2)}
                </p>
              </div>
            </div>

            {/* Body */}
            <div className="bg-white">
              {/* Screen 1: Contact */}
              {payScreen === "contact" && (
                <div className="px-5 py-5 space-y-3">
                  <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    Contact Details
                  </p>
                  <div>
                    <label className="block text-xs text-gray-500 mb-1">
                      Phone Number
                    </label>
                    <div className="flex border border-gray-200 rounded-lg overflow-hidden">
                      <div className="px-3 py-2.5 bg-gray-50 border-r border-gray-200 text-sm text-gray-700 font-medium">
                        +91
                      </div>
                      <input
                        type="tel"
                        className="flex-1 px-3 py-2.5 text-sm outline-none"
                        maxLength={10}
                        value={payPhone}
                        onChange={(e) =>
                          setPayPhone(
                            e.target.value.replace(/\D/g, "").slice(0, 10),
                          )
                        }
                      />
                    </div>
                  </div>
                </div>
              )}

              {/* Screen 2: UPI QR */}
              {payScreen === "upi" && (
                <div className="px-5 py-5 flex flex-col items-center gap-3">
                  {/* Mockup disclaimer */}
                  <div className="w-full bg-amber-50 border border-amber-300 rounded-lg px-3 py-2 flex items-start gap-2">
                    <span className="text-amber-500 text-base leading-none mt-0.5">⚠️</span>
                    <p className="text-xs text-amber-700 leading-snug">
                      <span className="font-semibold">Demo only.</span> This UPI interface is a mockup — no real payment will be processed.
                    </p>
                  </div>
                  <p className="text-sm font-semibold text-gray-700">
                    Scan &amp; Pay via UPI
                  </p>
                  <div className="border border-gray-200 rounded-xl p-3 bg-gray-50">
                    <img
                      src={`https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=upi://pay?pa=medicare%40upi%26am=${totalCost.toFixed(2)}%26tn=MediCare+Appointment`}
                      alt="UPI QR Code"
                      className="w-44 h-44"
                    />
                  </div>
                  <p className="text-xs text-gray-400 text-center">
                    Scan with GPay, PhonePe, PayTm or any UPI app
                  </p>
                </div>
              )}
            </div>

            {/* Footer */}
            <div className="bg-white border-t border-gray-100 px-5 py-4">
              {payScreen === "contact" ? (
                <button
                  onClick={() => setPayScreen("upi")}
                  disabled={payPhone.length < 10}
                  className="w-full bg-[#2563EB] hover:bg-blue-700 disabled:opacity-50 text-white font-bold py-3 rounded-xl transition text-sm"
                >
                  Go
                </button>
              ) : (
                <button
                  onClick={handleSubmit}
                  disabled={submitting}
                  className="w-full bg-[#2563EB] hover:bg-blue-700 disabled:opacity-50 text-white font-bold py-3 rounded-xl transition text-sm flex items-center justify-center gap-2"
                >
                  {submitting ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin" /> Processing…
                    </>
                  ) : (
                    "Pay Now"
                  )}
                </button>
              )}
              <p className="text-center text-[10px] text-gray-400 mt-3 flex items-center justify-center gap-1">
                <Shield className="w-3 h-3" /> Secured by Razorpay
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
