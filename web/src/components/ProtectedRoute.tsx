import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import type { ReactNode } from "react";
import { Loader2 } from "lucide-react";

export function ProtectedRoute({
  children,
  adminOnly = false,
  primaryAdminOnly = false,
  patientOnly = false,
}: {
  children: ReactNode;
  adminOnly?: boolean;
  primaryAdminOnly?: boolean;
  patientOnly?: boolean;
}) {
  const { user, loading, isAdmin, isCenterAdmin } = useAuth();

  if (loading)
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );

  if (!user) return <Navigate to="/login" replace />;
  if (adminOnly && !isAdmin && !isCenterAdmin)
    return <Navigate to="/dashboard" replace />;
  if (primaryAdminOnly && !isAdmin) return <Navigate to="/admin" replace />;
  if (patientOnly && (isAdmin || isCenterAdmin))
    return <Navigate to="/admin" replace />;
  return <>{children}</>;
}
