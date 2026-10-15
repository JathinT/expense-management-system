import { useCallback, useEffect, useState } from 'react'

const today = new Date()
const localDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
const emptyForm = { amount: '', category: '', description: '', expenseDate: localDate }

// Helper to build API URLs based on VITE_API_BASE_URL.
// - If VITE_API_BASE_URL is set, it is used as the backend base. We normalize it and ensure it points to the /api prefix.
// - If not set (development), we return paths under "/api" so Vite's dev proxy continues to work.
function apiUrl(path) {
  const envBase = import.meta.env.VITE_API_BASE_URL
  const normalizedPath = path.startsWith('/') ? path : `/${path}`

  if (!envBase) {
    // Development: rely on Vite proxy for /api
    return `/api${normalizedPath}`
  }

  // Production: use configured base. Trim trailing slashes and ensure a single /api segment.
  let base = String(envBase).trim()
  // Remove trailing slashes
  base = base.replace(/\/+$/, '')
  // Ensure base ends with /api
  if (!base.endsWith('/api')) {
    base = `${base}/api`
  }

  return `${base}${normalizedPath}`
}

function App() {
  const [expenses, setExpenses] = useState([])
  const [total, setTotal] = useState('0.00')
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  const loadExpenses = useCallback(async () => {
    setError('')
    try {
      const [listResponse, totalResponse] = await Promise.all([
        fetch(apiUrl('/expenses')),
        fetch(apiUrl('/expenses/total')),
      ])
      if (!listResponse.ok || !totalResponse.ok) throw new Error('Could not load expenses. Please try again.')
      const [list, totalData] = await Promise.all([listResponse.json(), totalResponse.json()])
      setExpenses(list)
      setTotal(totalData.total)
    } catch (loadError) {
      setError(loadError.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadExpenses()
  }, [loadExpenses])

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)
    try {
      const response = await fetch(apiUrl('/expenses'), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...form, amount: Number(form.amount) }),
      })
      if (!response.ok) {
        const body = await response.json()
        throw new Error(body.message || Object.values(body)[0] || 'Could not add expense.')
      }
      setForm(emptyForm)
      await loadExpenses()
    } catch (submitError) {
      setError(submitError.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id) {
    setError('')
    try {
      const response = await fetch(apiUrl(`/expenses/${id}`), { method: 'DELETE' })
      if (!response.ok) throw new Error('Could not delete expense. Please try again.')
      await loadExpenses()
    } catch (deleteError) {
      setError(deleteError.message)
    }
  }

  return (
    <main className="page-shell">
      <header className="page-header">
        <div>
          <p className="eyebrow">PERSONAL FINANCE</p>
          <h1>Expense Manager</h1>
          <p className="subtitle">Keep track of where your money goes.</p>
        </div>
        <section className="total-card" aria-label="Total expenses">
          <span>Total expenses</span>
          <strong>{Number(total).toFixed(2)}</strong>
        </section>
      </header>

      {error && <p className="error-banner" role="alert">{error}</p>}

      <div className="content-grid">
        <section className="panel form-panel">
          <div className="section-heading">
            <h2>Add expense</h2>
            <p>Enter the details below.</p>
          </div>
          <form onSubmit={handleSubmit}>
            <label>
              Amount
              <input type="number" min="0.01" step="0.01" value={form.amount} onChange={(event) => setForm({ ...form, amount: event.target.value })} required />
            </label>
            <label>
              Category
              <input type="text" maxLength="100" value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} placeholder="e.g. Food" required />
            </label>
            <label>
              Date
              <input type="date" value={form.expenseDate} onChange={(event) => setForm({ ...form, expenseDate: event.target.value })} required />
            </label>
            <label>
              Description <span className="optional">(optional)</span>
              <textarea rows="3" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} placeholder="Add a note" />
            </label>
            <button className="primary-button" type="submit" disabled={saving}>{saving ? 'Adding…' : 'Add expense'}</button>
          </form>
        </section>

        <section className="panel list-panel">
          <div className="section-heading list-heading">
            <div>
              <h2>Expenses</h2>
              <p>Your recent spending</p>
            </div>
            <span className="count-badge">{expenses.length}</span>
          </div>
          {loading ? <p className="empty-state">Loading expenses…</p> : expenses.length === 0 ? (
            <p className="empty-state">No expenses yet. Add one to get started.</p>
          ) : (
            <ul className="expense-list">
              {expenses.map((expense) => (
                <li className="expense-row" key={expense.id}>
                  <div className="expense-icon" aria-hidden="true">{expense.category.slice(0, 1).toUpperCase()}</div>
                  <div className="expense-info">
                    <strong>{expense.category}</strong>
                    <span>{expense.description || 'No description'} · {expense.expenseDate}</span>
                  </div>
                  <strong className="expense-amount">{Number(expense.amount).toFixed(2)}</strong>
                  <button className="delete-button" type="button" onClick={() => handleDelete(expense.id)} aria-label={`Delete ${expense.category} expense`}>Delete</button>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </main>
  )
}

export default App
