import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api, ApiError } from '../api/client'
import { AccountPanel, FormError, FormSuccess, inputClass, primaryButtonClass } from '../components/account/AccountPanel'

type Fields = 'username' | 'email' | 'password'

export function RegisterPage() {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<Fields, string>>>({})
  const [error, setError] = useState<string | null>(null)
  const [registeredEmail, setRegisteredEmail] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [resendStatus, setResendStatus] = useState<string | null>(null)

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError(null)
    setFieldErrors({})
    if (password !== confirmation) {
      setFieldErrors({ password: 'De wachtwoorden zijn niet hetzelfde.' })
      return
    }

    setLoading(true)
    try {
      await api.register({ username, email, password })
      setRegisteredEmail(email.trim())
      setPassword('')
      setConfirmation('')
    } catch (caught) {
      if (caught instanceof ApiError) {
        setFieldErrors(caught.fieldErrors)
        setError(caught.code === 'ACCOUNT_EXISTS'
          ? 'Er bestaat al een account met deze gebruikersnaam of dit e-mailadres.'
          : caught.message)
      } else {
        setError('Registreren lukt nu niet. Probeer het later opnieuw.')
      }
    } finally {
      setLoading(false)
    }
  }

  async function resend() {
    if (!registeredEmail) return
    setResendStatus(null)
    try {
      await api.resendVerification(registeredEmail)
      setResendStatus('Als het account bestaat en nog niet is geverifieerd, ontvang je een nieuwe e-mail.')
    } catch (caught) {
      setResendStatus(caught instanceof ApiError ? caught.message : 'Opnieuw verzenden lukt nu niet.')
    }
  }

  if (registeredEmail) {
    return (
      <AccountPanel title="Controleer je e-mail" description={`We hebben een verificatielink gestuurd naar ${registeredEmail}. Open die link om je account te activeren.`}>
        <div className="space-y-4">
          <FormSuccess>Je account is aangemaakt. De verificatielink is 24 uur geldig.</FormSuccess>
          <p className="text-sm leading-6 text-gray-600">Geen mail ontvangen? Kijk eerst in je spammap. Je kunt daarna een nieuwe mail aanvragen.</p>
          <button type="button" onClick={resend} className="w-full rounded-lg border border-blue-600 px-4 py-2.5 font-medium text-blue-700 hover:bg-blue-50">
            Verificatiemail opnieuw versturen
          </button>
          {resendStatus && <p role="status" className="text-sm text-gray-600">{resendStatus}</p>}
          <Link to="/login" className="block text-center text-sm font-medium text-blue-700 hover:underline">Naar inloggen</Link>
        </div>
      </AccountPanel>
    )
  }

  return (
    <AccountPanel title="Account maken" description="Met een account kun je straks reageren op de hitlijst.">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="register-username" className="text-sm font-medium text-gray-700">Gebruikersnaam</label>
          <input id="register-username" value={username} onChange={event => setUsername(event.target.value)} required minLength={3} maxLength={30} pattern="[A-Za-z0-9_-]+" autoComplete="username" autoFocus className={inputClass} />
          <p className="mt-1 text-xs text-gray-500">3–30 letters, cijfers, underscores of streepjes.</p>
          {fieldErrors.username && <p className="mt-1 text-sm text-red-600">{fieldErrors.username}</p>}
        </div>
        <div>
          <label htmlFor="register-email" className="text-sm font-medium text-gray-700">E-mailadres</label>
          <input id="register-email" type="email" value={email} onChange={event => setEmail(event.target.value)} required maxLength={254} autoComplete="email" className={inputClass} />
          {fieldErrors.email && <p className="mt-1 text-sm text-red-600">{fieldErrors.email}</p>}
        </div>
        <div>
          <label htmlFor="register-password" className="text-sm font-medium text-gray-700">Wachtwoord</label>
          <input id="register-password" type="password" value={password} onChange={event => setPassword(event.target.value)} required minLength={12} autoComplete="new-password" className={inputClass} />
          <p className="mt-1 text-xs text-gray-500">Minimaal 12 tekens.</p>
          {fieldErrors.password && <p className="mt-1 text-sm text-red-600">{fieldErrors.password}</p>}
        </div>
        <div>
          <label htmlFor="register-confirmation" className="text-sm font-medium text-gray-700">Herhaal wachtwoord</label>
          <input id="register-confirmation" type="password" value={confirmation} onChange={event => setConfirmation(event.target.value)} required minLength={12} autoComplete="new-password" className={inputClass} />
        </div>
        {error && <FormError>{error}</FormError>}
        <p className="text-xs leading-5 text-gray-500">Door een account te maken ga je akkoord met de <Link to="/huisregels" className="text-blue-700 hover:underline">huisregels</Link> en lees je hoe Vlasje24 je gegevens verwerkt in de <Link to="/privacy" className="text-blue-700 hover:underline">privacyverklaring</Link>.</p>
        <button type="submit" disabled={loading} className={primaryButtonClass}>{loading ? 'Account maken…' : 'Account maken'}</button>
      </form>
      <p className="mt-5 text-center text-sm text-gray-600">Al een account? <Link to="/login" className="font-medium text-blue-700 hover:underline">Inloggen</Link></p>
    </AccountPanel>
  )
}
