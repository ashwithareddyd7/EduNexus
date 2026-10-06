import useAuth from "@/context/useAuth";
import StudentDashboard from "@/pages/dashboard/StudentDashboard";
import StaffDashboard from "@/pages/dashboard/StaffDashboard";

export default function DashboardPage() {
  const { user } = useAuth();
  return user.role === "STUDENT" ? <StudentDashboard /> : <StaffDashboard />;
}
