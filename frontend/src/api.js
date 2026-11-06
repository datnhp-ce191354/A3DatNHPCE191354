import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const api = axios.create({ baseURL })

api.interceptors.request.use((config) => {
  const session = JSON.parse(localStorage.getItem('staybook-session') || 'null')
  if (session?.token) config.headers.Authorization = `Bearer ${session.token}`
  return config
})

export function errorMessage(error) {
  const body = error.response?.data
  if (typeof body === 'string') return body
  const validation = body?.fields || body?.fieldErrors
  if (validation && typeof validation === 'object') {
    const fields = Object.entries(validation)
    if (fields.length) return fields.map(([field, message]) => `${field}: ${message}`).join('; ')
  }
  return body?.message || body?.error || error.message || 'Request failed.'
}

export function listOf(data) {
  if (Array.isArray(data)) return data
  return data?.content || data?.items || []
}

export async function loadAllRooms() {
  const all = []
  let page = 0
  let totalPages = 1
  while (page < totalPages) {
    const { data } = await api.get('/api/rooms', { params: { page, size: 50 } })
    all.push(...listOf(data))
    totalPages = data.totalPages ?? 1
    page += 1
  }
  return all
}
