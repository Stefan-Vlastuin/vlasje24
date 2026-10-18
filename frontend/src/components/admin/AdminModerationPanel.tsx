import { useState } from 'react'
import { format } from 'date-fns'
import { nl } from 'date-fns/locale'
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { ApiError, api } from '../../api/client'
import type { AdminCommentDto, AdminUserDto, ModerationAction } from '../../types/api'

type Section = 'comments' | 'users' | 'audit'
type CommentFilter = 'ALL' | 'VISIBLE' | 'DELETED'

const PAGE_SIZE = 20

function message(error: unknown, fallback: string) {
  return error instanceof ApiError ? error.message : fallback
}

function timestamp(value: string) {
  return format(new Date(value), 'd MMM yyyy, HH:mm', { locale: nl })
}

export function AdminModerationPanel() {
  const [section, setSection] = useState<Section>('comments')
  const sections: { key: Section; label: string }[] = [
    { key: 'comments', label: 'Reacties' },
    { key: 'users', label: 'Accounts' },
    { key: 'audit', label: 'Auditlog' },
  ]

  return (
    <div>
      <div className="mb-5 flex flex-wrap gap-2" role="tablist" aria-label="Moderatieonderdelen">
        {sections.map(item => (
          <button
            key={item.key}
            type="button"
            role="tab"
            aria-selected={section === item.key}
            onClick={() => setSection(item.key)}
            className={`rounded-full px-3 py-1.5 text-sm font-medium ${section === item.key ? 'bg-gray-900 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
          >
            {item.label}
          </button>
        ))}
      </div>
      {section === 'comments' && <CommentsAdmin />}
      {section === 'users' && <UsersAdmin />}
      {section === 'audit' && <AuditAdmin />}
    </div>
  )
}

function CommentsAdmin() {
  const [filter, setFilter] = useState<CommentFilter>('ALL')
  const [page, setPage] = useState(0)
  const [error, setError] = useState('')
  const queryClient = useQueryClient()
  const queryKey = ['admin', 'comments', filter, page] as const
  const query = useQuery({
    queryKey,
    queryFn: () => api.getAdminComments(filter, page, PAGE_SIZE),
    placeholderData: keepPreviousData,
  })
  const remove = useMutation({
    mutationFn: ({ commentId, reason }: { commentId: number; reason: string }) =>
      api.moderateComment(commentId, reason),
    onSuccess: async () => {
      setError('')
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['admin', 'comments'] }),
        queryClient.invalidateQueries({ queryKey: ['admin', 'audit'] }),
        queryClient.invalidateQueries({ queryKey: ['chart-comments'] }),
      ])
    },
    onError: caught => setError(message(caught, 'Reactie verwijderen lukt nu niet.')),
  })

  function moderate(comment: AdminCommentDto) {
    const reason = window.prompt('Interne reden (optioneel). Kies Annuleren om niets te wijzigen.', '')
    if (reason === null) return
    remove.mutate({ commentId: comment.commentId, reason })
  }

  return (
    <section aria-labelledby="admin-comments-heading">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 id="admin-comments-heading" className="text-lg font-semibold text-gray-900">Recente reacties</h2>
          <p className="mt-1 text-sm text-gray-500">Ook door auteurs en moderators verwijderde reacties blijven hier zichtbaar.</p>
        </div>
        <select
          value={filter}
          onChange={event => { setFilter(event.target.value as CommentFilter); setPage(0) }}
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm"
          aria-label="Reactiestatus"
        >
          <option value="ALL">Alle reacties</option>
          <option value="VISIBLE">Zichtbaar</option>
          <option value="DELETED">Verwijderd</option>
        </select>
      </div>

      {error && <p role="alert" className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}
      {query.isPending && <p role="status" className="py-8 text-center text-sm text-gray-500">Reacties laden…</p>}
      {query.isError && <LoadError retry={() => query.refetch()} />}
      {query.data?.comments.length === 0 && <EmptyState>Geen reacties binnen dit filter.</EmptyState>}
      {query.data && query.data.comments.length > 0 && (
        <div className="mt-5 space-y-3">
          {query.data.comments.map(comment => (
            <article key={comment.commentId} className="rounded-xl border border-gray-200 p-4">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2 text-sm">
                    <span className="font-semibold text-gray-900">{comment.username}</span>
                    <span className="text-xs text-gray-500">#{comment.commentId} · {timestamp(comment.createdAt)}</span>
                    <StatusBadge value={comment.status} />
                  </div>
                  <Link to={`/chart/${comment.chartWeekId}`} className="mt-1 inline-block text-xs font-medium text-blue-700 hover:underline">
                    Chartweek {comment.chartWeekId}
                  </Link>
                </div>
                {comment.status === 'VISIBLE' && (
                  <button
                    type="button"
                    onClick={() => moderate(comment)}
                    disabled={remove.isPending}
                    className="rounded-lg bg-red-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-red-700 disabled:opacity-50"
                  >
                    Verwijderen
                  </button>
                )}
              </div>
              <p className="mt-3 whitespace-pre-wrap break-words text-sm leading-6 text-gray-700">{comment.body}</p>
            </article>
          ))}
        </div>
      )}
      {query.data && <Pagination page={page} totalPages={query.data.totalPages} setPage={setPage} />}
    </section>
  )
}

function UsersAdmin() {
  const [input, setInput] = useState('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [error, setError] = useState('')
  const queryClient = useQueryClient()
  const query = useQuery({
    queryKey: ['admin', 'users', search, page],
    queryFn: () => api.getAdminUsers(search, page, PAGE_SIZE),
    placeholderData: keepPreviousData,
  })
  const suspension = useMutation({
    mutationFn: ({ user, suspended, reason }: { user: AdminUserDto; suspended: boolean; reason: string }) =>
      api.updateUserSuspension(user.userId, suspended, reason),
    onSuccess: async () => {
      setError('')
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['admin', 'users'] }),
        queryClient.invalidateQueries({ queryKey: ['admin', 'audit'] }),
      ])
    },
    onError: caught => setError(message(caught, 'Accountstatus wijzigen lukt nu niet.')),
  })

  function update(user: AdminUserDto) {
    const suspended = user.status !== 'SUSPENDED'
    const reason = window.prompt(
      suspended
        ? 'Interne reden voor blokkeren (optioneel). Kies Annuleren om niets te wijzigen.'
        : 'Interne reden voor deblokkeren (optioneel). Kies Annuleren om niets te wijzigen.',
      '',
    )
    if (reason === null) return
    suspension.mutate({ user, suspended, reason })
  }

  return (
    <section aria-labelledby="admin-users-heading">
      <h2 id="admin-users-heading" className="text-lg font-semibold text-gray-900">Accounts</h2>
      <form
        onSubmit={event => { event.preventDefault(); setSearch(input.trim()); setPage(0) }}
        className="mt-4 flex gap-2"
      >
        <label htmlFor="admin-user-search" className="sr-only">Zoek op gebruikersnaam</label>
        <input
          id="admin-user-search"
          value={input}
          onChange={event => setInput(event.target.value)}
          placeholder="Zoek op gebruikersnaam"
          className="min-w-0 flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm"
        />
        <button type="submit" className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700">Zoeken</button>
      </form>

      {error && <p role="alert" className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}
      {query.isPending && <p role="status" className="py-8 text-center text-sm text-gray-500">Accounts laden…</p>}
      {query.isError && <LoadError retry={() => query.refetch()} />}
      {query.data?.users.length === 0 && <EmptyState>Geen accounts gevonden.</EmptyState>}
      {query.data && query.data.users.length > 0 && (
        <div className="mt-5 overflow-x-auto rounded-xl border border-gray-200">
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-gray-50 text-left text-xs uppercase tracking-wide text-gray-500">
              <tr><th className="px-4 py-3">Account</th><th className="px-4 py-3">Status</th><th className="px-4 py-3">Aangemaakt</th><th className="px-4 py-3"><span className="sr-only">Actie</span></th></tr>
            </thead>
            <tbody className="divide-y divide-gray-200 bg-white">
              {query.data.users.map(user => (
                <tr key={user.userId}>
                  <td className="px-4 py-3"><span className="font-medium text-gray-900">{user.username}</span><span className="ml-2 text-xs text-gray-500">{user.role}</span></td>
                  <td className="px-4 py-3"><StatusBadge value={user.status} /></td>
                  <td className="whitespace-nowrap px-4 py-3 text-gray-500">{timestamp(user.createdAt)}</td>
                  <td className="px-4 py-3 text-right">
                    {user.role === 'USER' && user.status !== 'DELETED' && (
                      <button
                        type="button"
                        onClick={() => update(user)}
                        disabled={suspension.isPending}
                        className={`font-medium hover:underline disabled:opacity-50 ${user.status === 'SUSPENDED' ? 'text-green-700' : 'text-red-700'}`}
                      >
                        {user.status === 'SUSPENDED' ? 'Deblokkeren' : 'Blokkeren'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {query.data && <Pagination page={page} totalPages={query.data.totalPages} setPage={setPage} />}
    </section>
  )
}

function AuditAdmin() {
  const [page, setPage] = useState(0)
  const query = useQuery({
    queryKey: ['admin', 'audit', page],
    queryFn: () => api.getModerationAudit(page, PAGE_SIZE),
    placeholderData: keepPreviousData,
  })
  const labels: Record<ModerationAction, string> = {
    COMMENT_DELETED: 'Reactie verwijderd',
    USER_SUSPENDED: 'Account geblokkeerd',
    USER_REACTIVATED: 'Account gedeblokkeerd',
  }

  return (
    <section aria-labelledby="admin-audit-heading">
      <h2 id="admin-audit-heading" className="text-lg font-semibold text-gray-900">Moderatie-auditlog</h2>
      <p className="mt-1 text-sm text-gray-500">Interne redenen zijn alleen in dit beheeroverzicht zichtbaar.</p>
      {query.isPending && <p role="status" className="py-8 text-center text-sm text-gray-500">Auditlog laden…</p>}
      {query.isError && <LoadError retry={() => query.refetch()} />}
      {query.data?.actions.length === 0 && <EmptyState>Nog geen moderatieacties.</EmptyState>}
      {query.data && query.data.actions.length > 0 && (
        <ol className="mt-5 divide-y divide-gray-200 rounded-xl border border-gray-200">
          {query.data.actions.map(action => (
            <li key={action.auditId} className="p-4 text-sm">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <span className="font-semibold text-gray-900">{labels[action.action]}</span>
                <time className="text-xs text-gray-500" dateTime={action.createdAt}>{timestamp(action.createdAt)}</time>
              </div>
              <p className="mt-1 text-gray-600">Door {action.adminUsername} · account {action.targetUsername}{action.targetCommentId ? ` · reactie #${action.targetCommentId}` : ''}</p>
              {action.reason && <p className="mt-2 rounded-lg bg-gray-50 px-3 py-2 text-gray-700">Reden: {action.reason}</p>}
            </li>
          ))}
        </ol>
      )}
      {query.data && <Pagination page={page} totalPages={query.data.totalPages} setPage={setPage} />}
    </section>
  )
}

