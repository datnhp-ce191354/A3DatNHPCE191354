import { useCallback, useEffect, useMemo, useState } from 'react'
import { api, errorMessage, listOf } from '../api.js'
import { Alert, Busy, Empty, Field, PageHeading, Status } from '../components/Shared.jsx'
import { dateText, money, nextDay, today, yesterday } from '../utils.js'

function RecordForm({ title, fields, values, setValues, onSave, onClose, busy }) {
  return <form className="panel editor" onSubmit={onSave}><div className="panel-head"><h2>{title}</h2><button type="button" className="text-button" onClick={onClose}>Close ✕</button></div><div className="form-grid">
    {fields.filter((field) => !field.hidden).map((field) => <Field key={field.key} label={field.label} hint={field.hint}>
      {field.type === 'select' ? <select required={field.required !== false} value={values[field.key] ?? ''} onChange={(e) => setValues({ ...values, [field.key]: e.target.value })}><option value="">Choose...</option>{field.options?.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select>
        : field.type === 'textarea' ? <textarea required={field.required !== false} rows={3} maxLength={field.maxLength} value={values[field.key] ?? ''} onChange={(e) => setValues({ ...values, [field.key]: e.target.value })} />
          : <input type={field.type || 'text'} required={field.required !== false} min={field.min} max={field.max} minLength={field.minLength} maxLength={field.maxLength} step={field.step} value={values[field.key] ?? ''} onChange={(e) => setValues({ ...values, [field.key]: e.target.value })} />}
    </Field>)}
  </div><div className="form-actions"><button type="button" className="button button--outline" onClick={onClose}>Cancel</button><button className="button button--accent" disabled={busy}>{busy ? 'Saving...' : 'Save'}</button></div></form>
}

function Manager({ title, eyebrow, description, endpoint, empty, fields, columns, searchText, toValues, toPayload, related, relatedError, deleteNote }) {
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [query, setQuery] = useState('')
  const [editing, setEditing] = useState(null)
  const [values, setValues] = useState(null)

  const reload = useCallback(async () => {
    try {
      const { data } = await api.get(endpoint)
      setRecords(listOf(data))
    } catch (err) { setError(errorMessage(err)) }
    finally { setLoading(false) }
  }, [endpoint])

  useEffect(() => { reload() }, [reload])
  const filtered = useMemo(() => records.filter((item) => searchText(item).toLowerCase().includes(query.toLowerCase().trim())), [records, searchText, query])

  function openCreate() {
    setEditing(null)
    setValues(empty)
    setError('')
    setNotice('')
  }

  function openEdit(item) {
    setEditing(item)
    setValues(toValues(item))
    setError('')
    setNotice('')
  }

  async function save(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const payload = toPayload(values, editing)
      if (editing) await api.put(`${endpoint}/${editing.id}`, payload)
      else await api.post(endpoint, payload)
      setValues(null)
      setEditing(null)
      setNotice(editing ? 'Record updated.' : 'Record created.')
      await reload()
    } catch (err) { setError(errorMessage(err)) }
    finally { setBusy(false) }
  }

  async function remove(item) {
    if (!window.confirm(`Delete record #${item.id}?${deleteNote ? ` ${deleteNote}` : ''}`)) return
    setBusy(true)
    setError('')
    setNotice('')
    try {
      await api.delete(`${endpoint}/${item.id}`)
      setNotice('Delete action completed.')
      await reload()
    } catch (err) { setError(errorMessage(err)) }
    finally { setBusy(false) }
  }

  return <div className="section-container page-space"><PageHeading eyebrow={eyebrow} title={title} description={description} action={<button className="button button--accent" onClick={openCreate}>+ Add new</button>} />
    <Alert>{relatedError || error}</Alert><Alert kind="success">{notice}</Alert>
    {values && <RecordForm title={editing ? `Edit ${title.toLowerCase().slice(0, -1)}` : `Add ${title.toLowerCase().slice(0, -1)}`} fields={fields(related, !!editing)} values={values} setValues={setValues} onSave={save} onClose={() => setValues(null)} busy={busy} />}
    <div className="panel table-panel"><div className="table-tools"><h2>All {title.toLowerCase()}</h2><input type="search" aria-label={`Search ${title.toLowerCase()}`} placeholder="Search records..." value={query} onChange={(e) => setQuery(e.target.value)} /></div>
      {loading ? <Busy /> : !filtered.length ? <Empty title="No matching records" description="Try another search or add a new record." /> : <div className="table-scroll"><table><thead><tr>{columns.map((column) => <th key={column.label}>{column.label}</th>)}<th>Actions</th></tr></thead><tbody>{filtered.map((item) => <tr key={item.id}>{columns.map((column) => <td key={column.label}>{column.render(item)}</td>)}<td><div className="row-actions"><button className="text-button" onClick={() => openEdit(item)}>Edit</button><button className="text-button text-button--danger" disabled={busy} onClick={() => remove(item)}>Delete</button></div></td></tr>)}</tbody></table></div>}
    </div>
  </div>
}

