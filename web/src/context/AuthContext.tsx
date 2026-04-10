import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { authApi } from "../api/auth";
import type { UserProfileResponse } from "../types";

interface AuthContextValue {
  user: UserProfileResponse | null;
  loading: boolean;
  isAdmin: boolean;
  isCenterAdmin: boolean;
  adminCenterId: number | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refetch: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchMe = async () => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      setLoading(false);
      return;
    }
    try {
      const res = await authApi.me();
      setUser(res.data.data ?? null);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMe();
  }, []);

  const login = async (username: string, password: string) => {
    const res = await authApi.login({ username, password });
    const tokens = res.data.data!;
    localStorage.setItem("accessToken", tokens.accessToken);
    localStorage.setItem("refreshToken", tokens.refreshToken);
    await fetchMe();
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem("refreshToken");
    if (refreshToken) {
      try {
        await authApi.logout(refreshToken);
      } catch {
        /* ignore */
      }
    }
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    setUser(null);
  };

  const isAdmin = user?.roles?.includes("ADMIN") ?? false;
  const isCenterAdmin = user?.roles?.includes("CENTER_ADMIN") ?? false;
  const adminCenterId = user?.centerId ?? null;

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAdmin,
        isCenterAdmin,
        adminCenterId,
        login,
        logout,
        refetch: fetchMe,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
