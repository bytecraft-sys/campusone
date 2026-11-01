import React, { useState } from 'react'

export const AuditLogsPage: React.FC = () => {
  const [selectedLog, setSelectedLog] = useState({
    action: 'attendance.correct',
    requestId: '9f1c-78ab-44de',
    before: 'NOT_MARKED',
    after: 'PRESENT',
    reason: 'Student arrived with verified doctor memo',
    actor: 'Prof. Alan Turing (TEA101)',
    ip: '192.168.1.45',
    time: '2026-10-29 10:14:02 UTC'
  })

  const logs = [
    { time: '10:14:02', actor: 'Prof. Alan Turing', action: 'attendance.correct', entity: 'attendance_record [77a1]', ip: '192.168.1.45' },
    { time: '09:45:11', actor: 'System Admin', action: 'leave.reassign', entity: 'leave_request [3b89]', ip: '192.168.1.10' },
    { time: '09:00:28', actor: 'System Admin', action: 'user.create', entity: 'user [STU2026042]', ip: '192.168.1.10' },
    { time: 'Yesterday', actor: 'System Admin', action: 'policy.update', entity: 'attendance_threshold', ip: '192.168.1.10' },
    { time: 'Yesterday', actor: 'System Admin', action: 'file.access', entity: 'leave doc [med_cert.pdf]', ip: '192.168.1.10' },
  ]

  return (
    <div style={{ padding: '28px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '22px', fontWeight: 700 }}>Audit logs</h1>
        <button style={{
          padding: '8px 16px',
          borderRadius: '8px',
          border: '1px solid #cbd5e1',
          backgroundColor: '#ffffff',
          fontWeight: 500,
          fontSize: '13px'
        }}>
          Export
        </button>
      </div>

      {/* Main Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: '20px' }}>
        <div style={{ backgroundColor: '#ffffff', borderRadius: 'var(--radius-lg)', padding: '20px', border: '1px solid var(--border-color)' }}>
          {/* Filters */}
          <div style={{ display: 'flex', gap: '10px', marginBottom: '18px' }}>
            <input
              type="text"
              placeholder="Search actor or entity"
              style={{ flex: 1, padding: '8px 12px', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '13px' }}
            />
            <select style={{ padding: '8px 12px', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '13px' }}>
              <option>All actions</option>
              <option>attendance.correct</option>
              <option>leave.reassign</option>
              <option>user.create</option>
              <option>policy.update</option>
            </select>
            <input
              type="text"
              placeholder="[date range]"
              style={{ width: '130px', padding: '8px 12px', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '13px' }}
            />
          </div>

          {/* Table */}
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #f1f5f9', color: '#64748b' }}>
                <th style={{ padding: '8px' }}>Time</th>
                <th style={{ padding: '8px' }}>Actor</th>
                <th style={{ padding: '8px' }}>Action</th>
                <th style={{ padding: '8px' }}>Entity</th>
                <th style={{ padding: '8px' }}>IP</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((l, i) => (
                <tr
                  key={i}
                  onClick={() => setSelectedLog({
                    ...selectedLog,
                    action: l.action,
                    actor: l.actor,
                    ip: l.ip,
                    time: l.time
                  })}
                  style={{ borderBottom: '1px solid #f8fafc', cursor: 'pointer' }}
                >
                  <td style={{ padding: '12px 8px', color: '#64748b' }}>{l.time}</td>
                  <td style={{ padding: '12px 8px', fontWeight: 500 }}>{l.actor}</td>
                  <td style={{ padding: '12px 8px', color: '#4f46e5', fontWeight: 500 }}>{l.action}</td>
                  <td style={{ padding: '12px 8px' }}>{l.entity}</td>
                  <td style={{ padding: '12px 8px', color: '#64748b' }}>{l.ip}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Side Detail Pane */}
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: 'var(--radius-lg)',
          padding: '20px',
          border: '1px solid var(--border-color)',
          display: 'flex',
          flexDirection: 'column',
          gap: '16px'
        }}>
          <div style={{ fontSize: '15px', fontWeight: 600 }}>Event detail</div>

          <div style={{ backgroundColor: '#f8fafc', padding: '14px', borderRadius: '8px', border: '1px solid #f1f5f9', fontSize: '13px' }}>
            <div style={{ fontWeight: 600, color: '#4f46e5', marginBottom: '8px' }}>
              {selectedLog.action} · request [{selectedLog.requestId}]
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div><strong style={{ color: '#64748b' }}>before:</strong> {selectedLog.before}</div>
              <div><strong style={{ color: '#64748b' }}>after:</strong> {selectedLog.after}</div>
              <div><strong style={{ color: '#64748b' }}>reason:</strong> {selectedLog.reason}</div>
              <div><strong style={{ color: '#64748b' }}>actor:</strong> {selectedLog.actor}</div>
              <div><strong style={{ color: '#64748b' }}>ip address:</strong> {selectedLog.ip}</div>
              <div><strong style={{ color: '#64748b' }}>timestamp:</strong> {selectedLog.time}</div>
            </div>
          </div>

          <div style={{ fontSize: '12px', color: '#94a3b8', fontStyle: 'italic', marginTop: 'auto' }}>
            Audit entries are append-only and cannot be edited or deleted.
          </div>
        </div>
      </div>
    </div>
  )
}

