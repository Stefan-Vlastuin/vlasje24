import type { AccountDto, ArtistDetailDto, ChartDto, CreatedDto, SearchResultDto, SongDetailDto, TopArtistsPageDto, TopSongsPageDto } from '../types/api'

const BASE_URL = '/api/v1'

interface ErrorResponse {
  code?: string
  message?: string
  fieldErrors?: Record<string, string>
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
    public readonly fieldErrors: Record<string, string> = {},
  ) {
    super(message)
  }
}

async function parseResponse<T>(res: Response): Promise<T> {
  if (res.ok) {
    if (res.status === 204) return undefined as T
    return res.json() as Promise<T>
  }

  let error: ErrorResponse = {}
  try {
    error = await res.json() as ErrorResponse
  } catch {
    // An upstream proxy can return a non-JSON error page.
  }
  throw new ApiError(
    res.status,
    error.code ?? 'HTTP_ERROR',
    error.message ?? `HTTP ${res.status}: ${res.statusText}`,
    error.fieldErrors,
  )
}

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    credentials: 'include',
  })
  return parseResponse<T>(res)
}

function readCookie(name: string): string | null {
  const prefix = `${name}=`
  const cookie = document.cookie.split('; ').find(value => value.startsWith(prefix))
  return cookie ? decodeURIComponent(cookie.substring(prefix.length)) : null
}

let pendingCsrfToken: Promise<string> | null = null

async function csrfToken(): Promise<string> {
  const cookieToken = readCookie('XSRF-TOKEN')
  if (cookieToken) return cookieToken
  if (!pendingCsrfToken) {
    pendingCsrfToken = get<{ token: string }>('/auth/csrf')
      .then(response => response.token)
      .finally(() => { pendingCsrfToken = null })
  }
  return pendingCsrfToken
}

async function mutate<T>(method: 'POST' | 'PATCH' | 'DELETE', path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {
    'X-XSRF-TOKEN': await csrfToken(),
  }
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    credentials: 'include',
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  return parseResponse<T>(res)
}

export const api = {
  getLatestChart: () => get<ChartDto>('/charts/latest'),
  getChart: (weekId: number) => get<ChartDto>(`/charts/${weekId}`),
  getSong: (songId: number) => get<SongDetailDto>(`/songs/${songId}`),
  getArtist: (artistId: number) => get<ArtistDetailDto>(`/artists/${artistId}`),
  search: (q: string) => get<SearchResultDto>(`/search?q=${encodeURIComponent(q)}`),
  getTopSongs: (sort: string, year: number | null, page: number) => {
    const params = new URLSearchParams({ sort, page: String(page) })
    if (year !== null) params.set('year', String(year))
    return get<TopSongsPageDto>(`/songs/top?${params}`)
  },
  getYears: () => get<number[]>('/charts/years'),
  getTopArtists: (sort: string, year: number | null, page: number) => {
    const params = new URLSearchParams({ sort, page: String(page) })
    if (year !== null) params.set('year', String(year))
    return get<TopArtistsPageDto>(`/artists/top?${params}`)
  },
  login: (username: string, password: string) =>
    mutate<AccountDto>('POST', '/auth/login', { username, password }),
  register: (data: { username: string; email: string; password: string }) =>
    mutate<AccountDto>('POST', '/auth/register', data),
  getCurrentAccount: () => get<AccountDto>('/auth/me'),
  logout: () => mutate<void>('POST', '/auth/logout'),
  createArtist: (name: string) =>
    mutate<CreatedDto>('POST', '/admin/artists', { name }),
  createSong: (data: { title: string; imageUrl: string; previewUrl: string; artistIds: number[] }) =>
    mutate<CreatedDto>('POST', '/admin/songs', data),
  createChart: (data: { date: string; songIds: number[] }) =>
    mutate<CreatedDto>('POST', '/admin/charts', data),
}