const statusOptions = [{ value: 'ACTIVE', label: 'Active' }, { value: 'INACTIVE', label: 'Inactive' }]

export function StaffCustomersPage() {
  return <Manager title="Customers" eyebrow="Staff workspace" description="Search and maintain customer accounts." endpoint="/api/staff/customers"
    empty={{ customerFullName: '', telephone: '', emailAddress: '', customerBirthday: '', customerStatus: 'ACTIVE', password: '' }}
    toValues={(item) => ({ customerFullName: item.customerFullName, telephone: item.telephone, emailAddress: item.emailAddress, customerBirthday: item.customerBirthday, customerStatus: item.customerStatus, password: '' })}
    toPayload={(values, editing) => { const payload = { ...values, customerBirthday: values.customerBirthday || null }; if (editing && !payload.password) delete payload.password; return payload }}
    searchText={(item) => `${item.id} ${item.customerFullName} ${item.emailAddress} ${item.telephone}`}
    fields={(_, editing) => [
      { key: 'customerFullName', label: 'Full name', maxLength: 100, minLength: 2 },
      { key: 'telephone', label: 'Telephone', type: 'tel', maxLength: 20, minLength: 8 },
      { key: 'emailAddress', label: 'Email address', type: 'email', maxLength: 120 },
      { key: 'customerBirthday', label: 'Birthday (optional)', type: 'date', max: yesterday(), required: false },
      { key: 'customerStatus', label: 'Status', type: 'select', options: statusOptions },
      { key: 'password', label: editing ? 'New password' : 'Password', type: 'password', minLength: editing ? undefined : 8, required: !editing, hint: editing ? 'Leave blank to keep the current password.' : 'At least 8 characters.' },
    ]}
    columns={[
      { label: 'Customer', render: (item) => <strong>{item.customerFullName}</strong> },
      { label: 'Email', render: (item) => item.emailAddress },
      { label: 'Telephone', render: (item) => item.telephone },
      { label: 'Birthday', render: (item) => dateText(item.customerBirthday) },
      { label: 'Status', render: (item) => <Status value={item.customerStatus} /> },
    ]} />
}

export function StaffRoomTypesPage() {
  return <Manager title="Room types" eyebrow="Staff workspace" description="Organize the categories shown with every room." endpoint="/api/staff/room-types"
    empty={{ roomTypeName: '', typeDescription: '', typeNote: '' }}
    toValues={(item) => ({ roomTypeName: item.roomTypeName, typeDescription: item.typeDescription, typeNote: item.typeNote })}
    toPayload={(values) => values}
    searchText={(item) => `${item.id} ${item.roomTypeName} ${item.typeDescription || ''}`}
    fields={() => [
      { key: 'roomTypeName', label: 'Type name', maxLength: 100 },
      { key: 'typeDescription', label: 'Description', type: 'textarea', maxLength: 500, required: false },
      { key: 'typeNote', label: 'Note', type: 'textarea', maxLength: 500, required: false },
    ]}
    columns={[
      { label: 'Type', render: (item) => <strong>{item.roomTypeName}</strong> },
      { label: 'Description', render: (item) => item.typeDescription },
      { label: 'Note', render: (item) => item.typeNote || '—' },
    ]} />
}

