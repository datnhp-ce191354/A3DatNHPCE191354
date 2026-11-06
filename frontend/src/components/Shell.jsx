import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'

function Navigation({ session }) {
  const links = session?.role === 'STAFF' ? [
    ['/staff/customers', 'Customers'],
    ['/staff/rooms', 'Rooms'],
    ['/staff/room-types', 'Room types'],
    ['/staff/bookings', 'Bookings'],
  ] : session?.role === 'CUSTOMER' ? [
    ['/', 'Explore rooms'],
    ['/book', 'Book a stay'],
    ['/bookings', 'My bookings'],
    ['/profile', 'Profile'],
  ] : [['/', 'Explore rooms']]

  return <nav className="nav-links" aria-label="Main navigation">
    {links.map(([path, label]) => <NavLink key={path} to={path} end={path === '/'} className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>{label}</NavLink>)}
  </nav>
}

export default function Shell({ children }) {
  const { session, signOut } = useAuth()
  const navigate = useNavigate()

  function logout() {
    signOut()
    navigate('/')
  }

  return <div className="app-shell">
    <header className="site-header">
      <div className="header-inner">
        <NavLink className="brand" to="/"><span className="brand-icon">S</span><span>Stay<span className="brand-accent">Book</span></span></NavLink>
        <Navigation session={session} />
        <div className="account-actions">
          {session ? <><span className="account-name" title={session.email}>{session.name || session.email}</span><button className="button button--outline button--small" onClick={logout}>Sign out</button></> : <><NavLink className="text-link" to="/login">Sign in</NavLink><NavLink className="button button--dark button--small" to="/register">Create account</NavLink></>}
        </div>
      </div>
    </header>
    <main className="main-content">{children}</main>
    <footer className="site-footer"><span>StayBook · Room reservation system</span><span>Spring Boot + React</span></footer>
  </div>
}
