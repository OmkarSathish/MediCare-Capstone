import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Search, MapPin, ChevronRight, Building2, Loader2 } from "lucide-react";
import { centersApi } from "../api/centers";
import type { CenterSearchResponse } from "../types";

export default function CentersPage() {
  const [centers, setCenters] = useState<CenterSearchResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState("");
  const [error, setError] = useState("");

  const fetchCenters = async (search?: string) => {
    setLoading(true);
    try {
      const res = await centersApi.list(search || undefined);
      setCenters(res.data.data ?? []);
    } catch {
      setError("Failed to load centers.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCenters();
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchCenters(query);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">
          Diagnostic Centers
        </h1>
        <p className="text-gray-500 mt-2">
          Browse certified diagnostic centers and find the right one for your
          needs.
        </p>
      </div>

      {/* Search bar */}
      <form onSubmit={handleSearch} className="mb-8 flex gap-3 max-w-xl">
        <div className="relative flex-1">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            className="input-field pl-10"
            placeholder="Search by center name…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>
        <button type="submit" className="btn-primary">
          Search
        </button>
      </form>

      {error && (
        <div className="mb-6 bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm">
          {error}
        </div>
      )}

      {loading ? (
        <div className="flex items-center justify-center py-24">
          <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
        </div>
      ) : centers.length === 0 ? (
        <div className="text-center py-20">
          <Building2 className="w-14 h-14 text-gray-200 mx-auto mb-4" />
          <p className="text-gray-500 text-lg">No centers found.</p>
          {query && (
            <button
              onClick={() => {
                setQuery("");
                fetchCenters();
              }}
              className="mt-4 text-blue-600 text-sm hover:underline"
            >
              Clear search
            </button>
          )}
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {centers.map((center) => (
            <Link
              key={center.id}
              to={`/centers/${center.id}`}
              className="card hover:shadow-md transition-all hover:border-blue-200 group"
            >
              <div className="flex items-start gap-4 mb-4">
                <div className="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center shrink-0 group-hover:bg-blue-600 transition-colors">
                  <Building2 className="w-5 h-5 text-blue-600 group-hover:text-white transition-colors" />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="font-bold text-gray-900 truncate">
                    {center.name}
                  </h3>
                  <div className="flex items-center gap-1 mt-1 text-gray-500 text-sm">
                    <MapPin className="w-3.5 h-3.5 shrink-0" />
                    <span className="truncate">{center.address}</span>
                  </div>
                </div>
              </div>
              <div className="flex items-center justify-between pt-4 border-t border-gray-100">
                <span className="text-xs text-blue-600 font-medium bg-blue-50 px-2.5 py-1 rounded-full">
                  View Tests
                </span>
                <ChevronRight className="w-4 h-4 text-gray-400 group-hover:text-blue-600 transition-colors" />
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
