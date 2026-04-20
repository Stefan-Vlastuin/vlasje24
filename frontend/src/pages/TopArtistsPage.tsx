import { useState } from 'react'
import { useInfiniteQuery, useQuery } from '@tanstack/react-query'
import { api } from '../api/client'
import { TopArtistEntryRow } from '../components/chart/TopArtistEntryRow'

type SortKey = 'points' | 'hits'

const SORT_OPTIONS: { key: SortKey; label: string }[] = [
  { key: 'points', label: 'Punten' },
  { key: 'hits',   label: 'Aantal nummers' },
]

export function TopArtistsPage() {
  const [sort, setSort] = useState<SortKey>('points')
  const [year, setYear] = useState<number | null>(null)

  const { data: years } = useQuery({
    queryKey: ['years'],
    queryFn: api.getYears,
  })

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
  } = useInfiniteQuery({
    queryKey: ['topArtists', sort, year],
    queryFn: ({ pageParam }) => api.getTopArtists(sort, year, pageParam as number),
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) =>
      lastPage.hasMore ? allPages.length : undefined,
  })

  const artists = data?.pages.flatMap(p => p.artists) ?? []
  const total = data?.pages[0]?.total ?? 0

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-4">Beste artiesten</h1>

      {/* Controls */}
      <div className="flex flex-wrap gap-3 mb-5 items-center">
        <div className="flex rounded-xl overflow-hidden border border-gray-200 bg-white shadow-sm">
          {SORT_OPTIONS.map(option => (
            <button
              key={option.key}
              onClick={() => setSort(option.key)}
              className={`px-3 py-2 text-sm font-medium transition-colors ${
                sort === option.key
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-600 hover:bg-blue-50 hover:text-blue-700'
              }`}
            >
              {option.label}
            </button>
          ))}
        </div>

        <select
          value={year ?? ''}
          onChange={e => setYear(e.target.value === '' ? null : Number(e.target.value))}
          className="px-3 py-2 rounded-xl border border-gray-200 bg-white text-sm text-gray-700 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">All-time</option>
          {years?.map(y => (
            <option key={y} value={y}>{y}</option>
          ))}
        </select>
      </div>

      {!isLoading && (
        <p className="text-sm text-gray-400 mb-3">{artists.length} van {total} artiesten</p>
      )}

      {isLoading && (
        <div className="flex items-center justify-center py-16">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
        </div>
      )}

      <div className="space-y-2">
        {artists.map((entry, index) => (
          <TopArtistEntryRow
            key={entry.artist.artistId}
            entry={entry}
            rank={index + 1}
            sort={sort}
          />
        ))}
      </div>

      {hasNextPage && (
        <div className="mt-6 text-center">
          <button
            onClick={() => fetchNextPage()}
            disabled={isFetchingNextPage}
            className="px-6 py-3 rounded-xl bg-blue-600 text-white font-semibold hover:bg-blue-700 disabled:opacity-60 transition-colors shadow-sm"
          >
            {isFetchingNextPage ? 'Laden…' : 'Laad meer'}
          </button>
        </div>
      )}
    </div>
  )
}
