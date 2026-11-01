import { useState } from 'react';
import { StatusBadge } from '../components/StatusBadge';

export default function AllocationsTimetablePage() {
  const [selectedSection, setSelectedSection] = useState('Section A');
  const showConflictWarning = true;

  const allocations = [
    { teacher: 'Dr. Alan Turing', section: 'Section A', subject: 'Data Structures', validFrom: '01 Aug 2026', status: 'Active', type: 'good' },
    { teacher: 'Prof. Grace Hopper', section: 'Section B', subject: 'Operating Systems', validFrom: '01 Aug 2026', status: 'Active', type: 'good' },
    { teacher: 'Dr. Edgar Codd', section: 'Section A', subject: 'Database Systems', validFrom: '15 Oct 2026', status: 'Substitute until 15 Nov', type: 'info' },
  ];

  const timetableSlots = [
    { slot: '09:00 - 10:00', mon: 'CS301 (Turing)', tue: 'CS302 (Hopper)', wed: 'CS301 (Turing)', thu: 'CS303 (Codd)', fri: 'CS301 (Turing)' },
    { slot: '10:30 - 11:30', mon: 'CS302 (Hopper)', tue: '—', wed: 'CS303 (Codd)', thu: 'CS302 (Hopper)', fri: '—' },
    { slot: '12:00 - 01:00', mon: 'CS303 (Codd)', tue: 'CS301 (Turing)', wed: '—', thu: 'CS301 (Turing)', fri: 'CS303 (Codd)' },
  ];

  return (
    <div className="space-y-6">
      {/* Page Title */}
      <div>
        <h2 className="text-xl font-bold text-slate-800">Teaching allocations & timetable</h2>
        <p className="text-xs text-slate-500">Manage faculty course assignments and weekly schedule matrices</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Current Allocations & Timetable (PDF Page 15) */}
        <div className="lg:col-span-2 space-y-6">
          {/* Current Allocations Table */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm space-y-4">
            <h3 className="font-bold text-slate-800 text-sm">Current allocations</h3>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-slate-200 text-slate-500 font-semibold uppercase">
                    <th className="py-2.5">Teacher</th>
                    <th className="py-2.5">Section</th>
                    <th className="py-2.5">Subject</th>
                    <th className="py-2.5">Valid from</th>
                    <th className="py-2.5">Status</th>
                    <th className="py-2.5 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {allocations.map((alloc, idx) => (
                    <tr key={idx} className="hover:bg-slate-50/60">
                      <td className="py-3 font-bold text-slate-800">{alloc.teacher}</td>
                      <td className="py-3 font-medium text-slate-600">{alloc.section}</td>
                      <td className="py-3 text-slate-700">{alloc.subject}</td>
                      <td className="py-3 text-slate-500">{alloc.validFrom}</td>
                      <td className="py-3">
                        <StatusBadge type={alloc.type as any} label={alloc.status} />
                      </td>
                      <td className="py-3 text-right">
                        <button className="px-2.5 py-1 bg-white border border-slate-300 rounded text-slate-700 font-medium hover:bg-slate-50">
                          End
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Weekly Timetable Table (PDF Page 15) */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-slate-800 text-sm">Weekly timetable: {selectedSection}</h3>
              <select
                value={selectedSection}
                onChange={(e) => setSelectedSection(e.target.value)}
                className="px-2.5 py-1 border border-slate-300 rounded-lg text-xs bg-white text-slate-700"
              >
                <option value="Section A">Section A</option>
                <option value="Section B">Section B</option>
                <option value="Section C">Section C</option>
              </select>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-center text-xs border border-slate-200">
                <thead>
                  <tr className="bg-slate-50 text-slate-600 font-bold border-b border-slate-200">
                    <th className="py-2.5 px-3 text-left">Slot</th>
                    <th className="py-2.5 px-3">Mon</th>
                    <th className="py-2.5 px-3">Tue</th>
                    <th className="py-2.5 px-3">Wed</th>
                    <th className="py-2.5 px-3">Thu</th>
                    <th className="py-2.5 px-3">Fri</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {timetableSlots.map((row, idx) => (
                    <tr key={idx} className="hover:bg-slate-50/50">
                      <td className="py-3 px-3 text-left font-bold text-slate-700">{row.slot}</td>
                      <td className="py-3 px-3 font-medium text-slate-800 bg-indigo-50/20">{row.mon}</td>
                      <td className="py-3 px-3 font-medium text-slate-800">{row.tue}</td>
                      <td className="py-3 px-3 font-medium text-slate-800 bg-indigo-50/20">{row.wed}</td>
                      <td className="py-3 px-3 font-medium text-slate-800">{row.thu}</td>
                      <td className="py-3 px-3 font-medium text-slate-800 bg-indigo-50/20">{row.fri}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Right Col: Allocate Teacher Drawer (PDF Page 15) */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm space-y-4 h-fit">
          <h3 className="font-bold text-slate-800 text-sm">Allocate teacher</h3>

          <div className="space-y-3">
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Teacher</label>
              <select className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white text-slate-800 focus:outline-none focus:border-indigo-500">
                <option>Dr. Alan Turing</option>
                <option>Prof. Grace Hopper</option>
                <option>Dr. Edgar Codd</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Section</label>
              <select className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white text-slate-800 focus:outline-none focus:border-indigo-500">
                <option>Section A (B.Tech CSE Sem 5)</option>
                <option>Section B (B.Tech CSE Sem 5)</option>
                <option>Section C (B.Tech CSE Sem 5)</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Subject</label>
              <select className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white text-slate-800 focus:outline-none focus:border-indigo-500">
                <option>CS301 Data Structures & Algorithms</option>
                <option>CS302 Operating Systems</option>
                <option>CS303 Database Management Systems</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Valid from</label>
              <input
                type="date"
                defaultValue="2026-11-01"
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white text-slate-800 focus:outline-none focus:border-indigo-500"
              />
            </div>

            {/* Overlap Conflict Engine Warning (PDF Page 15) */}
            {showConflictWarning && (
              <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg flex items-start space-x-2">
                <span className="text-amber-500 font-bold text-xs">▲</span>
                <p className="text-xs text-amber-800 font-medium">
                  This teacher already has a class in Mon, slot 2.
                </p>
              </div>
            )}

            <div className="pt-2 space-y-2">
              <button className="w-full py-2.5 bg-indigo-600 text-white font-bold rounded-lg text-xs hover:bg-indigo-700 shadow-sm transition">
                Save allocation
              </button>
              <button className="w-full py-2 bg-white border border-slate-300 text-slate-700 font-semibold rounded-lg text-xs hover:bg-slate-50 transition">
                Record substitution
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
