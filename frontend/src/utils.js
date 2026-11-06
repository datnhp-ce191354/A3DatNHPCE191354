export const money = (value) => new Intl.NumberFormat('vi-VN', {
  style: 'currency', currency: 'VND', maximumFractionDigits: 0,
}).format(Number(value || 0))

export const dateText = (value) => value ? new Date(`${String(value).slice(0, 10)}T00:00:00`).toLocaleDateString('en-GB') : '—'

export const today = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}

export const yesterday = () => {
  const date = new Date()
  date.setDate(date.getDate() - 1)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

export const nextDay = (date) => {
  const next = new Date(`${date}T00:00:00`)
  next.setDate(next.getDate() + 1)
  return `${next.getFullYear()}-${String(next.getMonth() + 1).padStart(2, '0')}-${String(next.getDate()).padStart(2, '0')}`
}

export const statusClass = (value) => {
  if (value === 'ACTIVE' || value === 'CONFIRMED') return 'status status--good'
  if (value === 'CANCELLED' || value === 'INACTIVE') return 'status status--muted'
  return 'status status--pending'
}
