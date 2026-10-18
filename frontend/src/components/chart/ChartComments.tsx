import { useEffect, useState } from 'react'
import { format } from 'date-fns'
import { nl } from 'date-fns/locale'
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useLocation } from 'react-router-dom'
import { ApiError, api } from '../../api/client'
import { useAuth } from '../../auth/AuthContext'
import type { CommentDto } from '../../types/api'

const PAGE_SIZE = 20

function errorMessage(error: unknown, fallback: string) {
  return error instanceof ApiError ? error.message : fallback
}

export function ChartComments({ weekId }: { weekId: number }) {
  const [page, setPage] = useState(0)
  const [body, setBody] = useState('')
  const [formError, setFormError] = useState('')
  const { account, isLoading: authLoading } = useAuth()
  const location = useLocation()
  const queryClient = useQueryClient()
  const queryKey = ['chart-comments', weekId] as const

  const commentsQuery = useQuery({
    queryKey: [...queryKey, page],
    queryFn: () => api.getComments(weekId, page, PAGE_SIZE),
    placeholderData: keepPreviousData,
  })

  const createMutation = useMutation({
    mutationFn: (text: string) => api.createComment(weekId, text),
    onSuccess: async () => {
      setBody('')
      setFormError('')
      setPage(0)
      await queryClient.invalidateQueries({ queryKey })
    },
    onError: error => setFormError(errorMessage(error, 'Reactie plaatsen lukt nu niet.')),
  })

  useEffect(() => {
    if (commentsQuery.data && page > 0 && page >= commentsQuery.data.totalPages) {
      setPage(Math.max(0, commentsQuery.data.totalPages - 1))
    }
  }, [commentsQuery.data, page])

  async function submitComment(event: React.FormEvent) {
    event.preventDefault()
    const trimmed = body.trim()
    if (!trimmed) {
      setFormError('Reactie mag niet leeg zijn.')
      return
    }
    if (trimmed.length > 1000) {
      setFormError('Reactie mag maximaal 1000 tekens bevatten.')
      return
    }
    setFormError('')
    createMutation.mutate(trimmed)
  }

  return (
    <section className="mt-10 border-t border-gray-200 pt-8" aria-labelledby="comments-heading">
      <div className="flex items-baseline justify-between gap-4">
        <h2 id="comments-heading" className="text-xl font-bold text-gray-900">Reacties</h2>
        {commentsQuery.data && (
          <span className="text-sm text-gray-500">
            {commentsQuery.data.totalElements} {commentsQuery.data.totalElements === 1 ? 'reactie' : 'reacties'}
          </span>
        )}
      </div>

      <div className="mt-4">
        {authLoading && <p role="status" className="text-sm text-gray-500">Account laden…</p>}
        {!authLoading && !account && (
          <p className="rounded-xl border border-blue-100 bg-blue-50 p-4 text-sm text-blue-900">
            <Link to="/login" state={{ from: location.pathname }} className="font-semibold underline">Log in</Link>
            {' '}of <Link to="/register" className="font-semibold underline">maak een account</Link> om te reageren.
          </p>
        )}
        {account && !account.emailVerified && (
          <p className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
            <Link to="/account" className="font-semibold underline">Verifieer je e-mailadres</Link> om een reactie te plaatsen.
          </p>
        )}
        {account?.emailVerified && (
          <form onSubmit={submitComment} className="space-y-2">
            <label htmlFor="new-comment" className="sr-only">Nieuwe reactie</label>
            <textarea
              id="new-comment"
              value={body}
              onChange={event => setBody(event.target.value)}
              maxLength={1000}
              rows={4}
              placeholder="Wat vind jij van deze lijst?"
              className="w-full resize-y rounded-xl border border-gray-300 px-3 py-2.5 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
            />
            <div className="flex items-center justify-between gap-3">
              <span className="text-xs text-gray-500">{body.length}/1000</span>
              <button
                type="submit"
                disabled={createMutation.isPending || !body.trim()}
                className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {createMutation.isPending ? 'Plaatsen…' : 'Reactie plaatsen'}
              </button>
            </div>
            {formError && <p role="alert" className="text-sm text-red-700">{formError}</p>}
          </form>
        )}
      </div>

      <div className="mt-6">
        {commentsQuery.isPending && (
          <p role="status" className="py-8 text-center text-sm text-gray-500">Reacties laden…</p>
        )}
        {commentsQuery.isError && (
          <div className="rounded-xl bg-red-50 p-4 text-sm text-red-700" role="alert">
            Reacties konden niet worden geladen.{' '}
            <button type="button" onClick={() => commentsQuery.refetch()} className="font-semibold underline">Probeer opnieuw</button>
          </div>
        )}
        {commentsQuery.data?.comments.length === 0 && (
          <p className="rounded-xl border border-dashed border-gray-300 py-8 text-center text-sm text-gray-500">
            Nog geen reacties. Wees de eerste!
          </p>
        )}
        {commentsQuery.data && commentsQuery.data.comments.length > 0 && (
          <div className="divide-y divide-gray-200 rounded-xl border border-gray-200 bg-white">
            {commentsQuery.data.comments.map(comment => (
              <CommentItem key={comment.commentId} comment={comment} weekId={weekId} />
            ))}
          </div>
        )}
      </div>

      {commentsQuery.data && commentsQuery.data.totalPages > 1 && (
        <nav className="mt-4 flex items-center justify-between" aria-label="Reactiepagina's">
          <button
            type="button"
            onClick={() => setPage(current => Math.max(0, current - 1))}
            disabled={page === 0}
            className="rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40"
          >
            Nieuwere reacties
          </button>
          <span className="text-sm text-gray-500">Pagina {page + 1} van {commentsQuery.data.totalPages}</span>
          <button
            type="button"
            onClick={() => setPage(current => current + 1)}
            disabled={page + 1 >= commentsQuery.data.totalPages}
            className="rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40"
          >
            Oudere reacties
          </button>
        </nav>
      )}
    </section>
  )
}

