import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Activity,
  ArrowLeft,
  CheckCircle2,
  Loader2,
  Eye,
  EyeOff,
} from "lucide-react";
import { useTitle } from "../hooks/useTitle";
import { authApi } from "../api/auth";

type Step = "email" | "otp" | "done";

export default function ForgotPasswordPage() {
  useTitle("Forgot Password");
  const navigate = useNavigate();

  const [step, setStep] = useState<Step>("email");
  const [email, setEmail] = useState("");
  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // ── Step 1: send OTP ──────────────────────────────────────────────────────
  const handleSendOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await authApi.forgotPassword(email);
      setStep("otp");
    } catch (err: any) {
      setError(
        err?.response?.data?.message ?? "Failed to send OTP. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  // ── Step 2: verify OTP + reset password ──────────────────────────────────
  const handleReset = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    setLoading(true);
    try {
      await authApi.resetPassword(email, otp, newPassword);
      setStep("done");
    } catch (err: any) {
      setError(
        err?.response?.data?.message ??
          "Failed to reset password. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center gap-2 mb-4">
            <div className="bg-blue-600 rounded-xl p-2">
              <Activity className="w-6 h-6 text-white" />
            </div>
            <span className="text-2xl font-bold text-gray-900">MediCare</span>
          </Link>
          <h1 className="text-2xl font-extrabold text-gray-900">
            {step === "done" ? "Password Reset!" : "Forgot Password"}
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            {step === "email" &&
              "Enter your email to receive a one-time password."}
            {step === "otp" && `We sent a 6-digit OTP to ${email}.`}
            {step === "done" &&
              "Your password has been updated. You can now sign in."}
          </p>
        </div>

        <div className="card shadow-xl border-0">
          {/* ── Success state ── */}
          {step === "done" && (
            <div className="text-center py-4">
              <div className="flex justify-center mb-4">
                <div className="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center">
                  <CheckCircle2 className="w-7 h-7 text-green-600" />
                </div>
              </div>
              <p className="text-gray-600 text-sm mb-6">
                Your password has been reset successfully.
              </p>
              <button
                onClick={() => navigate("/login")}
                className="btn-primary w-full"
              >
                Back to Login
              </button>
            </div>
          )}

          {/* ── Step 1: Email ── */}
          {step === "email" && (
            <form onSubmit={handleSendOtp} className="space-y-5">
              {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm">
                  {error}
                </div>
              )}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Email address
                </label>
                <input
                  type="email"
                  className="input-field"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  autoFocus
                />
              </div>
              <button
                type="submit"
                disabled={loading}
                className="btn-primary w-full flex items-center justify-center gap-2"
              >
                {loading && <Loader2 className="w-4 h-4 animate-spin" />}
                Send OTP
              </button>
              <div className="text-center">
                <Link
                  to="/login"
                  className="inline-flex items-center gap-1 text-sm text-blue-600 hover:underline"
                >
                  <ArrowLeft className="w-3.5 h-3.5" /> Back to Login
                </Link>
              </div>
            </form>
          )}

          {/* ── Step 2: OTP + New Password ── */}
          {step === "otp" && (
            <form onSubmit={handleReset} className="space-y-5">
              {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm">
                  {error}
                </div>
              )}

              {/* OTP input */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  One-Time Password (OTP)
                </label>
                <input
                  type="text"
                  inputMode="numeric"
                  maxLength={6}
                  className="input-field tracking-[0.4em] text-center text-lg font-semibold"
                  placeholder="------"
                  value={otp}
                  onChange={(e) =>
                    setOtp(e.target.value.replace(/\D/g, "").slice(0, 6))
                  }
                  required
                  autoFocus
                />
                <p className="text-xs text-gray-400 mt-1.5">
                  Enter the 6-digit code sent to {email}.
                </p>
              </div>

              {/* New password */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  New Password
                </label>
                <div className="relative">
                  <input
                    type={showPwd ? "text" : "password"}
                    className="input-field pr-10"
                    placeholder="••••••••"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                    minLength={8}
                    autoComplete="new-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPwd(!showPwd)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showPwd ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              {/* Confirm password */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Confirm Password
                </label>
                <input
                  type={showPwd ? "text" : "password"}
                  className="input-field"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  autoComplete="new-password"
                />
              </div>

              <button
                type="submit"
                disabled={loading || otp.length < 6}
                className="btn-primary w-full flex items-center justify-center gap-2 disabled:opacity-60"
              >
                {loading && <Loader2 className="w-4 h-4 animate-spin" />}
                Reset Password
              </button>

              <div className="flex items-center justify-between text-sm">
                <button
                  type="button"
                  onClick={() => {
                    setStep("email");
                    setError("");
                    setOtp("");
                  }}
                  className="inline-flex items-center gap-1 text-blue-600 hover:underline"
                >
                  <ArrowLeft className="w-3.5 h-3.5" /> Change email
                </button>
                <button
                  type="button"
                  disabled={loading}
                  onClick={() => {
                    setError("");
                    handleSendOtp({ preventDefault: () => {} } as any);
                  }}
                  className="text-gray-500 hover:text-blue-600 hover:underline"
                >
                  Resend OTP
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
