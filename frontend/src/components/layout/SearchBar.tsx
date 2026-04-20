import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Search, Music, User, X } from 'lucide-react'
import { api } from '../../api/client'

export function SearchBar() {
  const [isOpen, setIsOpen] = useState(false)
  const [query, setQuery] = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')
  const inputRef = useRef<HTMLInputElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()

  useEffect(() => {
    if (isOpen) inputRef.current?.focus()
  }, [isOpen])

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') close()
    }
    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [])

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        close()
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(query), 300)
    return () => clearTimeout(timer)
  }, [query])

  function close() {
    setIsOpen(false)
    setQuery('')
    setDebouncedQuery('')
  }

  function handleSelect(path: string) {
    navigate(path)
    close()
  }

  const { data } = useQuery({
    queryKey: ['search', debouncedQuery],
    queryFn: () => api.search(debouncedQuery),
    enabled: debouncedQuery.trim().length >= 2,
  })

  const hasResults = data && (data.songs.length > 0 || data.artists.length > 0)
  const showDropdown = debouncedQuery.trim().length >= 2

  return (
    <div ref={containerRef} className="relative flex items-center gap-2">
      {isOpen && (
        <input
          ref={inputRef}
          type="text"
          value={query}
          onChange={e => setQuery(e.target.value)}
          placeholder="Zoek…"
          className="w-44 sm:w-64 px-3 py-1.5 rounded-full bg-white/20 text-white placeholder-blue-200 border border-white/30 focus:outline-none focus:bg-white focus:text-gray-900 focus:placeholder-gray-400 transition-all text-sm"
        />
      )}

      <button
        onClick={() => isOpen ? close() : setIsOpen(true)}
        className="p-2 rounded-full text-white/80 hover:text-white hover:bg-white/20 transition-colors"
        aria-label={isOpen ? 'Sluiten' : 'Zoeken'}
      >
        {isOpen ? <X className="w-5 h-5" /> : <Search className="w-5 h-5" />}
      </button>

      {isOpen && showDropdown && (
        <div className="absolute top-full right-0 mt-2 w-80 bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-100 z-50">
          {!hasResults ? (
            <div className="px-4 py-4 text-sm text-gray-400 text-center">Geen resultaten gevonden</div>
          ) : (
            <>
              {data!.songs.length > 0 && (
                <>
                  <div className="px-4 py-2 text-xs font-semibold text-gray-400 uppercase tracking-wide bg-gray-50 border-b border-gray-100">
                    Nummers
                  </div>
                  {data!.songs.map(song => (
                    <button
                      key={song.songId}
                      className="w-full flex items-center gap-3 px-4 py-3 hover:bg-blue-50 transition-colors text-left border-b border-gray-50 last:border-0"
                      onClick={() => handleSelect(`/song/${song.songId}`)}
                    >
                      <div className="w-10 h-10 shrink-0 rounded-lg overflow-hidden bg-gray-100">
                        {song.imageUrl
                          ? <img src={song.imageUrl} alt={song.title} className="w-full h-full object-cover" />
                          : <div className="w-full h-full flex items-center justify-center"><Music className="w-4 h-4 text-gray-400" /></div>
                        }
                      </div>
                      <div className="min-w-0">
                        <div className="text-sm font-semibold text-gray-900 truncate">{song.title}</div>
                        <div className="text-xs text-gray-500 truncate">
                          {song.artists.map(a => a.name).join(', ')}
                        </div>
                      </div>
                    </button>
                  ))}
                </>
              )}

              {data!.artists.length > 0 && (
                <>
                  <div className="px-4 py-2 text-xs font-semibold text-gray-400 uppercase tracking-wide bg-gray-50 border-b border-gray-100">
                    Artiesten
                  </div>
                  {data!.artists.map(artist => (
                    <button
                      key={artist.artistId}
                      className="w-full flex items-center gap-3 px-4 py-3 hover:bg-blue-50 transition-colors text-left border-b border-gray-50 last:border-0"
                      onClick={() => handleSelect(`/artist/${artist.artistId}`)}
                    >
                      <div className="w-10 h-10 shrink-0 rounded-full bg-blue-100 flex items-center justify-center">
                        <User className="w-4 h-4 text-blue-600" />
                      </div>
                      <div className="text-sm font-semibold text-gray-900">{artist.name}</div>
                    </button>
                  ))}
                </>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}
