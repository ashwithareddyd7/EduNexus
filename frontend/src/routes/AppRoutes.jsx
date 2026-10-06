import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "@/routes/ProtectedRoute";
import AppLayout from "@/components/layout/AppLayout";
import LoginPage from "@/pages/LoginPage";
import RegisterPage from "@/pages/RegisterPage";
import DashboardPage from "@/pages/DashboardPage";
import ProfilePage from "@/pages/ProfilePage";
import AcademicRecordsPage from "@/pages/AcademicRecordsPage";
import DocumentsPage from "@/pages/DocumentsPage";
import StaffDocumentsPage from "@/pages/StaffDocumentsPage";
import PlaceholderPage from "@/pages/PlaceholderPage";
import NotFoundPage from "@/pages/NotFoundPage";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Everything below needs a logged-in user and gets the sidebar layout */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />

          <Route element={<ProtectedRoute allowedRoles={["STUDENT"]} />}>
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/academics" element={<AcademicRecordsPage />} />
            <Route path="/documents" element={<DocumentsPage />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={["HOD", "ADMIN"]} />}>
            <Route path="/students" element={<PlaceholderPage title="Students" />} />
            <Route path="/staff/documents" element={<StaffDocumentsPage />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
            <Route path="/admin/accounts" element={<PlaceholderPage title="Account Status" />} />
          </Route>
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}