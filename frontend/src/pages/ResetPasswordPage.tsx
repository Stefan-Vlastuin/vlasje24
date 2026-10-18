import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api, ApiError } from '../api/client'
import { AccountPanel, FormError, FormSuccess, inputClass, primaryButtonClass } from '../components/account/AccountPanel'

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [complete, setComplete] = useState(false)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError(null)
    if (password !== confirmation) {
      setError('De wachtwoorden zijn niet hetzelfde.')
      return
    }
    setLoading(true)
    try {
      await api.resetPassword(token, password)
      setComplete(true)
      setPassword('')
      setConfirmation('')
    } catch (caught) {
      if (caught instanceof ApiError && caught.code === 'TOKEN_USED') {
        setError('Deze resetlink is al gebruikt. Vraag zo nodig een nieuwe link aan.')
      } else if (caught instanceof ApiError && caught.code === 'INVALID_TOKEN') {
        setError('Deze resetlink is ongeldig of verlopen. Vraag een nieuwe link aan.')
      } else {
        setError(caught instanceof ApiError ? caught.message : 'Wachtwoord wijzigen lukt nu niet.')
      }
    } finally {
      setLoading(false)
    }
  }

  if (!token) {
    return (
      <AccountPanel title="Ongeldige resetlink">
        <div className="space-y-4"><FormError>In deze link ontbreekt een resetcode.</FormError><Link to="/forgot-password" className="block text-center font-medium text-blue-700 hover:underline">Nieuwe link aanvragen</Link></div>
      </AccountPanel>
    )
  }

  if (complete) {
    return (
      <AccountPanel title="Wachtwoord gewijzigd">
        <div className="space-y-4"><FormSuccess>Je nieuwe wachtwoord is opgeslagen. Bestaande sessies zijn beëindigd.</FormSuccess><Link to="/login" className="block text-center font-medium text-blue-700 hover:underline">Inloggen</Link></div>
      </AccountPanel>
    )
  }

  return (
    <AccountPanel title="Nieuw wachtwoord" description="Kies een nieuw wachtwoord van minimaal 12 tekens.">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="reset-password" className="text-sm font-medium text-gray-700">Nieuw wachtwoord</label>
          <input id="reset-password" type="password" value={password} onChange={event => setPassword(event.target.value)} required minLength={12} autoComplete="new-password" autoFocus className={inputClass} />
        </div>
        <div>
          <label htmlFor="reset-confirmation" className="text-sm font-medium text-gray-700">Herhaal nieuw wachtwoord</label>
          <input id="reset-confirmation" type="password" value={confirmation} onChange={event => setConfirmation(event.target.value)} required minLength={12} autoComplete="new-password" className={inputClass} />
        </div>
        {error && <FormError>{error}</FormError>}
        <button type="submit" disabled={loading} className={primaryButtonClass}>{loading ? 'Opslaan…' : 'Wachtwoord opslaan'}</button>
      </form>
    </AccountPanel>
  )
}
