import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { api, errorMessage } from '../api.js'
import { useAuth } from '../auth.jsx'
import { Alert, Field } from '../components/Shared.jsx'
import { yesterday } from '../utils.js'

export function LoginPage() {
  const { session, signIn } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  if (session) return <Navigate to={session.role === 'STAFF' ? '/staff/customers' : '/'} replace />

  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const { data } = await api.post('/api/auth/login', form)
      signIn(data)
      navigate(location.state?.from || (data.role === 'STAFF' ? '/staff/customers' : '/'), { replace: true })
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return <div className="auth-background"><div className="auth-card"><span className="eyebrow">Welcome back</span><h1>Sign in</h1><p>Use your customer or staff account to continue.</p>{location.state?.registered && <Alert kind="success">Account created. Please sign in.</Alert>}<Alert>{error}</Alert>
    <form className="form-stack" onSubmit={submit}><Field label="Email address"><input type="email" autoComplete="username" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="you@example.com" /></Field><Field label="Password"><input type="password" autoComplete="current-password" required value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder="Your password" /></Field><button className="button button--accent button--wide" disabled={busy}>{busy ? 'Signing in...' : 'Sign in →'}</button></form>
    <div className="auth-foot">New customer? <Link to="/register">Create an account</Link></div>
  </div></div>
}

export function RegisterPage() {
  const { session } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ customerFullName: '', telephone: '', emailAddress: '', customerBirthday: '', password: '', confirmPassword: '' })
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  if (session) return <Navigate to={session.role === 'STAFF' ? '/staff/customers' : '/'} replace />

  async function submit(event) {
    event.preventDefault()
    if (form.password !== form.confirmPassword) { setError('Passwords do not match.'); return }
    setBusy(true)
    setError('')
    try {
      const { confirmPassword, ...payload } = form
      await api.post('/api/auth/register', { ...payload, customerBirthday: payload.customerBirthday || null })
      navigate('/login', { replace: true, state: { registered: true } })
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return <div className="auth-background"><div className="auth-card auth-card--wide"><span className="eyebrow">Join StayBook</span><h1>Create your account</h1><p>One account is all you need to make and manage reservations.</p><Alert>{error}</Alert>
    <form className="form-grid" onSubmit={submit}>
      <Field label="Full name"><input required minLength={2} maxLength={100} value={form.customerFullName} onChange={(e) => setForm({ ...form, customerFullName: e.target.value })} autoComplete="name" /></Field>
      <Field label="Telephone"><input type="tel" required minLength={8} maxLength={20} pattern="[0-9+() .-]{8,20}" value={form.telephone} onChange={(e) => setForm({ ...form, telephone: e.target.value })} autoComplete="tel" /></Field>
      <Field label="Email address"><input type="email" required maxLength={120} value={form.emailAddress} onChange={(e) => setForm({ ...form, emailAddress: e.target.value })} autoComplete="email" /></Field>
      <Field label="Birthday (optional)"><input type="date" max={yesterday()} value={form.customerBirthday} onChange={(e) => setForm({ ...form, customerBirthday: e.target.value })} /></Field>
      <Field label="Password" hint="At least 8 characters"><input type="password" required minLength={8} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} autoComplete="new-password" /></Field>
      <Field label="Confirm password"><input type="password" required minLength={8} value={form.confirmPassword} onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })} autoComplete="new-password" /></Field>
      <div className="form-actions"><button className="button button--accent" disabled={busy}>{busy ? 'Creating account...' : 'Create account →'}</button></div>
    </form><div className="auth-foot">Already have an account? <Link to="/login">Sign in</Link></div>
  </div></div>
}
