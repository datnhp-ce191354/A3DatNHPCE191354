import { createContext, useContext, useMemo, useState } from 'react'

const AuthContext = createContext(null)

function readSession() {
  try {
    return JSON.parse(localStorage.getItem('staybook-session') || 'null')
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(readSession)

  const value = useMemo(() => ({
    session,
    signIn(data) {
      const next = { token: data.token, role: data.role, name: data.name, email: data.email }
      localStorage.setItem('staybook-session', JSON.stringify(next))
      setSession(next)
    },
    signOut() {
      localStorage.removeItem('staybook-session')
      setSession(null)
    },
  }), [session])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