function CommentItem({ comment, weekId }: { comment: CommentDto; weekId: number }) {
  const { account } = useAuth()
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  const [body, setBody] = useState(comment.body)
  const [actionError, setActionError] = useState('')
  const canManage = account && (account.userId === comment.authorId || account.role === 'ADMIN')
  const queryKey = ['chart-comments', weekId] as const

  const editMutation = useMutation({
    mutationFn: () => api.editComment(comment.commentId, body.trim()),
    onSuccess: async () => {
      setEditing(false)
      setActionError('')
      await queryClient.invalidateQueries({ queryKey })
    },
    onError: error => setActionError(errorMessage(error, 'Bewerken lukt nu niet.')),
  })

  const deleteMutation = useMutation({
    mutationFn: () => api.deleteComment(comment.commentId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey }),
    onError: error => setActionError(errorMessage(error, 'Verwijderen lukt nu niet.')),
  })

  function remove() {
    if (window.confirm('Weet je zeker dat je deze reactie wilt verwijderen?')) {
      setActionError('')
      deleteMutation.mutate()
    }
  }

  const timestamp = format(new Date(comment.createdAt), 'd MMM yyyy, HH:mm', { locale: nl })

  return (
    <article className="p-4">
      <header className="flex flex-wrap items-center gap-x-2 gap-y-1 text-sm">
        <span className="font-semibold text-gray-900">{comment.username}</span>
        <time dateTime={comment.createdAt} className="text-xs text-gray-500">{timestamp}</time>
        {comment.edited && <span className="text-xs text-gray-400">bewerkt</span>}
      </header>

      {editing ? (
        <form
          onSubmit={event => {
            event.preventDefault()
            if (body.trim()) editMutation.mutate()
          }}
          className="mt-2 space-y-2"
        >
          <label htmlFor={`comment-${comment.commentId}`} className="sr-only">Reactie bewerken</label>
          <textarea
            id={`comment-${comment.commentId}`}
            value={body}
            onChange={event => setBody(event.target.value)}
            maxLength={1000}
            rows={3}
            className="w-full resize-y rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
          />
          <div className="flex justify-end gap-2">
            <button type="button" onClick={() => { setEditing(false); setBody(comment.body); setActionError('') }} className="rounded-lg px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100">Annuleren</button>
            <button type="submit" disabled={editMutation.isPending || !body.trim()} className="rounded-lg bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50">Opslaan</button>
          </div>
        </form>
      ) : (
        <p className="mt-2 whitespace-pre-wrap break-words text-sm leading-6 text-gray-700">{comment.body}</p>
      )}

      {canManage && !editing && (
        <div className="mt-2 flex gap-3 text-xs">
          <button type="button" onClick={() => setEditing(true)} className="font-medium text-blue-700 hover:underline">Bewerken</button>
          <button type="button" onClick={remove} disabled={deleteMutation.isPending} className="font-medium text-red-700 hover:underline disabled:opacity-50">
            {deleteMutation.isPending ? 'Verwijderen…' : 'Verwijderen'}
          </button>
        </div>
      )}
      {actionError && <p role="alert" className="mt-2 text-sm text-red-700">{actionError}</p>}
    </article>
  )
}
