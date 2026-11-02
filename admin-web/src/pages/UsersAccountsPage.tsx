import React, { useState, useEffect } from 'react'
import { StatusBadge } from '../components/StatusBadge'

interface UserItem {
  id?: string
  name: string
  loginId: string
  role: string
  dept: string
  status: { type: 'good' | 'attention' | 'danger' | 'info'; label: string }
  lastLogin: string
}

export const UsersAccountsPage: React.FC = () => {
  const [users, setUsers] = useState<UserItem[]>([
    { name: 'Shrey Student', loginId: 'STU2026001', role: 'Student', dept: 'Section A', status: { type: 'good', label: 'Active' }, lastLogin: 'Today 09:15' },
    { name: 'Prof. Alan Turing', loginId: 'TEA101', role: 'Teacher', dept: 'CSE', status: { type: 'good', label: 'Active' }, lastLogin: 'Today 08:30' },
    { name: 'Aarav Sharma', loginId: 'STU2026002', role: 'Student', dept: 'Section A', status: { type: 'good', label: 'Active' }, lastLogin: 'Today 10:00' },
    { name: 'Ananya Verma', loginId: 'STU2026003', role: 'Student', dept: 'Section A', status: { type: 'good', label: 'Active' }, lastLogin: 'Today 09:45' },
    { name: 'Jane Doe', loginId: 'STU2026042', role: 'Student', dept: 'Section B', status: { type: 'info', label: 'Pending activation' }, lastLogin: '-' },
    { name: 'Dr. John von Neumann', loginId: 'TEA104', role: 'Teacher', dept: 'ECE', status: { type: 'attention', label: 'Suspended' }, lastLogin: '2026-10-15' },
  ])

  const [showDrawer, setShowDrawer] = useState(false)
  const [formData, setFormData] = useState({
    name: '',
    id: '',
    role: 'Student',
    sectionOrDept: '',
    contact: '',
    password: 'StudentPass123!'
  })
  const [successMsg, setSuccessMsg] = useState('')

  // Load live users from Supabase on mount
  useEffect(() => {
    fetch('/api/v1/admin/users')
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data) && data.length > 0) {
          setUsers(data)
        }
      })
      .catch(err => console.log('Live users fetch info:', err))
  }, [])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()

    const newLoginId = formData.id.trim() || `USER${Math.floor(1000 + Math.random() * 9000)}`
    const newName = formData.name.trim() || 'New User'
    const tempPass = formData.password.trim() || (formData.role === 'Student' ? 'StudentPass123!' : 'TeacherPass123!')

    const newRecord: UserItem = {
      name: newName,
      loginId: newLoginId,
      role: formData.role,
      dept: formData.sectionOrDept || (formData.role === 'Student' ? 'Section A' : 'CSE'),
      status: { type: 'good', label: 'Active' },
      lastLogin: '-'
    }

    // Immediately update the UI table
    setUsers(prev => [newRecord, ...prev])
    setSuccessMsg(`✓ Account created! Login ID: "${newLoginId}" | Password: "${tempPass}"`)
    setShowDrawer(false)
    setFormData({ name: '', id: '', role: 'Student', sectionOrDept: '', contact: '', password: 'StudentPass123!' })
    setTimeout(() => setSuccessMsg(''), 8000)

    // Save to Supabase backend in background
    try {
      await fetch('/api/v1/admin/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          full_name: newName,
          login_id: newLoginId,
          email: formData.contact.includes('@') ? formData.contact : `${newLoginId.toLowerCase()}@campusone.edu`,
          role: formData.role.toUpperCase(),
          initial_password: tempPass
        })
      })
    } catch (err) {
      console.log('Background user persistence sync:', err)
    }
  }

  return (
    <div style={{ padding: '28px' }}>
      {/* Top action bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '22px', fontWeight: 700 }}>Users & accounts</h1>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button style={{
            padding: '8px 16px',
            borderRadius: '8px',
            border: '1px solid #cbd5e1',
            backgroundColor: '#ffffff',
            fontWeight: 500,
            fontSize: '13px'
          }}>
            Import CSV
          </button>
          <button
            onClick={() => setShowDrawer(true)}
            style={{
              padding: '8px 16px',
              borderRadius: '8px',
              backgroundColor: 'var(--primary)',
              color: '#ffffff',
              fontWeight: 500,
              fontSize: '13px'
            }}
          >
            Create account
          </button>
        </div>
      </div>

      {successMsg && (
        <div style={{
          padding: '12px 16px',
          backgroundColor: '#ecfdf5',
          color: '#065f46',
          borderRadius: '8px',
          fontSize: '13px',
          marginBottom: '16px',
          border: '1px solid #a7f3d0'
        }}>
          {successMsg}
        </div>
      )}

      {/* Main Table Card */}
      <div style={{ display: 'grid', gridTemplateColumns: showDrawer ? '1fr 340px' : '1fr', gap: '20px' }}>
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: 'var(--radius-lg)',
          padding: '20px',
          border: '1px solid var(--border-color)'
        }}>
          {/* Filters */}
          <div style={{ display: 'flex', gap: '12px', marginBottom: '18px' }}>
            <input
              type="text"
              placeholder="Search name or ID"
              style={{
                flex: 1,
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #cbd5e1',
                fontSize: '13px'
              }}
            />
            <select style={{ padding: '8px 12px', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '13px' }}>
              <option>All roles</option>
              <option>Student</option>
              <option>Teacher</option>
              <option>Admin</option>
            </select>
            <select style={{ padding: '8px 12px', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '13px' }}>
              <option>All statuses</option>
              <option>Active</option>
              <option>Pending activation</option>
              <option>Suspended</option>
            </select>
          </div>

          {/* Table */}
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #f1f5f9', color: '#64748b' }}>
                <th style={{ padding: '10px 12px' }}>Name</th>
                <th style={{ padding: '10px 12px' }}>Login ID</th>
                <th style={{ padding: '10px 12px' }}>Role</th>
                <th style={{ padding: '10px 12px' }}>Section / Dept</th>
                <th style={{ padding: '10px 12px' }}>Status</th>
                <th style={{ padding: '10px 12px' }}>Last login</th>
                <th style={{ padding: '10px 12px' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u, i) => (
                <tr key={i} style={{ borderBottom: '1px solid #f8fafc' }}>
                  <td style={{ padding: '12px', fontWeight: 500 }}>{u.name}</td>
                  <td style={{ padding: '12px', color: '#64748b' }}>{u.loginId}</td>
                  <td style={{ padding: '12px' }}>{u.role}</td>
                  <td style={{ padding: '12px' }}>{u.dept}</td>
                  <td style={{ padding: '12px' }}><StatusBadge type={u.status.type} label={u.status.label} /></td>
                  <td style={{ padding: '12px', color: '#64748b' }}>{u.lastLogin}</td>
                  <td style={{ padding: '12px' }}>
                    {u.status.label === 'Pending activation' ? (
                      <button style={{ color: '#4f46e5', fontWeight: 500 }}>Resend link</button>
                    ) : (
                      <button style={{ color: '#64748b', fontWeight: 500 }}>Manage</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div style={{ marginTop: '16px', fontSize: '12px', color: '#94a3b8' }}>
            Showing 4 of 1,326 · deactivation is blocked while a user owns open work
          </div>
        </div>

        {/* Create Drawer */}
        {showDrawer && (
          <div style={{
            backgroundColor: '#ffffff',
            borderRadius: 'var(--radius-lg)',
            padding: '20px',
            border: '1px solid var(--border-color)',
            height: 'fit-content'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div style={{ fontSize: '15px', fontWeight: 600 }}>Create account</div>
              <button onClick={() => setShowDrawer(false)} style={{ color: '#94a3b8', fontSize: '16px' }}>✕</button>
            </div>
            <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Full name</label>
                <input
                  type="text"
                  required
                  placeholder="Name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
                />
              </div>
              <div>
                <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Institutional ID</label>
                <input
                  type="text"
                  required
                  placeholder="ID"
                  value={formData.id}
                  onChange={(e) => setFormData({ ...formData, id: e.target.value })}
                  style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
                />
              </div>
              <div>
                <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Role</label>
                <select
                  value={formData.role}
                  onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                  style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
                >
                  <option value="Student">Student</option>
                  <option value="Teacher">Teacher</option>
                  <option value="Admin">Admin</option>
                </select>
              </div>
              <div>
                <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Section or department</label>
                <input
                  type="text"
                  placeholder="[Section or Department]"
                  value={formData.sectionOrDept}
                  onChange={(e) => setFormData({ ...formData, sectionOrDept: e.target.value })}
                  style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
                />
              </div>
              <div>
                <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Email or phone for activation</label>
                <input
                  type="text"
                  required
                  placeholder="Contact"
                  value={formData.contact}
                  onChange={(e) => setFormData({ ...formData, contact: e.target.value })}
                  style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
                />
              </div>
              <div>
                <label style={{ fontSize: '12px', color: '#64748b', display: 'block', marginBottom: '4px' }}>Initial Temporary Password</label>
                <input
                  type="text"
                  required
                  placeholder="Password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
                />
              </div>
              <div style={{ fontSize: '11px', color: '#64748b' }}>
                Default: <code>StudentPass123!</code> for students, <code>TeacherPass123!</code> for faculty.
              </div>
              <button
                type="submit"
                style={{
                  padding: '9px 14px',
                  backgroundColor: 'var(--primary)',
                  color: '#ffffff',
                  borderRadius: '6px',
                  fontWeight: 600,
                  fontSize: '13px',
                  marginTop: '6px'
                }}
              >
                Create Account
              </button>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}

