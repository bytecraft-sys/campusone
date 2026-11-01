import React from 'react'
import { Search, Bell } from './Icons'


interface HeaderProps {
  adminName?: string
  onSignOut?: () => void
}

export const Header: React.FC<HeaderProps> = ({ adminName = 'System Admin', onSignOut }) => {
  return (
    <header style={{
      height: '64px',
      backgroundColor: '#ffffff',
      borderBottom: '1px solid #e2e8f0',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 28px',
      position: 'sticky',
      top: 0,
      zIndex: 10
    }}>
      {/* Search Input */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
        backgroundColor: '#f8fafc',
        border: '1px solid #e2e8f0',
        borderRadius: '8px',
        padding: '8px 14px',
        width: '380px'
      }}>
        <Search size={16} color="#94a3b8" />
        <input
          type="text"
          placeholder="Search users, sections, requests"
          style={{
            border: 'none',
            outline: 'none',
            backgroundColor: 'transparent',
            width: '100%',
            fontSize: '13px',
            color: '#0f172a'
          }}
        />
      </div>

      {/* Right Controls */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
        <button style={{ color: '#64748b', position: 'relative', padding: '6px' }}>
          <Bell size={18} />
          <span style={{
            position: 'absolute',
            top: '4px',
            right: '4px',
            width: '7px',
            height: '7px',
            backgroundColor: '#ef4444',
            borderRadius: '50%'
          }} />
        </button>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '13px', fontWeight: 600, color: '#0f172a' }}>{adminName}</div>
            <div style={{ fontSize: '11px', color: '#64748b' }}>Administrator</div>
          </div>
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: '50%',
            backgroundColor: '#e0e7ff',
            color: '#4338ca',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 700,
            fontSize: '13px'
          }}>
            AD
          </div>
          {onSignOut && (
            <button
              onClick={onSignOut}
              style={{
                fontSize: '12px',
                color: '#94a3b8',
                marginLeft: '6px',
                padding: '4px 8px',
                borderRadius: '4px',
                border: '1px solid #e2e8f0'
              }}
            >
              Sign out
            </button>
          )}
        </div>
      </div>
    </header>
  )
}
