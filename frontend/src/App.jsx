import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { useAuth } from './auth.jsx'
import Shell from './components/Shell.jsx'
import { LoginPage, RegisterPage } from './pages/AuthPages.jsx'
import { BookingPage, HistoryPage, ProfilePage } from './pages/CustomerPages.jsx'
import { RoomDetailsPage, RoomsPage } from './pages/PublicRooms.jsx'
import { StaffBookingsPage, StaffCustomersPage, StaffRoomTypesPage, StaffRoomsPage } from './pages/StaffPages.jsx'

function RequireRole({ role, children }) {
  const { session } = useAuth()
  const location = useLocation()
  if (!session) return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  if (session.role !== role) return <Navigate to={session.role === 'STAFF' ? '/staff/customers' : '/'} replace />
  return children
}

function NotFound() {
  return <div className="empty-panel"><h1>Page not found</h1><p>Use the navigation above to continue.</p></div>
}

export default function App() {
  return <Shell>
    <Routes>
      <Route path="/" element={<RoomsPage />} />
      <Route path="/rooms/:id" element={<RoomDetailsPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/book" element={<RequireRole role="CUSTOMER"><BookingPage /></RequireRole>} />
      <Route path="/profile" element={<RequireRole role="CUSTOMER"><ProfilePage /></RequireRole>} />
      <Route path="/bookings" element={<RequireRole role="CUSTOMER"><HistoryPage /></RequireRole>} />
      <Route path="/staff" element={<Navigate to="/staff/customers" replace />} />
      <Route path="/staff/customers" element={<RequireRole role="STAFF"><StaffCustomersPage /></RequireRole>} />
      <Route path="/staff/rooms" element={<RequireRole role="STAFF"><StaffRoomsPage /></RequireRole>} />
      <Route path="/staff/room-types" element={<RequireRole role="STAFF"><StaffRoomTypesPage /></RequireRole>} />
      <Route path="/staff/bookings" element={<RequireRole role="STAFF"><StaffBookingsPage /></RequireRole>} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  </Shell>
}
