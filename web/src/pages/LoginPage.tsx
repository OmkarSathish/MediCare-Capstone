import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff, Activity, Loader2 } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { useTitle } from "../hooks/useTitle";

export default function LoginPage() {
  useTitle("Login");
  const { login, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "", password: "" });
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const DEMO_CREDENTIALS = [
    { role: "Admin", email: "admin@healthcare.com", password: "Admin@1234", color: "bg-purple-100 text-purple-700 hover:bg-purple-200 border-purple-200" },
    { role: "Center Admin", email: "admin.healthfirst@healthcare.ph", password: "admin@1234", color: "bg-blue-100 text-blue-700 hover:bg-blue-200 border-blue-200" },
    { role: "Center Staff", email: "staff1.healthfirst@healthcare.ph", password: "staff@1234", color: "bg-teal-100 text-teal-700 hover:bg-teal-200 border-teal-200" },
    { role: "Patient", email: "juan.santos0@example.com", password: "Patient@1234", color: "bg-green-100 text-green-700 hover:bg-green-200 border-green-200" },
  ] as const;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(form.username, form.password);
      navigate(isAdmin ? "/admin" : "/dashboard");
    } catch (err: any) {
      setError(
        err?.response?.data?.message ??
          "Invalid credentials. Please try again.",
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
            Welcome back
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            Sign in to your account to continue
          </p>
        </div>

        <div className="card shadow-xl border-0">
          {error && (
            <div className="mb-5 bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Email address
              </label>
              <input
                type="text"
                className="input-field"
                placeholder="you@example.com"
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                required
                autoComplete="username"
              />
            </div>

            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="block text-sm font-medium text-gray-700">
                  Password
                </label>
              </div>
              <div className="relative">
                <input
                  type={showPwd ? "text" : "password"}
                  className="input-field pr-10"
                  placeholder="••••••••"
                  value={form.password}
                  onChange={(e) =>
                    setForm({ ...form, password: e.target.value })
                  }
                  required
                  autoComplete="current-password"
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

            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full flex items-center justify-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" /> Signing in...
                </>
              ) : (
                "Sign In"
              )}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-gray-500">
            Don't have an account?{" "}
            <Link
              to="/signup"
              className="text-blue-600 font-semibold hover:underline"
            >
              Create one
            </Link>
          </p>
        </div>

        {/* Demo credentials */}
        <div className="mt-4 card shadow-xl border-0">
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">
            Demo Credentials
          </p>
          <div className="grid grid-cols-2 gap-2">
            {DEMO_CREDENTIALS.map((c) => (
              <button
                key={c.role}
                type="button"
                onClick={() => setForm({ username: c.email, password: c.password })}
                className={`text-left px-3 py-2.5 rounded-xl border text-xs font-medium transition-colors ${c.color}`}
              >
                <span className="block font-semibold">{c.role}</span>
                <span className="block truncate opacity-75">{c.email}</span>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
