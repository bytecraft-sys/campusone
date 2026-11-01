import React from 'react'

export type StatusType = 'good' | 'attention' | 'danger' | 'info'

interface StatusBadgeProps {
  type: StatusType
  label: string
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ type, label }) => {
  let shape = '●'
  let className = 'status-badge '

  switch (type) {
    case 'good':
      shape = '●'
      className += 'status-good'
      break
    case 'attention':
      shape = '▲'
      className += 'status-attention'
      break
    case 'danger':
      shape = '■'
      className += 'status-danger'
      break
    case 'info':
      shape = '◆'
      className += 'status-info'
      break
  }

  return (
    <span className={className}>
      <span style={{ fontSize: '10px' }}>{shape}</span>
      <span>{label}</span>
    </span>
  )
}

