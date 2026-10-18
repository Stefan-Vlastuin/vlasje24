import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { api } from '../../api/client'
import type { AccountDto, CommentDto, CommentPageDto } from '../../types/api'
import { renderWithProviders } from '../../test/test-utils'
import { ChartComments } from './ChartComments'

const auth = vi.hoisted(() => ({
  account: null as AccountDto | null,
  isLoading: false,
}))

vi.mock('../../auth/AuthContext', () => ({
  useAuth: () => auth,
}))

const member: AccountDto = {
  userId: 42,
  username: 'stefan',
  role: 'USER',
  emailVerified: true,
}

const comment: CommentDto = {
  commentId: 1,
  authorId: 42,
  username: 'stefan',
  body: 'Mooie lijst!',
  createdAt: '2026-10-17T10:00:00Z',
  updatedAt: '2026-10-17T10:00:00Z',
  edited: false,
}

function page(comments: CommentDto[] = []): CommentPageDto {
  return {
    comments,
    page: 0,
    size: 20,
    totalElements: comments.length,
    totalPages: comments.length ? 1 : 0,
  }
}

describe('ChartComments', () => {
  beforeEach(() => {
    auth.account = null
    auth.isLoading = false
    vi.spyOn(api, 'getComments').mockResolvedValue(page())
  })

  it('toont gasten een inlogroute en geen invoerveld', async () => {
    renderWithProviders(<ChartComments weekId={7} />, '/hitlijst')

    expect(await screen.findByRole('link', { name: 'Log in' })).toHaveAttribute('href', '/login')
    expect(screen.queryByLabelText('Nieuwe reactie')).not.toBeInTheDocument()
  })

  it('laat een niet-geverifieerd account nog niet reageren', async () => {
    auth.account = { ...member, emailVerified: false }
    renderWithProviders(<ChartComments weekId={7} />)

    expect(await screen.findByRole('link', { name: 'Verifieer je e-mailadres' })).toBeInTheDocument()
    expect(screen.queryByLabelText('Nieuwe reactie')).not.toBeInTheDocument()
  })

  it('plaatst een getrimde reactie en houdt de bediening toetsenbordtoegankelijk', async () => {
    auth.account = member
    const createComment = vi.spyOn(api, 'createComment').mockResolvedValue(comment)
    const user = userEvent.setup()
    renderWithProviders(<ChartComments weekId={7} />)
    const input = await screen.findByLabelText('Nieuwe reactie')

    await user.click(input)
    await user.type(input, '  Mooie lijst!  ')
    await user.tab()
    expect(screen.getByRole('button', { name: 'Reactie plaatsen' })).toHaveFocus()
    await user.keyboard('{Enter}')

    expect(createComment).toHaveBeenCalledWith(7, 'Mooie lijst!')
  })

  it.each([
    ['de eigenaar', member],
    ['een beheerder', { ...member, userId: 99, role: 'ADMIN' as const }],
  ])('toont beheeracties aan %s', async (_description, account) => {
    auth.account = account
    vi.mocked(api.getComments).mockResolvedValue(page([comment]))
    renderWithProviders(<ChartComments weekId={7} />)

    expect(await screen.findByRole('button', { name: 'Bewerken' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Verwijderen' })).toBeInTheDocument()
  })

  it('verbergt beheeracties voor een andere gewone gebruiker', async () => {
    auth.account = { ...member, userId: 99 }
    vi.mocked(api.getComments).mockResolvedValue(page([comment]))
    renderWithProviders(<ChartComments weekId={7} />)

    expect(await screen.findByText('Mooie lijst!')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Bewerken' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Verwijderen' })).not.toBeInTheDocument()
  })

  it('stuurt wijzigingen en bevestigde verwijderingen naar de API', async () => {
    auth.account = member
    vi.mocked(api.getComments).mockResolvedValue(page([comment]))
    const editComment = vi.spyOn(api, 'editComment').mockResolvedValue({
      ...comment,
      body: 'Aangepast',
      edited: true,
    })
    const deleteComment = vi.spyOn(api, 'deleteComment').mockResolvedValue(undefined)
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    const user = userEvent.setup()
    renderWithProviders(<ChartComments weekId={7} />)

    await user.click(await screen.findByRole('button', { name: 'Bewerken' }))
    const editInput = screen.getByLabelText('Reactie bewerken')
    await user.clear(editInput)
    await user.type(editInput, '  Aangepast  ')
    await user.click(screen.getByRole('button', { name: 'Opslaan' }))
    expect(editComment).toHaveBeenCalledWith(1, 'Aangepast')

    await user.click(await screen.findByRole('button', { name: 'Verwijderen' }))
    expect(window.confirm).toHaveBeenCalled()
    expect(deleteComment).toHaveBeenCalledWith(1)
  })
})