export function StaffRoomsPage() {
  const [types, setTypes] = useState([])
  const [typeError, setTypeError] = useState('')
  useEffect(() => { api.get('/api/room-types').then(({ data }) => setTypes(listOf(data))).catch((err) => setTypeError(errorMessage(err))) }, [])

  return <Manager title="Rooms" eyebrow="Staff workspace" description="Manage room information and availability. Rooms with booking history become inactive when deleted." endpoint="/api/staff/rooms" related={types} relatedError={typeError}
    empty={{ roomNumber: '', roomDetailDescription: '', roomMaxCapacity: 1, roomPricePerDay: 1, roomTypeId: '', roomStatus: 'ACTIVE' }}
    toValues={(item) => ({ roomNumber: item.roomNumber, roomDetailDescription: item.roomDetailDescription, roomMaxCapacity: item.roomMaxCapacity, roomPricePerDay: item.roomPricePerDay, roomTypeId: item.roomType?.id ?? '', roomStatus: item.roomStatus })}
    toPayload={(values) => ({ ...values, roomMaxCapacity: Number(values.roomMaxCapacity), roomPricePerDay: Number(values.roomPricePerDay), roomTypeId: Number(values.roomTypeId) })}
    searchText={(item) => `${item.id} ${item.roomNumber} ${item.roomType?.roomTypeName || ''} ${item.roomDetailDescription || ''}`}
    fields={(roomTypes) => [
      { key: 'roomNumber', label: 'Room number', maxLength: 30 },
      { key: 'roomTypeId', label: 'Room type', type: 'select', options: roomTypes?.map((type) => ({ value: type.id, label: type.roomTypeName })) || [] },
      { key: 'roomDetailDescription', label: 'Description', type: 'textarea', maxLength: 1000, required: false },
      { key: 'roomMaxCapacity', label: 'Max guests', type: 'number', min: 1, max: 100 },
      { key: 'roomPricePerDay', label: 'Price per night (VND)', type: 'number', min: 1, step: 1 },
      { key: 'roomStatus', label: 'Status', type: 'select', options: statusOptions },
    ]}
    deleteNote="A room with booking history will be marked inactive."
    columns={[
      { label: 'Room', render: (item) => <strong>#{item.roomNumber}</strong> },
      { label: 'Type', render: (item) => item.roomType?.roomTypeName || '—' },
      { label: 'Capacity', render: (item) => `${item.roomMaxCapacity} guests` },
      { label: 'Nightly price', render: (item) => money(item.roomPricePerDay) },
      { label: 'Status', render: (item) => <Status value={item.roomStatus} /> },
    ]} />
}

function bookingLine(roomId = '') {
  const startDate = today()
  return { key: crypto.randomUUID(), roomId: String(roomId), startDate, endDate: nextDay(startDate) }
}

export function StaffBookingsPage() {
  const [bookings, setBookings] = useState([])
  const [customers, setCustomers] = useState([])
  const [rooms, setRooms] = useState([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [query, setQuery] = useState('')
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(null)

  const reload = useCallback(async () => {
    try {
      const [bookingResponse, customerResponse, activeRooms] = await Promise.all([
        api.get('/api/staff/bookings'), api.get('/api/staff/customers'), api.get('/api/staff/rooms'),
      ])
      setBookings(listOf(bookingResponse.data))
      setCustomers(listOf(customerResponse.data))
      setRooms(listOf(activeRooms.data))
    } catch (err) { setError(errorMessage(err)) }
    finally { setLoading(false) }
  }, [])

  useEffect(() => { reload() }, [reload])

  function openCreate() {
    setEditing(null)
    setForm({ customerId: '', bookingStatus: 'PENDING', rooms: [bookingLine()] })
    setError('')
    setNotice('')
  }

  function openEdit(item) {
    setEditing(item)
    setForm({ customerId: String(item.customerId), bookingStatus: item.bookingStatus, rooms: item.details?.map((detail) => ({ key: crypto.randomUUID(), roomId: String(detail.roomId), startDate: detail.startDate, endDate: detail.endDate })) || [] })
    setError('')
    setNotice('')
  }

  function updateLine(key, field, value) {
    setForm((current) => ({ ...current, rooms: current.rooms.map((line) => line.key === key ? { ...line, [field]: value } : line) }))
  }

  async function save(event) {
    event.preventDefault()
    if (!form.rooms.length || new Set(form.rooms.map((line) => line.roomId)).size !== form.rooms.length) { setError('Choose at least one room, without duplicates.'); return }
    if (form.rooms.some((line) => !line.roomId || line.endDate <= line.startDate)) { setError('Every room needs valid dates.'); return }
    setBusy(true)
    setError('')
    const roomsPayload = form.rooms.map(({ roomId, startDate, endDate }) => ({ roomId: Number(roomId), startDate, endDate }))
    try {
      if (editing) {
        const originalRooms = editing.details?.map((detail) => ({ roomId: Number(detail.roomId), startDate: detail.startDate, endDate: detail.endDate })) || []
        const roomsChanged = JSON.stringify(originalRooms) !== JSON.stringify(roomsPayload)
        await api.put(`/api/staff/bookings/${editing.id}`, roomsChanged ? { bookingStatus: form.bookingStatus, rooms: roomsPayload } : { bookingStatus: form.bookingStatus })
      }
      else await api.post('/api/staff/bookings', { customerId: Number(form.customerId), rooms: roomsPayload })
      setForm(null)
      setEditing(null)
      setNotice(editing ? 'Reservation updated.' : 'Reservation created.')
      await reload()
    } catch (err) { setError(errorMessage(err)) }
    finally { setBusy(false) }
  }

  async function remove(item) {
    if (!window.confirm(`Delete reservation #${item.id} and its details?`)) return
    setBusy(true)
    setError('')
    setNotice('')
    try { await api.delete(`/api/staff/bookings/${item.id}`); setNotice('Reservation deleted.'); await reload() }
    catch (err) { setError(errorMessage(err)) }
    finally { setBusy(false) }
  }

  const visible = bookings.filter((item) => `${item.id} ${item.customerName || ''} ${item.bookingStatus || ''} ${item.details?.map((detail) => detail.roomNumber).join(' ') || ''}`.toLowerCase().includes(query.toLowerCase().trim()))

  return <div className="section-container page-space"><PageHeading eyebrow="Staff workspace" title="Bookings" description="Review, create, update, and remove reservations and their room details." action={<button className="button button--accent" onClick={openCreate}>+ Add booking</button>} />
    <Alert>{error}</Alert><Alert kind="success">{notice}</Alert>
    {form && <form className="panel editor" onSubmit={save}><div className="panel-head"><h2>{editing ? `Edit booking #${editing.id}` : 'New booking'}</h2><button type="button" className="text-button" onClick={() => setForm(null)}>Close ✕</button></div><div className="form-grid">
      <Field label="Customer"><select required disabled={!!editing} value={form.customerId} onChange={(e) => setForm({ ...form, customerId: e.target.value })}><option value="">Choose a customer</option>{customers.filter((customer) => editing || customer.customerStatus === 'ACTIVE').map((customer) => <option key={customer.id} value={customer.id}>{customer.customerFullName} · {customer.emailAddress}</option>)}</select></Field>
      {editing && <Field label="Status"><select value={form.bookingStatus} onChange={(e) => setForm({ ...form, bookingStatus: e.target.value })}><option value="PENDING">Pending</option><option value="CONFIRMED">Confirmed</option><option value="CANCELLED">Cancelled</option></select></Field>}
    </div><h3 className="editor-subheading">Booking details</h3>{form.rooms.map((line, index) => <div className="booking-line" key={line.key}><div className="booking-line-heading"><h3>Room {index + 1}</h3>{form.rooms.length > 1 && <button className="text-button text-button--danger" type="button" onClick={() => setForm({ ...form, rooms: form.rooms.filter((item) => item.key !== line.key) })}>Remove</button>}</div><div className="form-grid form-grid--three">
      <Field label="Room"><select required value={line.roomId} onChange={(e) => updateLine(line.key, 'roomId', e.target.value)}><option value="">Choose a room</option>{rooms.filter((room) => room.roomStatus === 'ACTIVE' || String(room.id) === line.roomId).map((room) => <option key={room.id} value={room.id}>Room {room.roomNumber}</option>)}</select></Field>
      <Field label="Start date"><input type="date" required value={line.startDate} onChange={(e) => updateLine(line.key, 'startDate', e.target.value)} /></Field>
      <Field label="End date"><input type="date" required min={nextDay(line.startDate)} value={line.endDate} onChange={(e) => updateLine(line.key, 'endDate', e.target.value)} /></Field>
    </div></div>)}<button type="button" className="button button--outline" onClick={() => setForm({ ...form, rooms: [...form.rooms, bookingLine()] })}>+ Add room</button><div className="form-actions"><button type="button" className="button button--outline" onClick={() => setForm(null)}>Cancel</button><button className="button button--accent" disabled={busy}>{busy ? 'Saving...' : 'Save booking'}</button></div></form>}
    <div className="panel table-panel"><div className="table-tools"><h2>All reservations</h2><input type="search" aria-label="Search bookings" placeholder="Search bookings..." value={query} onChange={(e) => setQuery(e.target.value)} /></div>
      {loading ? <Busy /> : !visible.length ? <Empty title="No matching bookings" description="Try another search or create a reservation." /> : <div className="table-scroll"><table><thead><tr><th>ID / Date</th><th>Customer</th><th>Rooms and dates</th><th>Total</th><th>Status</th><th>Actions</th></tr></thead><tbody>{visible.map((item) => <tr key={item.id}><td><strong>#{item.id}</strong><small>{dateText(item.bookingDate)}</small></td><td>{item.customerName || item.customerId}</td><td>{item.details?.map((detail) => <div className="table-detail" key={`${detail.roomId}-${detail.startDate}`}>Room {detail.roomNumber}: {dateText(detail.startDate)} – {dateText(detail.endDate)}</div>)}</td><td>{money(item.totalPrice)}</td><td><Status value={item.bookingStatus} /></td><td><div className="row-actions"><button className="text-button" onClick={() => openEdit(item)}>Edit</button><button className="text-button text-button--danger" disabled={busy} onClick={() => remove(item)}>Delete</button></div></td></tr>)}</tbody></table></div>}
    </div>
  </div>
}
