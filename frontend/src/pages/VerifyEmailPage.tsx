import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router-dom'
import { api, ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AccountPanel, FormError, FormSuccess } from '../components/account/AccountPanel'

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const { refresh } = useAuth()
  const verification = useQuery({
    queryKey: ['verify-email', token],
    queryFn: async () => {
      await api.verifyEmail(token)
      return true
    },
    enabled: token.length > 0,
    retry: false,
    staleTime: Infinity,
    refetchOnWindowFocus: false,
  })

  useEffect(() => {
    if (verification.isSuccess) void refresh()
  }, [verification.isSuccess, refresh])

  if (!token) {
    return <AccountPanel title="Ongeldige verificatielink"><FormError>In deze link ontbreekt een verificatiecode.</FormError></AccountPanel>
  }

  if (verification.isPending) {
    return <AccountPanel title="E-mailadres verifiëren"><p role="status" className="text-sm text-gray-600">Je verificatielink wordt gecontroleerd…</p></AccountPanel>
  }

  if (verification.isError) {
    const used = verification.error instanceof ApiError && verification.error.code === 'TOKEN_USED'
    return (
      <AccountPanel title={used ? 'Link al gebruikt' : 'Verificatielink ongeldig'}>
        <div className="space-y-4">
          <FormError>{used ? 'Deze verificatielink is al gebruikt. Je kunt proberen in te loggen.' : 'Deze link is ongeldig of verlopen. Vraag vanuit je account een nieuwe link aan.'}</FormError>
          <Link to="/login" className="block text-center font-medium text-blue-700 hover:underline">Naar inloggen</Link>
        </div>
      </AccountPanel>
    )
  }

  return (
    <AccountPanel title="E-mailadres geverifieerd">
      <div className="space-y-4">
        <FormSuccess>Je e-mailadres is bevestigd. Je account is klaar voor gebruik.</FormSuccess>
        <Link to="/login" className="block text-center font-medium text-blue-700 hover:underline">Inloggen</Link>
      </div>
    </AccountPanel>
  )
}
