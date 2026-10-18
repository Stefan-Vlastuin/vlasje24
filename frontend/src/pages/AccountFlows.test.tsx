import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { api, ApiError } from '../api/client'
import { renderWithProviders } from '../test/test-utils'
import { RegisterPage } from './RegisterPage'
import { ResetPasswordPage } from './ResetPasswordPage'
import { VerifyEmailPage } from './VerifyEmailPage'
import { AccountPage } from './AccountPage'
import type { AccountDto } from '../types/api'

const auth = vi.hoisted(() => ({
  refresh: vi.fn(),
  deleteAccount: vi.fn(),
  account: null as AccountDto | null,
  isLoading: false,
  error: null as Error | null,
}))

vi.mock('../auth/AuthContext', () => ({
  useAuth: () => auth,
}))

async function fillRegistration(passwordConfirmation = 'lang-wachtwoord') {
  const user = userEvent.setup()
  await user.type(screen.getByLabelText('Gebruikersnaam'), 'stefan')
  await user.type(screen.getByLabelText('E-mailadres'), 'stefan@example.nl')
  await user.type(screen.getByLabelText('Wachtwoord'), 'lang-wachtwoord')
  await user.type(screen.getByLabelText('Herhaal wachtwoord'), passwordConfirmation)
  return user
}

describe('accountflows', () => {
  beforeEach(() => {
    auth.refresh.mockReset()
    auth.deleteAccount.mockReset()
    auth.account = null
    auth.isLoading = false
    auth.error = null
  })

  it('houdt een registratie met verschillende wachtwoorden client-side tegen', async () => {
    const register = vi.spyOn(api, 'register')
    renderWithProviders(<RegisterPage />)
    const user = await fillRegistration('ander-wachtwoord')

    await user.click(screen.getByRole('button', { name: 'Account maken' }))

    expect(screen.getByText('De wachtwoorden zijn niet hetzelfde.')).toBeInTheDocument()
    expect(register).not.toHaveBeenCalled()
  })

  it('stuurt de vrijwillige lijstmeldingvoorkeur mee bij registratie', async () => {
    const register = vi.spyOn(api, 'register').mockResolvedValue({
      userId: 42,
      username: 'stefan',
      role: 'USER',
      emailVerified: false,
      chartNotificationsEnabled: true,
    })
    renderWithProviders(<RegisterPage />)
    const user = await fillRegistration()

    await user.click(screen.getByRole('checkbox', { name: /nieuwe Vlasje24-lijst/ }))
    await user.click(screen.getByRole('button', { name: 'Account maken' }))

    expect(register).toHaveBeenCalledWith(expect.objectContaining({
      chartNotificationsEnabled: true,
    }))
  })

  it('laat een geverifieerde gebruiker lijstmeldingen aanzetten', async () => {
    auth.account = {
      userId: 42,
      username: 'stefan',
      role: 'USER',
      emailVerified: true,
      chartNotificationsEnabled: false,
    }
    const update = vi.spyOn(api, 'updateChartNotifications').mockResolvedValue({
      ...auth.account,
      chartNotificationsEnabled: true,
    })
    const user = userEvent.setup()
    renderWithProviders(<AccountPage />, '/account')

    await user.click(screen.getByRole('checkbox', { name: 'E-mail mij bij een nieuwe lijst' }))

    expect(update).toHaveBeenCalledWith(true)
    expect(await screen.findByText(/Je ontvangt voortaan/)).toBeInTheDocument()
    expect(auth.refresh).toHaveBeenCalled()
  })

  it('toont veldfouten en een bestaand account uit de API', async () => {
    vi.spyOn(api, 'register').mockRejectedValue(new ApiError(
      409,
      'ACCOUNT_EXISTS',
      'Account bestaat al',
      { username: 'Deze gebruikersnaam is al in gebruik.' },
    ))
    renderWithProviders(<RegisterPage />)
    const user = await fillRegistration()

    await user.click(screen.getByRole('button', { name: 'Account maken' }))

    expect(await screen.findByText('Deze gebruikersnaam is al in gebruik.')).toBeInTheDocument()
    expect(screen.getByText(/Er bestaat al een account/)).toBeInTheDocument()
  })

  it('onderscheidt een al gebruikte verificatielink', async () => {
    vi.spyOn(api, 'verifyEmail').mockRejectedValue(
      new ApiError(400, 'TOKEN_USED', 'Token is al gebruikt'),
    )

    renderWithProviders(<VerifyEmailPage />, '/verify-email?token=gebruikt')

    expect(await screen.findByRole('heading', { name: 'Link al gebruikt' })).toBeInTheDocument()
    expect(screen.getByText(/Deze verificatielink is al gebruikt/)).toBeInTheDocument()
  })

  it('rondt een geldige wachtwoordreset af', async () => {
    const resetPassword = vi.spyOn(api, 'resetPassword').mockResolvedValue(undefined)
    const user = userEvent.setup()
    renderWithProviders(<ResetPasswordPage />, '/reset-password?token=geldig')

    await user.type(screen.getByLabelText('Nieuw wachtwoord'), 'nieuw-wachtwoord')
    await user.type(screen.getByLabelText('Herhaal nieuw wachtwoord'), 'nieuw-wachtwoord')
    await user.click(screen.getByRole('button', { name: 'Wachtwoord opslaan' }))

    expect(resetPassword).toHaveBeenCalledWith('geldig', 'nieuw-wachtwoord')
    expect(await screen.findByRole('heading', { name: 'Wachtwoord gewijzigd' })).toBeInTheDocument()
  })

  it('toont dat een resetlink al is gebruikt', async () => {
    vi.spyOn(api, 'resetPassword').mockRejectedValue(
      new ApiError(400, 'TOKEN_USED', 'Token is al gebruikt'),
    )
    const user = userEvent.setup()
    renderWithProviders(<ResetPasswordPage />, '/reset-password?token=gebruikt')

    await user.type(screen.getByLabelText('Nieuw wachtwoord'), 'nieuw-wachtwoord')
    await user.type(screen.getByLabelText('Herhaal nieuw wachtwoord'), 'nieuw-wachtwoord')
    await user.click(screen.getByRole('button', { name: 'Wachtwoord opslaan' }))

    expect(await screen.findByText(/Deze resetlink is al gebruikt/)).toBeInTheDocument()
  })
})
