import React, { useState } from 'react'
import { Sidebar, NavTab } from './components/Sidebar'
import { Header } from './components/Header'
import { OverviewPage } from './pages/OverviewPage'
import { UsersAccountsPage } from './pages/UsersAccountsPage'
import { AttendanceSupervisionPage } from './pages/AttendanceSupervisionPage'
import { LeaveOverviewPage } from './pages/LeaveOverviewPage'
import { PoliciesSettingsPage } from './pages/PoliciesSettingsPage'
import { AuditLogsPage } from './pages/AuditLogsPage'
import { SignInMFA } from './pages/SignInMFA'
import AcademicStructurePage from './pages/AcademicStructurePage'
import AllocationsTimetablePage from './pages/AllocationsTimetablePage'

export const App: React.FC = () => {
  const [authToken, setAuthToken] = useState<string | null>('demo-token')
  const [currentTab, setCurrentTab] = useState<NavTab>('overview')
  const [adminUser, setAdminUser] = useState<any>({ full_name: 'System Admin', login_id: 'ADMIN001' })

  if (!authToken) {
    return (
      <SignInMFA
        onSuccess={(authData) => {
          setAuthToken(authData.access_token)
          setAdminUser(authData.user)
        }}
      />
    )
  }

  const renderContent = () => {
    switch (currentTab) {
      case 'overview':
        return <OverviewPage />
      case 'users':
        return <UsersAccountsPage />
      case 'academic':
        return <AcademicStructurePage />
      case 'allocations':
        return <AllocationsTimetablePage />
      case 'attendance':
        return <AttendanceSupervisionPage />
      case 'leave':
        return <LeaveOverviewPage />
      case 'policies':
        return <PoliciesSettingsPage />
      case 'audit-logs':
        return <AuditLogsPage />
      default:
        return (
          <div style={{ padding: '36px', textAlign: 'center', color: '#64748b' }}>
            <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '8px', color: '#0f172a' }}>
              {currentTab.toUpperCase().replace('-', ' ')}
            </h2>
            <p style={{ fontSize: '14px' }}>This module view is currently connected to backend contracts.</p>
          </div>
        )
    }
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: 'var(--bg-main)' }}>
      {/* Persistent Deep Navy Sidebar */}
      <Sidebar currentTab={currentTab} onSelectTab={setCurrentTab} />

      {/* Main Content Area */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0, overflowY: 'auto' }}>
        <Header adminName={adminUser?.full_name} onSignOut={() => setAuthToken(null)} />
        <main style={{ flex: 1 }}>
          {renderContent()}
        </main>
      </div>
    </div>
  )
}

export default App

