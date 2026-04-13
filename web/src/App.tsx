import { BrowserRouter, Routes, Route, Outlet } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ProtectedRoute } from "./components/ProtectedRoute";
import Navbar from "./components/Navbar";
import Footer from "./components/Footer";

// Pages
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import DashboardPage from "./pages/DashboardPage";
import CentersPage from "./pages/CentersPage";
import CenterDetailPage from "./pages/CenterDetailPage";
import TestsPage from "./pages/TestsPage";
import BookAppointmentPage from "./pages/BookAppointmentPage";
import AppointmentsPage from "./pages/AppointmentsPage";
import AppointmentDetailPage from "./pages/AppointmentDetailPage";
import ProfilePage from "./pages/ProfilePage";
import AdminDashboardPage from "./pages/AdminDashboardPage";
import AdminAppointmentsPage from "./pages/AdminAppointmentsPage";
import AdminCentersPage from "./pages/AdminCentersPage";
import AdminTestsPage from "./pages/AdminTestsPage";
import AdminCenterAdminsPage from "./pages/AdminCenterAdminsPage";
import AdminStaffPage from "./pages/AdminStaffPage";

function Layout() {
  return (
    <div className="flex flex-col min-h-screen">
      <Navbar />
      <main className="flex-1">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}

function AuthLayout() {
  return <Outlet />;
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<AuthLayout />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
          </Route>

          <Route element={<Layout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/centers" element={<CentersPage />} />
            <Route path="/centers/:id" element={<CenterDetailPage />} />
            <Route path="/tests" element={<TestsPage />} />

            <Route
              path="/dashboard"
              element={
                <ProtectedRoute patientOnly>
                  <DashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/book"
              element={
                <ProtectedRoute patientOnly>
                  <BookAppointmentPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/appointments"
              element={
                <ProtectedRoute>
                  <AppointmentsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/appointments/:id"
              element={
                <ProtectedRoute>
                  <AppointmentDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile/:username"
              element={
                <ProtectedRoute>
                  <ProfilePage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/admin"
              element={
                <ProtectedRoute adminOnly>
                  <AdminDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/appointments"
              element={
                <ProtectedRoute centerAdminOnly>
                  <AdminAppointmentsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/centers"
              element={
                <ProtectedRoute primaryAdminOnly>
                  <AdminCentersPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/tests"
              element={
                <ProtectedRoute adminOnly>
                  <AdminTestsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/center-admins"
              element={
                <ProtectedRoute primaryAdminOnly>
                  <AdminCenterAdminsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/staff"
              element={
                <ProtectedRoute mainCenterAdminOnly>
                  <AdminStaffPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="*"
              element={
                <div className="flex flex-col items-center justify-center min-h-96 gap-4">
                  <h1 className="text-6xl font-extrabold text-gray-200">404</h1>
                  <p className="text-gray-500">Page not found.</p>
                  <a href="/" className="btn-primary">
                    Go Home
                  </a>
                </div>
              }
            />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
