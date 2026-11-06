import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, errorMessage, listOf } from '../api.js'
import { Alert, Busy, Empty, PageHeading, Status } from '../components/Shared.jsx'
import { money } from '../utils.js'

function RoomCard({ room }) {
  return <article className="room-card">
    <div className="room-card-art"><span className="art-horizon" /><span className="art-circle" /><span className="room-number">ROOM {room.roomNumber}</span></div>
    <div className="room-card-body">
      <div className="room-card-top"><span className="room-type">{room.roomType?.roomTypeName || 'Guest room'}</span><Status value={room.roomStatus} /></div>
      <h3>Room {room.roomNumber}</h3>
      <p>{room.roomDetailDescription || 'A comfortable room for your next stay.'}</p>
      <div className="room-features"><span>◉ Up to {room.roomMaxCapacity} guests</span><span>◷ Per night</span></div>
      <div className="room-card-bottom"><strong>{money(room.roomPricePerDay)}<small> / night</small></strong><Link className="button button--dark button--small" to={`/rooms/${room.id}`}>View room</Link></div>
    </div>
  </article>
}

export function RoomsPage() {
  const [input, setInput] = useState('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    api.get('/api/rooms', { params: { search, page, size: 8 } })
      .then(({ data }) => { if (active) setResult(data) })
      .catch((err) => { if (active) setError(errorMessage(err)) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [search, page])

  function submit(event) {
    event.preventDefault()
    setPage(0)
    setSearch(input.trim())
  }

  const rooms = listOf(result)
  return <>
    <section className="hero">
      <div className="hero-copy"><span className="eyebrow eyebrow--light">A better way to stay</span><h1>Find your place<br />to unwind.</h1><p>Explore comfortable rooms, choose your dates, and reserve your next stay in minutes.</p>
        <form className="hero-search" onSubmit={submit}><span className="search-symbol">⌕</span><input value={input} onChange={(event) => setInput(event.target.value)} placeholder="Search by room number or type" aria-label="Search rooms" /><button className="button button--accent" type="submit">Search rooms</button></form>
      </div>
      <div className="hero-art" aria-hidden="true"><div className="hero-sun" /><div className="hero-window"><div className="hero-window-inner" /></div><div className="hero-bed" /><div className="hero-pillow" /></div>
    </section>
    <section className="section-container room-section">
      <PageHeading eyebrow="Available spaces" title="Explore our rooms" description={search ? `Search results for “${search}”` : 'Compare rooms and find the right fit for your stay.'} action={<span className="result-count">{result?.totalElements ?? rooms.length} room(s)</span>} />
      <Alert>{error}</Alert>
      {loading ? <Busy label="Loading rooms..." /> : rooms.length ? <div className="room-grid">{rooms.map((room) => <RoomCard key={room.id} room={room} />)}</div> : <Empty title="No rooms found" description="Try another search term or check back later." />}
      {!loading && (result?.totalPages || 0) > 1 && <div className="pagination"><button className="button button--outline" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button><span>Page {page + 1} of {result.totalPages}</span><button className="button button--outline" disabled={page + 1 >= result.totalPages} onClick={() => setPage(page + 1)}>Next</button></div>}
    </section>
  </>
}

export function RoomDetailsPage() {
  const { id } = useParams()
  const [room, setRoom] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    api.get(`/api/rooms/${id}`).then(({ data }) => { if (active) setRoom(data) })
      .catch((err) => { if (active) setError(errorMessage(err)) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id])

  if (loading) return <div className="section-container"><Busy label="Loading room..." /></div>
  if (error) return <div className="section-container"><Alert>{error}</Alert><Link className="text-link" to="/">← Back to rooms</Link></div>
  if (!room) return <div className="section-container"><Empty title="Room not found" description="This room may no longer be available." /></div>

  return <div className="section-container detail-page">
    <Link className="back-link" to="/">← All rooms</Link>
    <div className="detail-layout"><div className="detail-art"><span>ROOM {room.roomNumber}</span><div className="detail-art-window" /><div className="detail-art-bed" /></div>
      <div className="detail-card"><div className="detail-kicker"><span className="eyebrow">{room.roomType?.roomTypeName || 'Guest room'}</span><Status value={room.roomStatus} /></div><h1>Room {room.roomNumber}</h1><p className="detail-description">{room.roomDetailDescription || 'A comfortable room for your next stay.'}</p>
        <div className="detail-facts"><div><span>Capacity</span><strong>{room.roomMaxCapacity} guests</strong></div><div><span>Room type</span><strong>{room.roomType?.roomTypeName || '—'}</strong></div><div><span>Price</span><strong>{money(room.roomPricePerDay)} / night</strong></div></div>
        {room.roomType?.typeDescription && <p className="muted">{room.roomType.typeDescription}</p>}
        {room.roomStatus === 'ACTIVE' ? <Link className="button button--accent button--wide" to={`/book?roomId=${room.id}`}>Reserve this room →</Link> : <button className="button button--outline button--wide" disabled>Currently unavailable</button>}
      </div>
    </div>
  </div>
}
