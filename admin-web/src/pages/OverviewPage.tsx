import React from 'react'
import { StatusBadge } from '../components/StatusBadge'

export const OverviewPage: React.FC = () => {
  const kpis = [
    { label: 'Active students', value: '1,240' },
    { label: 'Teachers', value: '86' },
    { label: 'Sessions finalized today', value: '92%', status: { type: 'good' as const, label: 'Healthy' } },
    { label: 'Leave escalated', value: '3', status: { type: 'attention' as const, label: 'Needs reassignment' } },
  ]

  const weekAttendance = [
    { day: 'Mon', value: 88 },
    { day: 'Tue', value: 92 },
    { day: 'Wed', value: 84 },
    { day: 'Thu', value: 91 },
    { day: 'Fri', value: 79 },
  ]

  const attentionItems = [
    { label: 'Open attendance sessions older than 24 h', count: 5, type: 'attention' as const },
    { label: 'Late corrections awaiting approval', count: 2, type: 'attention' as const },
    { label: 'Escalated leave requests', count: 3, type: 'danger' as const },
    { label: 'Accounts locked after failed logins', count: 1, type: 'info' as const },
    { label: 'Data requests due this week', count: 2, type: 'info' as const },
  ]

  const recentActivity = [
    { text: 'Prof. Alan Turing corrected attendance for Section A', time: '10 min ago' },
    { text: 'System Admin created 40 student accounts by CSV import', time: '1 h ago' },
    { text: 'Student Shrey submitted leave request', time: '2 h ago' },
  ]

  return (
    <div style={{ padding: '28px' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, marginBottom: '20px' }}>Overview</h1>

      {/* KPI Row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginBottom: '24px' }}>
        {kpis.map((kpi, idx) => (
          <div key={idx} style={{
            backgroundColor: '#ffffff',
            borderRadius: 'var(--radius-lg)',
            padding: '20px',
            border: '1px solid var(--border-color)',
            boxShadow: '0 1px 3px rgba(0,0,0,0.02)'
          }}>
            <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '8px' }}>{kpi.label}</div>
            <div style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text-primary)', marginBottom: kpi.status ? '8px' : '0' }}>
              {kpi.value}
            </div>
            {kpi.status && <StatusBadge type={kpi.status.type} label={kpi.status.label} />}
          </div>
        ))}
      </div>

      {/* Middle Row: Attendance Chart & Action Center */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '20px', marginBottom: '24px' }}>
        {/* Attendance chart */}
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: 'var(--radius-lg)',
          padding: '20px',
          border: '1px solid var(--border-color)'
        }}>
          <div style={{ fontSize: '15px', fontWeight: 600, marginBottom: '2px' }}>Average attendance this week</div>
          <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '24px' }}>
            Finalized sessions only · institution-wide
          </div>
          <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-around', height: '140px', paddingBottom: '10px' }}>
            {weekAttendance.map((item, idx) => (
              <div key={idx} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
                <div style={{
                  width: '38px',
                  height: `${item.value * 1.2}px`,
                  backgroundColor: '#4f46e5',
                  borderRadius: '6px 6px 0 0'
                }} />
                <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{item.day}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Needs attention */}
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: 'var(--radius-lg)',
          padding: '20px',
          border: '1px solid var(--border-color)'
        }}>
          <div style={{ fontSize: '15px', fontWeight: 600, marginBottom: '16px' }}>Needs attention</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {attentionItems.map((item, idx) => (
              <div key={idx} style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '8px 12px',
                borderRadius: '8px',
                backgroundColor: '#f8fafc',
                border: '1px solid #f1f5f9'
              }}>
                <span style={{ fontSize: '13px', color: '#1e293b' }}>{item.label}</span>
                <StatusBadge type={item.type} label={item.count.toString()} />
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Recent Activity */}
      <div style={{
        backgroundColor: '#ffffff',
        borderRadius: 'var(--radius-lg)',
        padding: '20px',
        border: '1px solid var(--border-color)'
      }}>
        <div style={{ fontSize: '15px', fontWeight: 600, marginBottom: '16px' }}>Recent activity</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {recentActivity.map((act, idx) => (
            <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
              <span style={{ color: '#1e293b' }}>{act.text}</span>
              <span style={{ color: '#94a3b8' }}>{act.time}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

