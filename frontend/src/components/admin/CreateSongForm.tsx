import { useState, useEffect, useRef } from 'react'
import { api } from '../../api/client'
import { useAuth } from '../../hooks/useAuth'
import type { ArtistDto } from '../../types/api'

export function CreateSongForm() {
  const [title, setTitle] = useState('')
  const [imageUrl, setImageUrl] = useState('')
  const [previewUrl, setPreviewUrl] = useState('')
  const [artistSearch, setArtistSearch] = useState('')
  const [artistResults, setArtistResults] = useState<ArtistDto[]>([])
  const [selectedArtists, setSelectedArtists] = useState<ArtistDto[]>([])
  const [status, setStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null)
  const [loading, setLoading] = useState(false)
  const searchTimeout = useRef<ReturnType<typeof setTimeout> | null>(null)
  const { getToken } = useAuth()

  useEffect(() => {
    if (artistSearch.length < 2) {
      setArtistResults([])
      return
    }
    if (searchTimeout.current) clearTimeout(searchTimeout.current)
    searchTimeout.current = setTimeout(async () => {
      const res = await api.search(artistSearch)
      setArtistResults(res.artists.filter(a => !selectedArtists.find(s => s.artistId === a.artistId)))
    }, 300)
  }, [artistSearch, selectedArtists])

  function addArtist(artist: ArtistDto) {
    setSelectedArtists(prev => [...prev, artist])
    setArtistSearch('')
    setArtistResults([])
  }

  function removeArtist(artistId: number) {
    setSelectedArtists(prev => prev.filter(a => a.artistId !== artistId))
  }

  function moveArtist(index: number, direction: -1 | 1) {
    setSelectedArtists(prev => {
      const next = [...prev]
      const target = index + direction
      if (target < 0 || target >= next.length) return next
      ;[next[index], next[target]] = [next[target], next[index]]
      return next
    })
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setStatus(null)
    if (selectedArtists.length === 0) {
      setStatus({ type: 'error', message: 'Voeg minimaal één artiest toe' })
      return
    }
    setLoading(true)
    try {
      const res = await api.createSong(
        { title, imageUrl, previewUrl, artistIds: selectedArtists.map(a => a.artistId) },
        getToken()!,
      )
      setStatus({ type: 'success', message: `Nummer aangemaakt met ID ${res.id}` })
      setTitle('')
      setImageUrl('')
      setPreviewUrl('')
      setSelectedArtists([])
    } catch (err) {
      setStatus({ type: 'error', message: err instanceof Error ? err.message : 'Er ging iets mis' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Nummer toevoegen</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Titel</label>
          <input
            type="text"
            value={title}
            onChange={e => setTitle(e.target.value)}
            required
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Image URL</label>
          <input
            type="text"
            value={imageUrl}
            onChange={e => setImageUrl(e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Preview URL</label>
          <input
            type="text"
            value={previewUrl}
            onChange={e => setPreviewUrl(e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Artiesten</label>
          <div className="relative">
            <input
              type="text"
              value={artistSearch}
              onChange={e => setArtistSearch(e.target.value)}
              placeholder="Zoek artiest..."
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {artistResults.length > 0 && (
              <div className="absolute z-10 left-0 right-0 bg-white border border-gray-200 rounded-lg shadow-lg mt-1 max-h-40 overflow-y-auto">
                {artistResults.map(a => (
                  <button
                    key={a.artistId}
                    type="button"
                    onClick={() => addArtist(a)}
                    className="w-full text-left px-3 py-2 text-sm hover:bg-gray-50"
                  >
                    {a.name}
                  </button>
                ))}
              </div>
            )}
          </div>
          {selectedArtists.length > 0 && (
            <div className="mt-2 space-y-1">
              {selectedArtists.map((a, i) => (
                <div key={a.artistId} className="flex items-center gap-2 bg-blue-50 rounded-lg px-3 py-1.5">
                  <span className="text-xs text-blue-500 w-4 font-mono">{i + 1}</span>
                  <span className="text-sm flex-1 text-gray-800">{a.name}</span>
                  <button
                    type="button"
                    onClick={() => moveArtist(i, -1)}
                    disabled={i === 0}
                    className="text-gray-400 hover:text-gray-600 disabled:opacity-30 text-xs px-1"
                  >
                    ▲
                  </button>
                  <button
                    type="button"
                    onClick={() => moveArtist(i, 1)}
                    disabled={i === selectedArtists.length - 1}
                    className="text-gray-400 hover:text-gray-600 disabled:opacity-30 text-xs px-1"
                  >
                    ▼
                  </button>
                  <button
                    type="button"
                    onClick={() => removeArtist(a.artistId)}
                    className="text-red-400 hover:text-red-600 text-xs px-1"
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
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
          {loading ? 'Bezig...' : 'Nummer toevoegen'}
        </button>
      </form>
    </div>
  )
}
