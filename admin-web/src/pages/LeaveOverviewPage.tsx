import React, { useState } from 'react'
import { StatusBadge } from '../components/StatusBadge'

export const LeaveOverviewPage: React.FC = () => {
  const [activeFilter, setActiveFilter] = useState<'All' | 'Pending' | 'Escalated' | 'Decided'>('Escalated')
  const [selectedRequest, setSelectedRequest] = useState<any>({
    id: 'leave-101',
    student: 'Shrey Student (STU2026001)',
    dates: '2026-11-10 to 2026-11-12',
    assignedTeacher: 'Prof. Alan Turing',
    status: { type: 'danger' as const, label: 'Escalated' },
    reason: 'Family emergency and hospital visit with relative',
    history: [
      { event: 'Submitted by student', time: '2026-10-25 09:30' },
      { event: 'Assigned to Prof. Alan Turing', time: '2026-10-25 09:30' },
      { event: 'Escalated: no decision in 3 days', time: '2026-10-28 09:30' },
    ]
  })

  const [notification, setNotification] = useState('')

  const handleReassign = () => {
    setNotification('Leave request reassigned to Prof. John von Neumann and recorded in audit log.')
    setTimeout(() => setNotification(''), 4000)
  }

  const requests = [
    { student: 'Jane Doe', dates: '2026-11-05 to 2026-11-06', teacher: 'Prof. Alan Turing', status: { type: 'info' as const, label: 'Pending' } },
    { student: 'Shrey Student', dates: '2026-11-10 to 2026-11-12', teacher: 'Prof. Alan Turing', status: { type: 'danger' as const, label: 'Escalated' } },
    { student: 'Mark Smith', dates: '2026-10-20 to 2026-10-22', teacher: 'Prof. Ada Lovelace', status: { type: 'good' as const, label: 'Approved' } },
    { student: 'Alex Johnson', dates: '2026-10-18 to 2026-10-19', teacher: 'Prof. Grace Hopper', status: { type: 'attention' as const, label: 'Rejected' } },
  ]

  return (
    <div style={{ padding: '28px' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, marginBottom: '8px' }}>Leave overview</h1>
      <div style={{
        fontSize: '13px',
        color: '#475569',
        backgroundColor: '#f1f5f9',
        padding: '10px 14px',
        borderRadius: '8px',
        marginBottom: '20px',
        display: 'flex',
        alignItems: 'center',
        gap: '8px'
      }}>
        <span>◆</span>
        <span>Admins can supervise leave and reassign escalated requests, but cannot approve or reject.</span>
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

      {/* Main Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: '20px' }}>
        {/* Table pane */}
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: 'var(--radius-lg)',
          padding: '20px',
          border: '1px solid var(--border-color)'
        }}>
          {/* Tabs */}
          <div style={{ display: 'flex', gap: '8px', marginBottom: '18px' }}>
            {(['All', 'Pending', 'Escalated', 'Decided'] as const).map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveFilter(tab)}
                style={{
                  padding: '6px 14px',
                  borderRadius: '20px',
                  fontSize: '13px',
                  fontWeight: 500,
                  backgroundColor: activeFilter === tab ? '#4f46e5' : '#f1f5f9',
                  color: activeFilter === tab ? '#ffffff' : '#64748b'
                }}
              >
                {tab === 'Escalated' ? 'Escalated 3' : tab}
              </button>
            ))}
          </div>

          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #f1f5f9', color: '#64748b' }}>
                <th style={{ padding: '8px' }}>Student</th>
                <th style={{ padding: '8px' }}>Dates</th>
                <th style={{ padding: '8px' }}>Assigned teacher</th>
                <th style={{ padding: '8px' }}>Status</th>
                <th style={{ padding: '8px' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {requests.map((r, i) => (
                <tr key={i} style={{ borderBottom: '1px solid #f8fafc' }}>
                  <td style={{ padding: '12px 8px', fontWeight: 500 }}>{r.student}</td>
                  <td style={{ padding: '12px 8px', color: '#64748b' }}>{r.dates}</td>
                  <td style={{ padding: '12px 8px' }}>{r.teacher}</td>
                  <td style={{ padding: '12px 8px' }}><StatusBadge type={r.status.type} label={r.status.label} /></td>
                  <td style={{ padding: '12px 8px' }}>
                    {r.status.label === 'Escalated' ? (
                      <button
                        onClick={() => setSelectedRequest({
                          ...selectedRequest,
                          student: r.student,
                          dates: r.dates,
                          assignedTeacher: r.teacher
                        })}
                        style={{ color: '#4f46e5', fontWeight: 600, fontSize: '12px' }}
                      >
                        Reassign
                      </button>
                    ) : (
                      <span style={{ color: '#cbd5e1' }}>—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Decision History & Reassign Pane */}
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: 'var(--radius-lg)',
          padding: '20px',
          border: '1px solid var(--border-color)',
          display: 'flex',
          flexDirection: 'column',
          gap: '16px'
        }}>
          <div style={{ fontSize: '15px', fontWeight: 600 }}>Decision history</div>

          <div style={{ backgroundColor: '#f8fafc', padding: '12px', borderRadius: '8px', border: '1px solid #f1f5f9' }}>
            <div style={{ fontSize: '13px', fontWeight: 600, marginBottom: '4px' }}>{selectedRequest.student}</div>
            <div style={{ fontSize: '12px', color: '#64748b', marginBottom: '8px' }}>Dates: {selectedRequest.dates}</div>
            <div style={{ fontSize: '12px', color: '#334155', fontStyle: 'italic' }}>Reason: "{selectedRequest.reason}"</div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {selectedRequest.history.map((h: any, idx: number) => (
              <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px' }}>
                <span style={{ color: '#334155' }}>{h.event}</span>
                <span style={{ color: '#94a3b8' }}>{h.time}</span>
              </div>
            ))}
          </div>

          <div style={{ borderTop: '1px solid #f1f5f9', paddingTop: '16px' }}>
            <label style={{ fontSize: '12px', fontWeight: 600, color: '#475569', display: 'block', marginBottom: '6px' }}>
              Reassign to
            </label>
            <select style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px', marginBottom: '12px' }}>
              <option>Prof. John von Neumann (Department Head)</option>
              <option>Prof. Grace Hopper</option>
              <option>Prof. Ada Lovelace</option>
            </select>
            <button
              onClick={handleReassign}
              style={{
                width: '100%',
                padding: '9px',
                backgroundColor: '#4f46e5',
                color: '#ffffff',
                borderRadius: '6px',
                fontWeight: 600,
                fontSize: '13px'
              }}
            >
              Reassign and record
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

