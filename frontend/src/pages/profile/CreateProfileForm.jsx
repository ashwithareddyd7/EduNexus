import { useEffect, useState } from "react";
import { getCourses } from "@/api/courses";
import { createMyProfile } from "@/api/students";
import { getErrorMessage } from "@/api/client";
import Alert from "@/components/ui/Alert";
import Button from "@/components/ui/Button";
import SelectField from "@/components/ui/SelectField";
import TextField from "@/components/ui/TextField";

function validate(form) {
  if (!form.studentId.trim()) return "Please enter your student ID.";
  if (form.studentId.trim().length > 30) return "Student ID can be at most 30 characters.";
  if (!form.fullName.trim()) return "Please enter your full name.";
  if (form.fullName.trim().length > 100) return "Full name can be at most 100 characters.";
  if (form.phone.trim().length > 20) return "Phone can be at most 20 characters.";
  if (!form.courseId) return "Please choose your course.";
  return "";
}

export default function CreateProfileForm({ onCreated }) {
  const [courses, setCourses] = useState([]);
  const [coursesLoading, setCoursesLoading] = useState(true);
  const [form, setForm] = useState({
    studentId: "",
    fullName: "",
    phone: "",
    courseId: "",
    currentSemester: "1",
  });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let cancelled = false;
    getCourses()
      .then((list) => {
        if (!cancelled) setCourses(list);
      })
      .catch((err) => {
        if (!cancelled) setError(getErrorMessage(err));
      })
      .finally(() => {
        if (!cancelled) setCoursesLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const selectedCourse = courses.find((c) => String(c.id) === form.courseId);
  const semesterCount = selectedCourse?.totalSemesters ?? 8;

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((previous) => {
      const next = { ...previous, [name]: value };
      if (name === "courseId") {
        const course = courses.find((c) => String(c.id) === value);
        if (course && Number(next.currentSemester) > course.totalSemesters) {
          next.currentSemester = "1";
        }
      }
      return next;
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const message = validate(form);
    if (message) {
      setError(message);
      return;
    }

    setError("");
    setSubmitting(true);
    try {
      await createMyProfile({
        studentId: form.studentId.trim(),
        fullName: form.fullName.trim(),
        phone: form.phone.trim() || null,
        courseId: Number(form.courseId),
        currentSemester: Number(form.currentSemester),
      });
      await onCreated();
    } catch (err) {
      setError(getErrorMessage(err));
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg rounded-2xl bg-white p-8 shadow">
      <h1 className="text-2xl font-bold text-slate-900">Create your profile</h1>
      <p className="mt-1 text-slate-600">
        Add your details once. Your results and documents are linked to this
        profile.
      </p>

      {error && (
        <div className="mt-6">
          <Alert>{error}</Alert>
        </div>
      )}

      <form onSubmit={handleSubmit} className="mt-6 space-y-5">
        <TextField
          id="studentId"
          label="Student ID"
          type="text"
          required
          maxLength={30}
          value={form.studentId}
          onChange={handleChange}
        />
        <TextField
          id="fullName"
          label="Full name"
          type="text"
          autoComplete="name"
          required
          maxLength={100}
          value={form.fullName}
          onChange={handleChange}
        />
        <TextField
          id="phone"
          label="Phone (optional)"
          type="tel"
          autoComplete="tel"
          maxLength={20}
          value={form.phone}
          onChange={handleChange}
        />
        <SelectField
          id="courseId"
          label="Course"
          required
          disabled={coursesLoading}
          value={form.courseId}
          onChange={handleChange}
        >
          <option value="">
            {coursesLoading ? "Loading courses..." : "Select your course"}
          </option>
          {courses.map((course) => (
            <option key={course.id} value={course.id}>
              {course.name} ({course.departmentName})
            </option>
          ))}
        </SelectField>
        <SelectField
          id="currentSemester"
          label="Current semester"
          required
          value={form.currentSemester}
          onChange={handleChange}
        >
          {Array.from({ length: semesterCount }, (_, index) => index + 1).map(
            (number) => (
              <option key={number} value={number}>
                Semester {number}
              </option>
            )
          )}
        </SelectField>

        <Button type="submit" disabled={submitting} className="w-full">
          {submitting ? "Saving..." : "Create profile"}
        </Button>
      </form>
    </div>
  );
}