function Pagination({ page, totalPages, setPage }: { page: number; totalPages: number; setPage: (page: number) => void }) {
  if (totalPages <= 1) return null
  return (
    <nav className="mt-5 flex items-center justify-between gap-3" aria-label="Paginering">
      <button type="button" onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0} className="rounded-lg border border-gray-300 px-3 py-2 text-sm disabled:opacity-40">Vorige</button>
      <span className="text-sm text-gray-500">Pagina {page + 1} van {totalPages}</span>
      <button type="button" onClick={() => setPage(page + 1)} disabled={page + 1 >= totalPages} className="rounded-lg border border-gray-300 px-3 py-2 text-sm disabled:opacity-40">Volgende</button>
    </nav>
  )
}

function StatusBadge({ value }: { value: string }) {
  const labels: Record<string, string> = {
    VISIBLE: 'Zichtbaar',
    DELETED_BY_AUTHOR: 'Verwijderd door auteur',
    DELETED_BY_MODERATOR: 'Verwijderd door moderator',
    ACTIVE: 'Actief',
    SUSPENDED: 'Geblokkeerd',
    DELETED: 'Verwijderd',
  }
  const warning = value === 'SUSPENDED' || value.startsWith('DELETED')
  return <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${warning ? 'bg-red-50 text-red-700' : 'bg-green-50 text-green-700'}`}>{labels[value] ?? value}</span>
}

function LoadError({ retry }: { retry: () => void }) {
  return <p role="alert" className="mt-5 rounded-lg bg-red-50 p-3 text-sm text-red-700">Laden mislukt. <button type="button" onClick={retry} className="font-semibold underline">Probeer opnieuw</button></p>
}

function EmptyState({ children }: { children: React.ReactNode }) {
  return <p className="mt-5 rounded-xl border border-dashed border-gray-300 py-8 text-center text-sm text-gray-500">{children}</p>
}
