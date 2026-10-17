import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AccountPanel, FormError, inputClass, primaryButtonClass } from '../components/account/AccountPanel'

export function AccountLoginPage() {
  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const { account, isLoading, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const requestedPath = (location.state as { from?: string } | null)?.from
  const destination = requestedPath?.startsWith('/') && !requestedPath.startsWith('//') && requestedPath !== '/login'
    ? requestedPath
    : '/account'

  if (!isLoading && account) return <Navigate to={destination} replace />

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login(identifier, password)
      navigate(destination, { replace: true })
    } catch (caught) {
      setError(caught instanceof ApiError && caught.code === 'RATE_LIMITED'
        ? caught.message
        : 'De gebruikersnaam, het e-mailadres of het wachtwoord klopt niet.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AccountPanel title="Inloggen" description="Log in met je gebruikersnaam of e-mailadres.">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="login-identifier" className="text-sm font-medium text-gray-700">Gebruikersnaam of e-mailadres</label>
          <input
            id="login-identifier"
            type="text"
            autoComplete="username"
            value={identifier}
            onChange={event => setIdentifier(event.target.value)}
            required
            autoFocus
            className={inputClass}
          />
        </div>
        <div>
          <div className="flex items-center justify-between gap-3">
            <label htmlFor="login-password" className="text-sm font-medium text-gray-700">Wachtwoord</label>
            <Link to="/forgot-password" className="text-sm text-blue-700 hover:underline">Wachtwoord vergeten?</Link>
          </div>
          <input
            id="login-password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={event => setPassword(event.target.value)}
            required
            className={inputClass}
          />
        </div>
        {error && <FormError>{error}</FormError>}
        <button type="submit" disabled={loading || isLoading} className={primaryButtonClass}>
          {loading ? 'Inloggen…' : 'Inloggen'}
        </button>
      </form>
      <p className="mt-5 text-center text-sm text-gray-600">
        Nog geen account? <Link to="/register" className="font-medium text-blue-700 hover:underline">Registreren</Link>
      </p>
    </AccountPanel>
  )
}
