import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff, Activity, Loader2 } from "lucide-react";
import { authApi } from "../api/auth";
import { useAuth } from "../context/AuthContext";
import { useTitle } from "../hooks/useTitle";

export default function SignupPage() {
  useTitle("Sign Up");
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
  });
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [phoneError, setPhoneError] = useState("");

  const PHONE_RE = /^(\+91[\-\s]?|0)?[6-9]\d{9}$/;

  const handleChange =
    (field: string) => (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setPhoneError("");

    if (form.phone && !PHONE_RE.test(form.phone)) {
      setPhoneError(
        "Enter a valid 10-digit Indian mobile number (e.g. 98765 43210 or +91 98765 43210).",
      );
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    if (form.password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }

    setLoading(true);
    try {
      await authApi.signup({
        fullName: form.fullName,
        email: form.email,
        phone: form.phone || undefined,
        password: form.password,
        role: "CUSTOMER",
      });
      await login(form.email, form.password);
      navigate(`/profile/${form.email}`, { replace: true });
    } catch (err: any) {
      const fieldErrors = err?.response?.data?.fieldErrors;
      if (fieldErrors?.length) {
        setError(fieldErrors.join(", "));
      } else {
        setError(
          err?.response?.data?.message ??
            "Registration failed. Please try again.",
        );
      }
    } finally {
      setLoading(false);
    }
  };

  if (false) {
    // removed success screen
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center gap-2 mb-4">
            <div className="bg-blue-600 rounded-xl p-2">
              <Activity className="w-6 h-6 text-white" />
            </div>
            <span className="text-2xl font-bold text-gray-900">MediCare</span>
          </Link>
          <h1 className="text-2xl font-extrabold text-gray-900">
            Create your account
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            Start your healthcare journey today
          </p>
        </div>

        <div className="card shadow-xl border-0">
          {error && (
            <div className="mb-5 bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Full Name
              </label>
              <input
                type="text"
                className="input-field"
                placeholder="John Doe"
                value={form.fullName}
                onChange={handleChange("fullName")}
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Email
              </label>
              <input
                type="email"
                className="input-field"
                placeholder="you@example.com"
                value={form.email}
                onChange={handleChange("email")}
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Phone (optional)
              </label>
              <input
                type="tel"
                className={`input-field ${phoneError ? "border-red-400 focus:ring-red-300" : ""}`}
                placeholder="+91 98765 43210"
                value={form.phone}
                onChange={(e) => {
                  const val = e.target.value.replace(/[^0-9+\s\-]/g, "");
                  setForm({ ...form, phone: val });
                  setPhoneError("");
                }}
              />
              {phoneError && (
                <p className="text-xs text-red-500 mt-1">{phoneError}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Password
              </label>
              <div className="relative">
                <input
                  type={showPwd ? "text" : "password"}
                  className="input-field pr-10"
                  placeholder="Min. 8 characters"
                  value={form.password}
                  onChange={handleChange("password")}
                  required
                  minLength={8}
                />
                <button
                  type="button"
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  onClick={() => setShowPwd(!showPwd)}
                >
                  {showPwd ? (
                    <EyeOff className="w-4 h-4" />
                  ) : (
                    <Eye className="w-4 h-4" />
                  )}
                </button>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Confirm Password
              </label>
              <input
                type={showPwd ? "text" : "password"}
                className="input-field"
                placeholder="Re-enter password"
                value={form.confirmPassword}
                onChange={handleChange("confirmPassword")}
                required
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full flex items-center justify-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed mt-2"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" /> Creating Account…
                </>
              ) : (
                "Create Account"
              )}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-gray-500">
            Already have an account?{" "}
            <Link
              to="/login"
              className="text-blue-600 font-semibold hover:underline"
            >
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
