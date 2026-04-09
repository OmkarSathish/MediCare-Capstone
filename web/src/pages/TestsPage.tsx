import { useEffect, useState } from "react";
import { Search, Microscope, Tag, Loader2 } from "lucide-react";
import { testsApi } from "../api/tests";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import type { TestResponse } from "../types";

export default function TestsPage() {
  const { isAdmin } = useAuth();
  const [tests, setTests] = useState<TestResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState("");

  const fetchTests = async (search?: string) => {
    setLoading(true);
    try {
      const res = await testsApi.list(search || undefined);
      setTests(res.data.data ?? []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTests();
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchTests(query);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">
          Diagnostic Tests
        </h1>
        <p className="text-gray-500 mt-2">
          Explore our wide range of diagnostic tests available across our
          network of centers.
        </p>
      </div>

      <form onSubmit={handleSearch} className="mb-8 flex gap-3 max-w-xl">
        <div className="relative flex-1">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            className="input-field pl-10"
            placeholder="Search tests…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>
        <button type="submit" className="btn-primary">
          Search
        </button>
      </form>

      {loading ? (
        <div className="flex items-center justify-center py-24">
          <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
        </div>
      ) : tests.length === 0 ? (
        <div className="text-center py-20">
          <Microscope className="w-14 h-14 text-gray-200 mx-auto mb-4" />
          <p className="text-gray-500">No tests found.</p>
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {tests.map((test) => (
            <div
              key={test.id}
              className="card hover:shadow-md transition-all hover:border-blue-200 group"
            >
              <div className="flex items-start gap-3 mb-4">
                <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center shrink-0 group-hover:bg-blue-600 transition-colors">
                  <Microscope className="w-4 h-4 text-blue-600 group-hover:text-white transition-colors" />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="font-bold text-gray-900 truncate">
                    {test.testName}
                  </h3>
                  {test.categoryName && (
                    <span className="inline-flex items-center gap-1 text-xs text-gray-400 mt-0.5">
                      <Tag className="w-3 h-3" /> {test.categoryName}
                    </span>
                  )}
                </div>
              </div>

              <div className="space-y-2 text-sm text-gray-600">
                {test.normalValue && (
                  <div className="flex justify-between">
                    <span className="text-gray-400">Normal range</span>
                    <span className="font-medium">
                      {test.normalValue} {test.units}
                    </span>
                  </div>
                )}
                <div className="flex justify-between pt-2 border-t border-gray-100">
                  <span className="text-gray-400">Price</span>
                  <span className="text-blue-600 font-bold">
                    ₹{test.testPrice.toFixed(2)}
                  </span>
                </div>
              </div>

              {!isAdmin && (
                <Link
                  to={`/book?testId=${test.id}`}
                  className="mt-4 block text-center text-sm font-semibold text-blue-600 bg-blue-50 hover:bg-blue-600 hover:text-white rounded-xl py-2 transition-colors"
                >
                  Book This Test
                </Link>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
