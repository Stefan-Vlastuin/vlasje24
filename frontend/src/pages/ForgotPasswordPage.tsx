import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api, ApiError } from '../api/client'
import { AccountPanel, FormError, FormSuccess, inputClass, primaryButtonClass } from '../components/account/AccountPanel'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await api.forgotPassword(email)
      setSent(true)
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : 'Aanvragen lukt nu niet. Probeer het later opnieuw.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AccountPanel title="Wachtwoord vergeten" description="Vul je e-mailadres in. Als daar een actief account bij hoort, sturen we een resetlink.">
      {sent ? (
        <div className="space-y-4">
          <FormSuccess>Als het e-mailadres bij een account hoort, ontvang je binnen enkele minuten een e-mail.</FormSuccess>
          <p className="text-sm text-gray-600">Kijk ook in je spammap. De resetlink is 60 minuten geldig.</p>
          <Link to="/login" className="block text-center font-medium text-blue-700 hover:underline">Terug naar inloggen</Link>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="forgot-email" className="text-sm font-medium text-gray-700">E-mailadres</label>
            <input id="forgot-email" type="email" value={email} onChange={event => setEmail(event.target.value)} required autoComplete="email" autoFocus className={inputClass} />
          </div>
          {error && <FormError>{error}</FormError>}
          <button type="submit" disabled={loading} className={primaryButtonClass}>{loading ? 'Versturen…' : 'Resetlink versturen'}</button>
          <Link to="/login" className="block text-center text-sm text-blue-700 hover:underline">Terug naar inloggen</Link>
        </form>
      )}
    </AccountPanel>
  )
}
