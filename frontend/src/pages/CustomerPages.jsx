import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { api, errorMessage, listOf, loadAllRooms } from '../api.js'
import { Alert, Busy, Empty, Field, PageHeading, Status } from '../components/Shared.jsx'
import { dateText, money, nextDay, today, yesterday } from '../utils.js'

function newLine(roomId = '') {
  const startDate = today()
  return { key: crypto.randomUUID(), roomId: String(roomId), startDate, endDate: nextDay(startDate) }
}

export function BookingPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const [rooms, setRooms] = useState([])
  const [lines, setLines] = useState(() => [newLine(params.get('roomId') || '')])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    loadAllRooms().then((data) => { if (active) setRooms(data.filter((room) => room.roomStatus === 'ACTIVE')) })
      .catch((err) => { if (active) setError(errorMessage(err)) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  const estimate = useMemo(() => lines.reduce((total, line) => {
    const room = rooms.find((item) => String(item.id) === line.roomId)
    const days = Math.max(0, Math.round((Date.parse(`${line.endDate}T00:00:00`) - Date.parse(`${line.startDate}T00:00:00`)) / 86400000))
    return total + Number(room?.roomPricePerDay || 0) * days
  }, 0), [lines, rooms])

  function changeLine(key, field, value) {
    setLines((current) => current.map((line) => line.key === key ? { ...line, [field]: value } : line))
  }

  async function submit(event) {
    event.preventDefault()
    const roomIds = lines.map((line) => line.roomId)
    if (new Set(roomIds).size !== roomIds.length) { setError('Choose each room only once in a reservation.'); return }
    if (lines.some((line) => !line.roomId || line.startDate < today() || line.endDate <= line.startDate)) { setError('Choose a room and a valid future date range for every stay.'); return }
    setBusy(true)
    setError('')
    try {
      await api.post('/api/bookings/me', { rooms: lines.map(({ roomId, startDate, endDate }) => ({ roomId: Number(roomId), startDate, endDate })) })
      navigate('/bookings', { state: { created: true } })
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return <div className="section-container page-space"><PageHeading eyebrow="Plan your stay" title="Book a room" description="Add one or more rooms, choose your dates, and confirm your reservation." />
    <Alert>{error}</Alert>{loading ? <Busy label="Loading available rooms..." /> : !rooms.length ? <Empty title="No active rooms" description="Please check back when more rooms are available." /> : <div className="booking-layout"><form className="panel booking-form" onSubmit={submit}><div className="panel-head"><h2>Rooms and dates</h2><span>{lines.length} room(s)</span></div>
      {lines.map((line, index) => <div className="booking-line" key={line.key}><div className="booking-line-heading"><h3>Stay {index + 1}</h3>{lines.length > 1 && <button className="text-button text-button--danger" type="button" onClick={() => setLines((current) => current.filter((item) => item.key !== line.key))}>Remove</button>}</div><div className="form-grid form-grid--three">
        <Field label="Room"><select required value={line.roomId} onChange={(e) => changeLine(line.key, 'roomId', e.target.value)}><option value="">Select a room</option>{rooms.map((room) => <option key={room.id} value={room.id}>Room {room.roomNumber} · {money(room.roomPricePerDay)}/night</option>)}</select></Field>
        <Field label="Check in"><input type="date" min={today()} required value={line.startDate} onChange={(e) => { changeLine(line.key, 'startDate', e.target.value); if (line.endDate <= e.target.value) changeLine(line.key, 'endDate', nextDay(e.target.value)) }} /></Field>
        <Field label="Check out"><input type="date" min={nextDay(line.startDate)} required value={line.endDate} onChange={(e) => changeLine(line.key, 'endDate', e.target.value)} /></Field>
      </div></div>)}
      <button type="button" className="button button--outline" onClick={() => setLines((current) => [...current, newLine()])}>+ Add another room</button>
      <div className="booking-actions"><Link className="text-link" to="/">Continue exploring</Link><button className="button button--accent" disabled={busy}>{busy ? 'Creating reservation...' : 'Confirm reservation →'}</button></div>
    </form><aside className="panel booking-summary"><span className="eyebrow">Reservation summary</span><h2>Your stay</h2>{lines.map((line, index) => {
      const room = rooms.find((item) => String(item.id) === line.roomId)
      return <div className="summary-line" key={line.key}><strong>{room ? `Room ${room.roomNumber}` : `Stay ${index + 1}`}</strong><span>{dateText(line.startDate)} — {dateText(line.endDate)}</span></div>
    })}<div className="summary-total"><span>Estimated total</span><strong>{money(estimate)}</strong></div><p>Final price and availability are confirmed by the server when you submit.</p></aside></div>}
  </div>
}

export function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    api.get('/api/customers/me').then(({ data }) => setProfile(data)).catch((err) => setError(errorMessage(err))).finally(() => setLoading(false))
  }, [])

  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setSuccess('')
    try {
      const { data } = await api.put('/api/customers/me', {
        customerFullName: profile.customerFullName,
        telephone: profile.telephone,
        emailAddress: profile.emailAddress,
        customerBirthday: profile.customerBirthday || null,
      })
      setProfile(data)
      setSuccess('Profile updated successfully.')
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return <div className="section-container page-space"><PageHeading eyebrow="Your account" title="My profile" description="Keep your contact details up to date for your reservations." />
    <Alert>{error}</Alert><Alert kind="success">{success}</Alert>{loading ? <Busy label="Loading profile..." /> : profile && <form className="panel profile-form" onSubmit={submit}><div className="panel-head"><h2>Personal details</h2><Status value={profile.customerStatus} /></div><div className="form-grid">
      <Field label="Full name"><input required minLength={2} maxLength={100} value={profile.customerFullName || ''} onChange={(e) => setProfile({ ...profile, customerFullName: e.target.value })} /></Field>
      <Field label="Telephone"><input type="tel" required minLength={8} maxLength={20} pattern="[0-9+() .-]{8,20}" value={profile.telephone || ''} onChange={(e) => setProfile({ ...profile, telephone: e.target.value })} /></Field>
      <Field label="Email address"><input type="email" required maxLength={120} value={profile.emailAddress || ''} onChange={(e) => setProfile({ ...profile, emailAddress: e.target.value })} /></Field>
      <Field label="Birthday (optional)"><input type="date" max={yesterday()} value={profile.customerBirthday || ''} onChange={(e) => setProfile({ ...profile, customerBirthday: e.target.value })} /></Field>
    </div><div className="form-actions"><button className="button button--accent" disabled={busy}>{busy ? 'Saving...' : 'Save changes'}</button></div></form>}
  </div>
}

export function HistoryPage() {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    api.get('/api/bookings/me').then(({ data }) => setBookings(listOf(data))).catch((err) => setError(errorMessage(err))).finally(() => setLoading(false))
  }, [])

  return <div className="section-container page-space"><PageHeading eyebrow="Your trips" title="Booking history" description="View each reservation and its rooms in one place." action={<button className="button button--accent" onClick={() => navigate('/book')}>+ New booking</button>} />
    <Alert>{error}</Alert>{loading ? <Busy label="Loading bookings..." /> : !bookings.length ? <Empty title="No bookings yet" description="Explore rooms and make your first reservation." /> : <div className="booking-history">{[...bookings].sort((a, b) => String(b.bookingDate).localeCompare(String(a.bookingDate))).map((booking) => <article className="panel history-card" key={booking.id}><div className="history-head"><div><span className="eyebrow">Reservation #{booking.id}</span><h2>{dateText(booking.bookingDate)}</h2></div><Status value={booking.bookingStatus} /></div><div className="detail-list">{booking.details?.map((detail) => <div key={`${detail.roomId}-${detail.startDate}`}><strong>Room {detail.roomNumber}</strong><span>{dateText(detail.startDate)} — {dateText(detail.endDate)}</span><b>{money(detail.actualPrice)}</b></div>)}</div><div className="history-total"><span>Total</span><strong>{money(booking.totalPrice)}</strong></div></article>)}</div>}
  </div>
}
