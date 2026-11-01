import React, { useState } from 'react'

export const PoliciesSettingsPage: React.FC = () => {
  const [attendance, setAttendance] = useState({
    minPct: '75',
    correctionDays: '7',
    excusedLeave: 'No'
  })

  const [assignments, setAssignments] = useState({
    allowLate: 'Yes, until 48 hours',
    penaltyPct: '10',
    maxSize: '25'
  })

  const [leaveLib, setLeaveLib] = useState({
    escalateDays: '3',
    booksPerStudent: '4',
    loanDays: '14'
  })

  const [savedMsg, setSavedMsg] = useState('')

  const handleSave = () => {
    setSavedMsg('Policies updated successfully. Audit event recorded.')
    setTimeout(() => setSavedMsg(''), 4000)
  }

  return (
    <div style={{ padding: '28px' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, marginBottom: '20px' }}>Policies & settings</h1>

      {savedMsg && (
        <div style={{
          padding: '12px 16px',
          backgroundColor: '#ecfdf5',
          color: '#065f46',
          borderRadius: '8px',
          fontSize: '13px',
          marginBottom: '16px',
          border: '1px solid #a7f3d0'
        }}>
          {savedMsg}
        </div>
      )}

      {/* 3 Columns */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px', marginBottom: '24px' }}>
        {/* Attendance Policy */}
        <div style={{ backgroundColor: '#ffffff', borderRadius: 'var(--radius-lg)', padding: '20px', border: '1px solid var(--border-color)' }}>
          <div style={{ fontSize: '15px', fontWeight: 600, marginBottom: '4px' }}>Attendance</div>
          <div style={{ fontSize: '12px', color: '#64748b', marginBottom: '16px' }}>Scope: institution (override per course)</div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Minimum attendance %</label>
              <input
                type="text"
                value={attendance.minPct}
                onChange={(e) => setAttendance({ ...attendance, minPct: e.target.value })}
                style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
              />
            </div>
            <div>
              <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Correction window (days)</label>
              <input
                type="text"
                value={attendance.correctionDays}
                onChange={(e) => setAttendance({ ...attendance, correctionDays: e.target.value })}
                style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
              />
            </div>
            <div>
              <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Count approved leave as excused</label>
              <select
                value={attendance.excusedLeave}
                onChange={(e) => setAttendance({ ...attendance, excusedLeave: e.target.value })}
                style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
              >
                <option value="No">No</option>
                <option value="Yes">Yes</option>
              </select>
            </div>
          </div>
        </div>

        {/* Assignments Policy */}
        <div style={{ backgroundColor: '#ffffff', borderRadius: 'var(--radius-lg)', padding: '20px', border: '1px solid var(--border-color)' }}>
          <div style={{ fontSize: '15px', fontWeight: 600, marginBottom: '4px' }}>Assignments</div>
          <div style={{ fontSize: '12px', color: '#64748b', marginBottom: '16px' }}>Defaults for new assignments</div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Allow late submission</label>
              <input
                type="text"
                value={assignments.allowLate}
                onChange={(e) => setAssignments({ ...assignments, allowLate: e.target.value })}
                style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
              />
            </div>
            <div>
              <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Late penalty %</label>
              <input
                type="text"
                value={assignments.penaltyPct}
                onChange={(e) => setAssignments({ ...assignments, penaltyPct: e.target.value })}
                style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
              />
            </div>
            <div>
              <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Max upload size (MB)</label>
              <input
                type="text"
                value={assignments.maxSize}
                onChange={(e) => setAssignments({ ...assignments, maxSize: e.target.value })}
                style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
              />
            </div>
          </div>
        </div>

        {/* Leave & Library Policy */}
        <div style={{ backgroundColor: '#ffffff', borderRadius: 'var(--radius-lg)', padding: '20px', border: '1px solid var(--border-color)' }}>
          <div style={{ fontSize: '15px', fontWeight: 600, marginBottom: '4px' }}>Leave and library</div>
          <div style={{ fontSize: '12px', color: '#64748b', marginBottom: '16px' }}>Workflow limits</div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Escalate after (days)</label>
              <input
                type="text"
                value={leaveLib.escalateDays}
                onChange={(e) => setLeaveLib({ ...leaveLib, escalateDays: e.target.value })}
                style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
              />
            </div>
            <div>
              <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Books per student (R2)</label>
              <input
                type="text"
                value={leaveLib.booksPerStudent}
                onChange={(e) => setLeaveLib({ ...leaveLib, booksPerStudent: e.target.value })}
                style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
              />
            </div>
            <div>
              <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Loan days (R2)</label>
              <input
                type="text"
                value={leaveLib.loanDays}
                onChange={(e) => setLeaveLib({ ...leaveLib, loanDays: e.target.value })}
                style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
              />
            </div>
          </div>
        </div>
      </div>

      {/* Footer Note & Save Action */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ fontSize: '12px', color: '#64748b' }}>
          Every change is saved in the audit log with who changed it and the old value.
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button style={{ padding: '8px 16px', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '13px', color: '#64748b' }}>
            Discard
          </button>
          <button
            onClick={handleSave}
            style={{ padding: '8px 18px', borderRadius: '8px', backgroundColor: '#4f46e5', color: '#ffffff', fontWeight: 600, fontSize: '13px' }}
          >
            Save changes
          </button>
        </div>
      </div>
    </div>
  )
}

