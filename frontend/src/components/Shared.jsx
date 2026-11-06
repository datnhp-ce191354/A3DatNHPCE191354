import { statusClass } from '../utils.js'

export function Alert({ children, kind = 'error' }) {
  if (!children) return null
  return <div role="alert" className={`alert alert--${kind}`}>{children}</div>
}

export function Busy({ label = 'Loading...' }) {
  return <div className="loading" role="status"><span className="spinner" />{label}</div>
}

export function Empty({ title, description }) {
  return <div className="empty-panel"><div className="empty-symbol">⌁</div><h2>{title}</h2><p>{description}</p></div>
}

export function Status({ value }) {
  return <span className={statusClass(value)}>{value || 'UNKNOWN'}</span>
}

export function Field({ label, children, hint }) {
  return <label className="field"><span className="field-label">{label}</span>{children}{hint && <small>{hint}</small>}</label>
}

export function PageHeading({ eyebrow, title, description, action }) {
  return <div className="page-heading"><div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1>{description && <p>{description}</p>}</div>{action && <div className="heading-action">{action}</div>}</div>
}
