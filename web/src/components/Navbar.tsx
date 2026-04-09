import { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import {
  Menu,
  X,
  Activity,
  ChevronDown,
  LogOut,
  User,
  LayoutDashboard,
  Calendar,
  Building2,
  FlaskConical,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [dropOpen, setDropOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate("/");
  };

  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    `text-sm font-medium transition-colors ${isActive ? "text-blue-600" : "text-gray-600 hover:text-blue-600"}`;

  return (
    <nav className="bg-white border-b border-gray-100 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2">
            <div className="bg-blue-600 rounded-xl p-1.5">
              <Activity className="w-5 h-5 text-white" />
            </div>
            <span className="font-bold text-lg text-gray-900">MediCare</span>
          </Link>

          {/* Desktop Nav */}
          <div className="hidden md:flex items-center gap-8">
            {isAdmin ? (
              <>
                <NavLink to="/admin" end className={navLinkClass}>Dashboard</NavLink>
                <NavLink to="/admin/appointments" className={navLinkClass}>Appointments</NavLink>
                <NavLink to="/admin/centers" className={navLinkClass}>Centers</NavLink>
                <NavLink to="/admin/tests" className={navLinkClass}>Tests</NavLink>
              </>
            ) : (
              <>
                <NavLink to="/" end className={navLinkClass}>Home</NavLink>
                <NavLink to="/centers" className={navLinkClass}>Centers</NavLink>
                <NavLink to="/tests" className={navLinkClass}>Tests</NavLink>
                {user && (
                  <NavLink to="/appointments" className={navLinkClass}>Appointments</NavLink>
                )}
              </>
            )}
          </div>

          {/* Desktop Auth */}
          <div className="hidden md:flex items-center gap-3">
            {user ? (
              <div className="relative">
                <button
                  onClick={() => setDropOpen(!dropOpen)}
                  className="flex items-center gap-2 bg-gray-50 hover:bg-blue-50 border border-gray-200 rounded-full px-4 py-2 text-sm font-medium text-gray-700 transition-colors"
                >
                  <div className="w-6 h-6 bg-blue-600 rounded-full flex items-center justify-center">
                    <span className="text-white text-xs font-bold">
                      {user.fullName.charAt(0).toUpperCase()}
                    </span>
                  </div>
                  <span>{user.fullName.split(" ")[0]}</span>
                  <ChevronDown className="w-4 h-4" />
                </button>
                {dropOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-lg border border-gray-100 py-1 z-50">
                    {isAdmin ? (
                      <>
                        <Link to="/admin" className="flex items-center gap-2 px-4 py-2.5 text-sm text-gray-700 hover:bg-blue-50 hover:text-blue-600" onClick={() => setDropOpen(false)}>
                          <LayoutDashboard className="w-4 h-4" /> Dashboard
                        </Link>
                        <Link to="/admin/appointments" className="flex items-center gap-2 px-4 py-2.5 text-sm text-gray-700 hover:bg-blue-50 hover:text-blue-600" onClick={() => setDropOpen(false)}>
                          <Calendar className="w-4 h-4" /> Appointments
                        </Link>
                        <Link to="/admin/centers" className="flex items-center gap-2 px-4 py-2.5 text-sm text-gray-700 hover:bg-blue-50 hover:text-blue-600" onClick={() => setDropOpen(false)}>
                          <Building2 className="w-4 h-4" /> Centers
                        </Link>
                        <Link to="/admin/tests" className="flex items-center gap-2 px-4 py-2.5 text-sm text-gray-700 hover:bg-blue-50 hover:text-blue-600" onClick={() => setDropOpen(false)}>
                          <FlaskConical className="w-4 h-4" /> Tests
                        </Link>
                      </>
                    ) : (
                      <>
                        <Link to={`/profile/${user.email}`} className="flex items-center gap-2 px-4 py-2.5 text-sm text-gray-700 hover:bg-blue-50 hover:text-blue-600" onClick={() => setDropOpen(false)}>
                          <User className="w-4 h-4" /> My Profile
                        </Link>
                        <Link to="/appointments" className="flex items-center gap-2 px-4 py-2.5 text-sm text-gray-700 hover:bg-blue-50 hover:text-blue-600" onClick={() => setDropOpen(false)}>
                          <Calendar className="w-4 h-4" /> Appointments
                        </Link>
                      </>
                    )}
                    <hr className="my-1 border-gray-100" />
                    <button
                      onClick={() => {
                        setDropOpen(false);
                        handleLogout();
                      }}
                      className="flex items-center gap-2 w-full px-4 py-2.5 text-sm text-red-600 hover:bg-red-50"
                    >
                      <LogOut className="w-4 h-4" /> Sign Out
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <>
                <Link
                  to="/login"
                  className="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors"
                >
                  Login
                </Link>
                <Link to="/signup" className="btn-primary text-sm">
                  Sign Up
                </Link>
              </>
            )}
          </div>

          {/* Mobile toggle */}
          <button
            className="md:hidden p-2 text-gray-600"
            onClick={() => setMobileOpen(!mobileOpen)}
          >
            {mobileOpen ? (
              <X className="w-5 h-5" />
            ) : (
              <Menu className="w-5 h-5" />
            )}
          </button>
        </div>
      </div>

      {/* Mobile Menu */}
      {mobileOpen && (
        <div className="md:hidden border-t border-gray-100 bg-white px-4 py-4 space-y-3">
          {isAdmin ? (
            <>
              <NavLink to="/admin" end className={navLinkClass} onClick={() => setMobileOpen(false)}>Dashboard</NavLink>
              <NavLink to="/admin/appointments" className={navLinkClass} onClick={() => setMobileOpen(false)}>Appointments</NavLink>
              <NavLink to="/admin/centers" className={navLinkClass} onClick={() => setMobileOpen(false)}>Centers</NavLink>
              <NavLink to="/admin/tests" className={navLinkClass} onClick={() => setMobileOpen(false)}>Tests</NavLink>
            </>
          ) : (
            <>
              <NavLink to="/" end className={navLinkClass} onClick={() => setMobileOpen(false)}>Home</NavLink>
              <NavLink to="/centers" className={navLinkClass} onClick={() => setMobileOpen(false)}>Centers</NavLink>
              <NavLink to="/tests" className={navLinkClass} onClick={() => setMobileOpen(false)}>Tests</NavLink>
              {user && (
                <NavLink to="/appointments" className={navLinkClass} onClick={() => setMobileOpen(false)}>Appointments</NavLink>
              )}
            </>
          )}
          <hr className="border-gray-100" />
          {user ? (
            <button
              onClick={handleLogout}
              className="text-red-600 text-sm font-medium"
            >
              Sign Out
            </button>
          ) : (
            <div className="flex gap-3">
              <Link
                to="/login"
                className="btn-outline text-sm"
                onClick={() => setMobileOpen(false)}
              >
                Login
              </Link>
              <Link
                to="/signup"
                className="btn-primary text-sm"
                onClick={() => setMobileOpen(false)}
              >
                Sign Up
              </Link>
            </div>
          )}
        </div>
      )}
    </nav>
  );
}
