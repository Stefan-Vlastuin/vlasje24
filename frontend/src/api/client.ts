import type { ArtistDetailDto, ChartDto, CreatedDto, SearchResultDto, SongDetailDto, TopArtistsPageDto, TopSongsPageDto } from '../types/api'

const BASE_URL = '/api/v1'

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`)
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}: ${res.statusText}`)
  }
  return res.json() as Promise<T>
}

async function post<T>(path: string, body: unknown, token?: string): Promise<T> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`
  const res = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers,
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    const text = await res.text()
    throw new Error(text || `HTTP ${res.status}: ${res.statusText}`)
  }
  return res.json() as Promise<T>
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
    post<{ token: string }>('/auth/login', { username, password }),
  createArtist: (name: string, token: string) =>
    post<CreatedDto>('/admin/artists', { name }, token),
  createSong: (data: { title: string; imageUrl: string; previewUrl: string; artistIds: number[] }, token: string) =>
    post<CreatedDto>('/admin/songs', data, token),
  createChart: (data: { date: string; songIds: number[] }, token: string) =>
    post<CreatedDto>('/admin/charts', data, token),
}
