import { QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { api, ApiError } from '../api/client'
import type { AccountDto } from '../types/api'
import { createTestQueryClient } from '../test/test-utils'
import { AuthProvider, useAuth } from './AuthContext'

const member: AccountDto = {
  userId: 42,
  username: 'stefan',
  role: 'USER',
  emailVerified: true,
}

function AuthProbe() {
  const { account, isLoading, login, logout } = useAuth()
  if (isLoading) return <p>laden</p>
  return (
    <div>
      <p>{account?.username ?? 'gast'}</p>
      <button type="button" onClick={() => void login('stefan', 'wachtwoord')}>inloggen</button>
      <button type="button" onClick={() => void logout()}>uitloggen</button>
    </div>
  )
}

function renderAuthProbe() {
  return render(
    <QueryClientProvider client={createTestQueryClient()}>
      <AuthProvider><AuthProbe /></AuthProvider>
    </QueryClientProvider>,
  )
}

describe('AuthProvider', () => {
  it('herstelt een bestaande sessie tijdens het laden', async () => {
    vi.spyOn(api, 'getCurrentAccount').mockResolvedValue(member)

    renderAuthProbe()

    expect(await screen.findByText('stefan')).toBeInTheDocument()
  })

  it('behandelt een 401 bij de initiële sessiecontrole als gast', async () => {
    vi.spyOn(api, 'getCurrentAccount').mockRejectedValue(
      new ApiError(401, 'UNAUTHORIZED', 'Niet ingelogd'),
    )

    renderAuthProbe()

    expect(await screen.findByText('gast')).toBeInTheDocument()
  })

  it('werkt de centrale accountstatus bij na in- en uitloggen', async () => {
    vi.spyOn(api, 'getCurrentAccount').mockRejectedValue(
      new ApiError(401, 'UNAUTHORIZED', 'Niet ingelogd'),
    )
    vi.spyOn(api, 'login').mockResolvedValue(member)
    vi.spyOn(api, 'logout').mockResolvedValue(undefined)
    const user = userEvent.setup()

    renderAuthProbe()
    await screen.findByText('gast')

    await user.click(screen.getByRole('button', { name: 'inloggen' }))
    expect(await screen.findByText('stefan')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'uitloggen' }))
    expect(await screen.findByText('gast')).toBeInTheDocument()
  })
})
