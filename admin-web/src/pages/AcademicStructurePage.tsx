import { useState } from 'react';
import { StatusBadge } from '../components/StatusBadge';

export default function AcademicStructurePage() {
  const [selectedCourse, setSelectedCourse] = useState('B.Tech Computer Science');

  const courses = [
    { name: 'B.Tech Computer Science', dept: 'CSE Department', semesters: '8 semesters', sections: '3 sections' },
    { name: 'B.Tech Electronics & Comm', dept: 'ECE Department', semesters: '8 semesters', sections: '2 sections' },
    { name: 'B.Sc Data Science', dept: 'Math & Computing', semesters: '6 semesters', sections: '2 sections' },
    { name: 'M.Tech Artificial Intelligence', dept: 'CSE Department', semesters: '4 semesters', sections: '1 section' },
  ];

  const sections = [
    { section: 'Section A', year: '2026-27', students: 40, teacher: 'Dr. Alan Turing', isTeacherSet: true },
    { section: 'Section B', year: '2026-27', students: 38, teacher: 'Prof. Grace Hopper', isTeacherSet: true },
    { section: 'Section C', year: '2026-27', students: 0, teacher: 'Not set', isTeacherSet: false },
  ];

  const subjects = [
    { name: 'Data Structures & Algorithms', credits: '4 credits' },
    { name: 'Operating Systems', credits: '4 credits' },
    { name: 'Database Management Systems', credits: '3 credits' },
    { name: 'Computer Networks', credits: '3 credits' },
    { name: 'Machine Learning Elective', credits: 'Elective' },
  ];

  return (
    <div className="space-y-6">
      {/* Page Title & Action Bar */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-800">Academic structure</h2>
          <p className="text-xs text-slate-500">Configure courses, sections, and curriculum mapping</p>
        </div>
        <button className="px-3 py-1.5 bg-white border border-slate-300 text-slate-700 font-semibold rounded-lg text-xs hover:bg-slate-50 transition">
          Academic years
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: Courses Column (PDF Page 14) */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-slate-800 text-sm">Courses</h3>
            <button className="px-2.5 py-1 bg-indigo-50 text-indigo-600 rounded-md text-xs font-semibold hover:bg-indigo-100 transition">
              + Add
            </button>
          </div>
          <p className="text-xs text-slate-400 font-medium">Academic year 2026–27</p>

          <div className="space-y-2">
            {courses.map((course, idx) => (
              <div
                key={idx}
                onClick={() => setSelectedCourse(course.name)}
                className={`p-3 rounded-lg border cursor-pointer transition ${
                  selectedCourse === course.name
                    ? 'border-indigo-500 bg-indigo-50/40'
                    : 'border-slate-200 hover:border-slate-300 bg-slate-50/50'
                }`}
              >
                <div className="flex justify-between items-start">
                  <span className="font-bold text-sm text-slate-800">{course.name}</span>
                  <span className="text-xs font-medium text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded">
                    {course.sections}
                  </span>
                </div>
                <div className="text-xs text-slate-500 mt-1">
                  {course.dept} · {course.semesters}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Right: Sections and Subjects (PDF Page 14) */}
        <div className="lg:col-span-2 space-y-6">
          {/* Sections Table */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-slate-800 text-sm">Sections: {selectedCourse}, Semester 5</h3>
                <p className="text-xs text-slate-500">Allocated student cohorts and mentors</p>
              </div>
              <button className="px-3 py-1.5 bg-indigo-600 text-white rounded-lg text-xs font-semibold hover:bg-indigo-700 transition">
                + Add section
              </button>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-slate-200 text-slate-500 font-semibold uppercase">
                    <th className="py-2.5">Section</th>
                    <th className="py-2.5">Year</th>
                    <th className="py-2.5">Students</th>
                    <th className="py-2.5">Class teacher</th>
                    <th className="py-2.5 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {sections.map((sec, idx) => (
                    <tr key={idx} className="hover:bg-slate-50/60">
                      <td className="py-3 font-bold text-slate-800">{sec.section}</td>
                      <td className="py-3 text-slate-600">{sec.year}</td>
                      <td className="py-3 font-semibold text-slate-700">{sec.students}</td>
                      <td className="py-3">
                        {sec.isTeacherSet ? (
                          <span className="text-slate-800 font-medium">{sec.teacher}</span>
                        ) : (
                          <StatusBadge type="attention" label="Not set" />
                        )}
                      </td>
                      <td className="py-3 text-right">
                        <button className="px-2.5 py-1 bg-white border border-slate-300 rounded text-slate-700 font-medium hover:bg-slate-50">
                          Edit
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Subjects in this Semester */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-slate-800 text-sm">Subjects in this semester</h3>
              <button className="px-3 py-1.5 bg-white border border-slate-300 text-slate-700 rounded-lg text-xs font-semibold hover:bg-slate-50 transition">
                Map subject
              </button>
            </div>

            <div className="flex flex-wrap gap-2.5">
              {subjects.map((sub, idx) => (
                <div
                  key={idx}
                  className="flex items-center space-x-2 px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
                >
                  <span className="font-semibold text-slate-800">{sub.name}</span>
                  <span className="text-slate-400">·</span>
                  <span className="text-indigo-600 font-medium">{sub.credits}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
