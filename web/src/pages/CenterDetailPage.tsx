import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { useTitle } from "../hooks/useTitle";
import {
  Building2,
  MapPin,
  Phone,
  Mail,
  Tag,
  Loader2,
  ArrowLeft,
  CheckCircle,
  Calendar,
  Microscope,
} from "lucide-react";
import { centersApi } from "../api/centers";
import { useAuth } from "../context/AuthContext";
import type { CenterResponse } from "../types";

export default function CenterDetailPage() {
  const { isAdmin } = useAuth();
  const { id } = useParams<{ id: string }>();
  const [center, setCenter] = useState<CenterResponse | null>(null);
  useTitle(center?.name ?? "Center Details");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!id) return;
    centersApi
      .getById(Number(id))
      .then((r) => setCenter(r.data.data ?? null))
      .catch(() => setError("Center not found."))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading)
    return (
      <div className="flex items-center justify-center min-h-96">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );

  if (error || !center)
    return (
      <div className="max-w-7xl mx-auto px-4 py-16 text-center">
        <p className="text-red-500">{error || "Center not found."}</p>
        <Link
          to={isAdmin ? "/admin/centers" : "/centers"}
          className="btn-primary inline-flex mt-4"
        >
          Back to Centers
        </Link>
      </div>
    );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <Link
        to={isAdmin ? "/admin/centers" : "/centers"}
        className="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-blue-600 mb-6 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" /> Back to Centers
      </Link>

      {/* Header card */}
      <div className="card mb-6">
        <div className="flex flex-col sm:flex-row sm:items-start gap-6">
          <div className="w-16 h-16 bg-blue-100 rounded-2xl flex items-center justify-center shrink-0">
            <Building2 className="w-8 h-8 text-blue-600" />
          </div>
          <div className="flex-1">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div>
                <h1 className="text-2xl font-extrabold text-gray-900">
                  {center.name}
                </h1>
                <div className="flex flex-wrap gap-4 mt-2 text-sm text-gray-500">
                  {center.address && (
                    <span className="flex items-center gap-1.5">
                      <MapPin className="w-4 h-4 text-blue-400" />{" "}
                      {center.address}
                    </span>
                  )}
                  {center.contactNo && (
                    <span className="flex items-center gap-1.5">
                      <Phone className="w-4 h-4 text-blue-400" />{" "}
                      {center.contactNo}
                    </span>
                  )}
                  {center.contactEmail && (
                    <span className="flex items-center gap-1.5">
                      <Mail className="w-4 h-4 text-blue-400" />{" "}
                      {center.contactEmail}
                    </span>
                  )}
                </div>
              </div>
              {!isAdmin && (
                <Link
                  to={`/book?centerId=${center.id}`}
                  className="btn-primary inline-flex items-center gap-2 shrink-0"
                >
                  <Calendar className="w-4 h-4" /> Book Appointment
                </Link>
              )}
            </div>

            {/* Services */}
            {center.servicesOffered?.length > 0 && (
              <div className="mt-4 flex flex-wrap gap-2">
                {center.servicesOffered.map((s) => (
                  <span
                    key={s}
                    className="inline-flex items-center gap-1 text-xs bg-blue-50 text-blue-700 border border-blue-100 rounded-full px-3 py-1"
                  >
                    <Tag className="w-3 h-3" /> {s}
                  </span>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Tests */}
      <div className="card">
        <h2 className="text-lg font-bold text-gray-900 mb-5 flex items-center gap-2">
          <Microscope className="w-5 h-5 text-blue-600" /> Available Tests
          <span className="ml-auto text-sm font-normal text-gray-400">
            {center.tests?.length ?? 0} tests
          </span>
        </h2>

        {!center.tests?.length ? (
          <p className="text-gray-400 text-sm py-6 text-center">
            No tests listed for this center yet.
          </p>
        ) : (
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {center.tests.map((test) => (
              <div
                key={test.id}
                className="flex items-center gap-3 p-4 bg-gray-50 rounded-xl hover:bg-blue-50 transition-colors"
              >
                <CheckCircle className="w-4 h-4 text-green-500 shrink-0" />
                <div className="flex-1 min-w-0">
                  <p className="font-medium text-gray-900 text-sm truncate">
                    {test.testName}
                  </p>
                  <p className="text-blue-600 font-semibold text-sm">
                    ₹{test.testPrice.toFixed(2)}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
