import { useParams, useNavigate, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft } from 'lucide-react'
import { api } from '../api/client'
import { ArtistLinks } from '../components/shared/ArtistLinks'

export function ArtistPage() {
  const { artistId } = useParams<{ artistId: string }>()
  const navigate = useNavigate()

  const { data, isLoading, error } = useQuery({
    queryKey: ['artist', Number(artistId)],
    queryFn: () => api.getArtist(Number(artistId)),
    enabled: !!artistId,
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
      </div>
    )
  }

  if (error || !data) {
    return <div className="text-center py-16 text-red-500">Artiest niet gevonden.</div>
  }

  return (
    <div>
      <Link to="/chart/latest" className="inline-flex items-center gap-1 text-blue-600 hover:text-blue-800 font-medium transition-colors mb-6">
        <ArrowLeft className="w-4 h-4" />
        Terug naar chart
      </Link>

      <div className="bg-white rounded-2xl shadow-sm p-5 mb-5">
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-4">{data.name}</h1>
        <div className="flex gap-4">
          <div className="bg-blue-50 rounded-xl px-5 py-3 text-center">
            <div className="text-2xl font-bold text-blue-600">{data.hitCount}</div>
            <div className="text-sm text-gray-500">Hits</div>
          </div>
          <div className="bg-blue-50 rounded-xl px-5 py-3 text-center">
            <div className="text-2xl font-bold text-blue-600">{data.totalPoints}</div>
            <div className="text-sm text-gray-500">Totaal punten</div>
          </div>
        </div>
      </div>

      <h2 className="text-base font-semibold text-gray-600 mb-3">Nummers</h2>
      <div className="space-y-2">
        {data.songs.map(song => (
          <div
            key={song.songId}
            className="flex items-center gap-4 p-3 rounded-xl bg-white shadow-sm hover:shadow-md hover:bg-blue-50 cursor-pointer transition-all"
            onClick={() => navigate(`/song/${song.songId}`)}
          >
            <div className="w-14 h-14 shrink-0 rounded-lg overflow-hidden bg-gray-100">
              {song.imageUrl ? (
                <img src={song.imageUrl} alt={song.title} className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-gray-400 text-lg">♪</div>
              )}
            </div>
            <div className="flex-1 min-w-0">
              <div className="font-semibold text-gray-900 truncate">{song.title}</div>
              <ArtistLinks artists={song.artists} className="text-sm text-gray-500" />
            </div>
            <div className="text-right shrink-0">
              <div className="text-sm text-gray-500">{song.weeksInChart} weken</div>
              <div className="text-sm font-semibold text-blue-600">{song.totalPoints} punten</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
