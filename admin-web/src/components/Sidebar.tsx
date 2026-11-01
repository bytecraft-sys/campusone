import React from 'react'
import {
  LayoutDashboard,
  Users,
  Network,
  CalendarDays,
  CalendarCheck2,
  FileSpreadsheet,
  CalendarClock,
  BookOpen,
  Bell,
  BarChart3,
  Sliders,
  ShieldCheck,
  ClipboardList,
} from './Icons'


export type NavTab =
  | 'overview'
  | 'users'
  | 'academic'
  | 'allocations'
  | 'attendance'
  | 'assignments'
  | 'leave'
  | 'library'
  | 'notices'
  | 'reports'
  | 'policies'
  | 'data-requests'
  | 'audit-logs'

interface SidebarProps {
  currentTab: NavTab
  onSelectTab: (tab: NavTab) => void
}

export const Sidebar: React.FC<SidebarProps> = ({ currentTab, onSelectTab }) => {
  const menuItems: { id: NavTab; label: string; icon: any; releaseBadge?: string }[] = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'users', label: 'Users & accounts', icon: Users },
    { id: 'academic', label: 'Academic structure', icon: Network },
    { id: 'allocations', label: 'Allocations & timetable', icon: CalendarDays },
    { id: 'attendance', label: 'Attendance', icon: CalendarCheck2 },
    { id: 'assignments', label: 'Assignments', icon: FileSpreadsheet },
    { id: 'leave', label: 'Leave overview', icon: CalendarClock },
    { id: 'library', label: 'Library', icon: BookOpen, releaseBadge: 'R2' },
    { id: 'notices', label: 'Notices', icon: Bell, releaseBadge: 'R2' },
    { id: 'reports', label: 'Reports', icon: BarChart3, releaseBadge: 'R2' },
    { id: 'policies', label: 'Policies', icon: Sliders },
    { id: 'data-requests', label: 'Data requests', icon: ShieldCheck },
    { id: 'audit-logs', label: 'Audit logs', icon: ClipboardList },
  ]

  return (
    <aside style={{
      width: '240px',
      backgroundColor: 'var(--bg-sidebar)',
      color: '#ffffff',
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      flexShrink: 0
    }}>
      {/* Brand Header */}
      <div style={{
        padding: '24px 20px',
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
        borderBottom: '1px solid rgba(255,255,255,0.08)'
      }}>
        <div style={{
          width: '32px',
          height: '32px',
          backgroundColor: '#4f46e5',
          borderRadius: '8px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontWeight: 700,
          fontSize: '16px'
        }}>
          C
        </div>
        <div>
          <div style={{ fontWeight: 700, fontSize: '16px', letterSpacing: '-0.3px' }}>CampusOne</div>
          <div style={{ fontSize: '11px', color: '#94a3b8' }}>Admin Console</div>
        </div>
      </div>

      {/* Nav List */}
      <nav style={{ flex: 1, padding: '16px 12px', overflowY: 'auto' }}>
        {menuItems.map((item) => {
          const Icon = item.icon
          const isActive = currentTab === item.id
          return (
            <button
              key={item.id}
              onClick={() => onSelectTab(item.id)}
              style={{
                width: '100%',
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                padding: '9px 12px',
                borderRadius: '8px',
                marginBottom: '4px',
                color: isActive ? '#ffffff' : '#94a3b8',
                backgroundColor: isActive ? 'var(--primary)' : 'transparent',
                fontWeight: isActive ? 600 : 500,
                fontSize: '13px',
                textAlign: 'left'
              }}
              onMouseEnter={(e) => {
                if (!isActive) e.currentTarget.style.backgroundColor = 'var(--bg-sidebar-hover)'
              }}
              onMouseLeave={(e) => {
                if (!isActive) e.currentTarget.style.backgroundColor = 'transparent'
              }}
            >
              <Icon size={16} />
              <span style={{ flex: 1 }}>{item.label}</span>
              {item.releaseBadge && (
                <span style={{
                  fontSize: '10px',
                  backgroundColor: 'rgba(255,255,255,0.1)',
                  color: '#cbd5e1',
                  padding: '2px 5px',
                  borderRadius: '4px',
                  fontWeight: 600
                }}>
                  {item.releaseBadge}
                </span>
              )}
            </button>
          )
        })}
      </nav>
    </aside>
  )
}
