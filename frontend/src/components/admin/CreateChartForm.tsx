import { useState, useEffect, useRef } from 'react'
import { api } from '../../api/client'
import { useAuth } from '../../hooks/useAuth'
import type { SongDto } from '../../types/api'

const CHART_SIZE = 24

export function CreateChartForm() {
  const [date, setDate] = useState('')
  const [slots, setSlots] = useState<(SongDto | null)[]>(Array(CHART_SIZE).fill(null))
  const [searchIndex, setSearchIndex] = useState<number | null>(null)
  const [searchQuery, setSearchQuery] = useState('')
  const [searchResults, setSearchResults] = useState<SongDto[]>([])
  const [status, setStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null)
  const [loading, setLoading] = useState(false)
  const searchTimeout = useRef<ReturnType<typeof setTimeout> | null>(null)
  const { getToken } = useAuth()

  useEffect(() => {
    if (searchQuery.length < 2) {
      setSearchResults([])
      return
    }
    if (searchTimeout.current) clearTimeout(searchTimeout.current)
    searchTimeout.current = setTimeout(async () => {
      const res = await api.search(searchQuery)
      setSearchResults(res.songs)
    }, 300)
  }, [searchQuery])

  function selectSong(song: SongDto) {
    if (searchIndex === null) return
    setSlots(prev => {
      const next = [...prev]
      next[searchIndex] = song
      return next
    })
    setSearchIndex(null)
    setSearchQuery('')
    setSearchResults([])
  }

  function clearSlot(index: number) {
    setSlots(prev => {
      const next = [...prev]
      next[index] = null
      return next
    })
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setStatus(null)
    const filledCount = slots.filter(Boolean).length
    if (filledCount !== CHART_SIZE) {
      setStatus({ type: 'error', message: `Vul alle ${CHART_SIZE} posities in (${filledCount}/${CHART_SIZE} ingevuld)` })
      return
    }
    setLoading(true)
    try {
      const res = await api.createChart(
        { date, songIds: (slots as SongDto[]).map(s => s.songId) },
        getToken()!,
      )
      setStatus({ type: 'success', message: `Chart aangemaakt met ID ${res.id}` })
      setDate('')
      setSlots(Array(CHART_SIZE).fill(null))
    } catch (err) {
      setStatus({ type: 'error', message: err instanceof Error ? err.message : 'Er ging iets mis' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Chart toevoegen</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Datum</label>
          <input
            type="date"
            value={date}
            onChange={e => setDate(e.target.value)}
            required
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Nummers ({slots.filter(Boolean).length}/{CHART_SIZE})
          </label>
          <div className="space-y-1 max-h-96 overflow-y-auto">
            {slots.map((song, i) => (
              <div key={i} className="flex items-center gap-2">
                <span className="text-xs text-gray-400 font-mono w-5 text-right shrink-0">{i + 1}</span>
                {song ? (
                  <div className="flex items-center gap-2 flex-1 bg-blue-50 rounded-lg px-3 py-1.5">
                    <span className="text-sm flex-1 truncate text-gray-800">
                      {song.artists.map(a => a.name).join(' & ')} – {song.title}
                    </span>
                    <button
                      type="button"
                      onClick={() => clearSlot(i)}
                      className="text-red-400 hover:text-red-600 text-xs shrink-0"
                    >
                      ✕
                    </button>
                  </div>
                ) : (
                  <button
                    type="button"
                    onClick={() => { setSearchIndex(i); setSearchQuery('') }}
                    className={`flex-1 text-left border rounded-lg px-3 py-1.5 text-sm transition-colors ${
                      searchIndex === i
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-dashed border-gray-300 text-gray-400 hover:border-blue-400 hover:text-blue-500'
                    }`}
                  >
                    {searchIndex === i ? 'Bezig met zoeken...' : 'Klik om nummer te zoeken'}
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>

        {searchIndex !== null && (
          <div className="border border-blue-200 rounded-lg p-3 bg-blue-50">
            <p className="text-xs text-blue-600 font-medium mb-2">Positie {searchIndex + 1} — zoek nummer</p>
            <div className="relative">
              <input
                autoFocus
                type="text"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                placeholder="Zoek op titel..."
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
              />
              {searchResults.length > 0 && (
                <div className="absolute z-10 left-0 right-0 bg-white border border-gray-200 rounded-lg shadow-lg mt-1 max-h-40 overflow-y-auto">
                  {searchResults.map(s => (
                    <button
                      key={s.songId}
                      type="button"
                      onClick={() => selectSong(s)}
                      className="w-full text-left px-3 py-2 text-sm hover:bg-gray-50"
                    >
                      {s.artists.map(a => a.name).join(' & ')} – {s.title}
                    </button>
                  ))}
                </div>
              )}
            </div>
            <button
              type="button"
              onClick={() => { setSearchIndex(null); setSearchQuery(''); setSearchResults([]) }}
              className="mt-2 text-xs text-gray-500 hover:text-gray-700"
            >
              Annuleren
            </button>
          </div>
        )}

        {status && (
          <p className={`text-sm ${status.type === 'success' ? 'text-green-600' : 'text-red-600'}`}>
            {status.message}
          </p>
        )}
        <button
          type="submit"
          disabled={loading}
          className="bg-blue-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          {loading ? 'Bezig...' : 'Chart opslaan'}
        </button>
      </form>
    </div>
  )
}
