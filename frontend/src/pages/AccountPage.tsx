import { useState } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { api, ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AccountPanel, FormError, FormSuccess, inputClass, primaryButtonClass } from '../components/account/AccountPanel'

export function AccountPage() {
  const { account, isLoading, error: authError } = useAuth()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [resendStatus, setResendStatus] = useState<'success' | 'error' | null>(null)
  const [resendMessage, setResendMessage] = useState('')
  const [sending, setSending] = useState(false)

  if (isLoading) return <AccountPanel title="Account"><p role="status" className="text-sm text-gray-600">Account laden…</p></AccountPanel>
  if (!account && !authError) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (!account) return <AccountPanel title="Account"><FormError>Je account kon niet worden geladen. Vernieuw de pagina om het opnieuw te proberen.</FormError></AccountPanel>

  async function resend(event: React.FormEvent) {
    event.preventDefault()
    setResendStatus(null)
    setSending(true)
    try {
      await api.resendVerification(email)
      setResendStatus('success')
      setResendMessage('Als het adres bij dit account hoort, ontvang je een nieuwe verificatiemail.')
    } catch (caught) {
      setResendStatus('error')
      setResendMessage(caught instanceof ApiError ? caught.message : 'Opnieuw verzenden lukt nu niet.')
    } finally {
      setSending(false)
    }
  }

  return (
    <AccountPanel title="Mijn account">
      <dl className="divide-y divide-gray-100 rounded-xl border border-gray-200">
        <div className="flex justify-between gap-4 p-4"><dt className="text-sm text-gray-500">Gebruikersnaam</dt><dd className="text-sm font-medium text-gray-900 break-all">{account.username}</dd></div>
        <div className="flex justify-between gap-4 p-4"><dt className="text-sm text-gray-500">Type account</dt><dd className="text-sm font-medium text-gray-900">{account.role === 'ADMIN' ? 'Beheerder' : 'Gebruiker'}</dd></div>
        <div className="flex justify-between gap-4 p-4"><dt className="text-sm text-gray-500">E-mailadres</dt><dd className={`text-sm font-medium ${account.emailVerified ? 'text-green-700' : 'text-amber-700'}`}>{account.emailVerified ? 'Geverifieerd' : 'Nog niet geverifieerd'}</dd></div>
      </dl>

      {!account.emailVerified && (
        <div className="mt-6 rounded-xl border border-amber-200 bg-amber-50 p-4">
          <h2 className="font-semibold text-amber-900">Verifieer je e-mailadres</h2>
          <p className="mt-1 text-sm leading-6 text-amber-800">Voor reacties is een geverifieerd adres nodig. Vul hetzelfde e-mailadres in als bij registratie om een nieuwe link te ontvangen.</p>
          <form onSubmit={resend} className="mt-3 space-y-3">
            <label htmlFor="account-email" className="sr-only">E-mailadres</label>
            <input id="account-email" type="email" value={email} onChange={event => setEmail(event.target.value)} required autoComplete="email" placeholder="jij@example.nl" className={inputClass} />
            <button type="submit" disabled={sending} className={primaryButtonClass}>{sending ? 'Versturen…' : 'Nieuwe verificatielink sturen'}</button>
            {resendStatus === 'success' && <FormSuccess>{resendMessage}</FormSuccess>}
            {resendStatus === 'error' && <FormError>{resendMessage}</FormError>}
          </form>
        </div>
      )}

      {account.role === 'ADMIN' && <Link to="/admin" className="mt-6 block text-center font-medium text-blue-700 hover:underline">Naar beheer</Link>}
    </AccountPanel>
  )
}
