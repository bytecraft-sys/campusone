import React, { useState } from 'react'
import { StatusBadge } from '../components/StatusBadge'

export const AttendanceSupervisionPage: React.FC = () => {
  const [openSessions] = useState([
    { section: 'Section A · CS301 (DBMS)', teacher: 'Prof. Turing', date: '2026-10-27', marked: '28 of 40', status: { type: 'attention' as const, label: 'Open' } },

    { section: 'Section B · CS302 (OS)', teacher: 'Prof. Neumann', date: '2026-10-27', marked: '0 of 38', status: { type: 'danger' as const, label: 'Not started' } },
    { section: 'Section C · CS303 (DSA)', teacher: 'Prof. Hopper', date: '2026-10-28', marked: '40 of 40', status: { type: 'info' as const, label: 'Not finalized' } },
  ])

  const [corrections, setCorrections] = useState([
    { id: '1', student: 'Jane Doe (STU2026042)', change: 'NM to P', reason: 'Student joined mid-lecture after principal approval' },
    { id: '2', student: 'Mark Smith (STU2026019)', change: 'AB to P', reason: 'Medical certificate submitted and verified late' },
  ])

  const [notification, setNotification] = useState('')

  const handleRemind = (teacher: string) => {
    setNotification(`Automated notification sent to ${teacher} to finalize open attendance session.`)
    setTimeout(() => setNotification(''), 4000)
  }

  const handleDecision = (id: string, action: 'approved' | 'rejected') => {
    setCorrections(corrections.filter(c => c.id !== id))
    setNotification(`Attendance correction has been ${action}. Student record updated and audited.`)
    setTimeout(() => setNotification(''), 4000)
  }

  return (
    <div style={{ padding: '28px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '22px', fontWeight: 700 }}>Attendance supervision</h1>
        <button style={{
          padding: '8px 16px',
          borderRadius: '8px',
          border: '1px solid #cbd5e1',
          backgroundColor: '#ffffff',
          fontWeight: 500,
          fontSize: '13px'
        }}>
          Export report
        </button>
      </div>

      {notification && (
        <div style={{
          padding: '12px 16px',
          backgroundColor: '#ecfdf5',
          color: '#065f46',
          borderRadius: '8px',
          fontSize: '13px',
          marginBottom: '16px',
          border: '1px solid #a7f3d0'
        }}>
          {notification}
        </div>
      )}

      {/* KPI Row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginBottom: '24px' }}>
        <div style={{ backgroundColor: '#ffffff', borderRadius: 'var(--radius-lg)', padding: '20px', border: '1px solid var(--border-color)' }}>
          <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '8px' }}>Sessions today</div>
          <div style={{ fontSize: '26px', fontWeight: 700 }}>42</div>
        </div>
        <div style={{ backgroundColor: '#ffffff', borderRadius: 'var(--radius-lg)', padding: '20px', border: '1px solid var(--border-color)' }}>
          <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '8px' }}>Finalized</div>
          <div style={{ fontSize: '26px', fontWeight: 700 }}>37</div>
        </div>
        <div style={{ backgroundColor: '#ffffff', borderRadius: 'var(--radius-lg)', padding: '20px', border: '1px solid var(--border-color)' }}>
          <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '8px' }}>Open longer than 24 h</div>
          <div style={{ fontSize: '26px', fontWeight: 700, marginBottom: '6px' }}>5</div>
          <StatusBadge type="attention" label="Needs follow-up" />
        </div>
        <div style={{ backgroundColor: '#ffffff', borderRadius: 'var(--radius-lg)', padding: '20px', border: '1px solid var(--border-color)' }}>
          <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '8px' }}>Corrections to approve</div>
          <div style={{ fontSize: '26px', fontWeight: 700 }}>{corrections.length}</div>
        </div>
      </div>

      {/* Grid: Open Sessions & Late Corrections */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '20px' }}>
        {/* Open sessions */}
        <div style={{ backgroundColor: '#ffffff', borderRadius: 'var(--radius-lg)', padding: '20px', border: '1px solid var(--border-color)' }}>
          <div style={{ fontSize: '15px', fontWeight: 600, marginBottom: '16px' }}>Open or incomplete sessions</div>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #f1f5f9', color: '#64748b' }}>
                <th style={{ padding: '8px' }}>Section · Subject</th>
                <th style={{ padding: '8px' }}>Teacher</th>
                <th style={{ padding: '8px' }}>Date</th>
                <th style={{ padding: '8px' }}>Marked</th>
                <th style={{ padding: '8px' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {openSessions.map((s, idx) => (
                <tr key={idx} style={{ borderBottom: '1px solid #f8fafc' }}>
                  <td style={{ padding: '10px 8px', fontWeight: 500 }}>{s.section}</td>
                  <td style={{ padding: '10px 8px' }}>{s.teacher}</td>
                  <td style={{ padding: '10px 8px', color: '#64748b' }}>{s.date}</td>
                  <td style={{ padding: '10px 8px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span>{s.marked}</span>
                      <StatusBadge type={s.status.type} label={s.status.label} />
                    </div>
                  </td>
                  <td style={{ padding: '10px 8px' }}>
                    <button
                      onClick={() => handleRemind(s.teacher)}
                      style={{ color: '#4f46e5', fontWeight: 500, fontSize: '12px', border: '1px solid #e0e7ff', padding: '4px 8px', borderRadius: '4px' }}
                    >
                      Remind
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Late corrections */}
        <div style={{ backgroundColor: '#ffffff', borderRadius: 'var(--radius-lg)', padding: '20px', border: '1px solid var(--border-color)' }}>
          <div style={{ fontSize: '15px', fontWeight: 600, marginBottom: '16px' }}>Late corrections awaiting approval</div>
          {corrections.length === 0 ? (
            <div style={{ color: '#94a3b8', fontSize: '13px' }}>No pending late corrections.</div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {corrections.map((c) => (
                <div key={c.id} style={{ border: '1px solid #f1f5f9', borderRadius: '8px', padding: '12px', backgroundColor: '#fafafa' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                    <span style={{ fontWeight: 600, fontSize: '13px' }}>{c.student}</span>
                    <span style={{ fontWeight: 600, fontSize: '12px', color: '#4f46e5' }}>{c.change}</span>
                  </div>
                  <div style={{ fontSize: '12px', color: '#64748b', marginBottom: '10px' }}>Reason: {c.reason}</div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                      onClick={() => handleDecision(c.id, 'approved')}
                      style={{ padding: '5px 12px', backgroundColor: '#4f46e5', color: '#ffffff', borderRadius: '4px', fontSize: '12px', fontWeight: 500 }}
                    >
                      Approve
                    </button>
                    <button
                      onClick={() => handleDecision(c.id, 'rejected')}
                      style={{ padding: '5px 12px', border: '1px solid #cbd5e1', color: '#64748b', borderRadius: '4px', fontSize: '12px' }}
                    >
                      Reject
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
          <div style={{ marginTop: '16px', fontSize: '12px', color: '#94a3b8', fontStyle: 'italic' }}>
            Admins approve late corrections but never mark attendance.
          </div>
        </div>
      </div>
    </div>
  )
}
