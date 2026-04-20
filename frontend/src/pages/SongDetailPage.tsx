import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Play, Pause, ArrowLeft } from 'lucide-react'
import { api } from '../api/client'
import { ArtistLinks } from '../components/shared/ArtistLinks'
import { PositionChart } from '../components/song/PositionChart'
import { useAudioPlayer } from '../hooks/useAudioPlayer'

export function SongDetailPage() {
  const { songId } = useParams<{ songId: string }>()
  const { playingId, toggle } = useAudioPlayer()

  const { data, isLoading, error } = useQuery({
    queryKey: ['song', Number(songId)],
    queryFn: () => api.getSong(Number(songId)),
    enabled: !!songId,
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
      </div>
    )
  }

  if (error || !data) {
    return <div className="text-center py-16 text-red-500">Nummer niet gevonden.</div>
  }

  const isPlaying = playingId === data.songId

  return (
    <div>
      <Link to="/chart/latest" className="inline-flex items-center gap-1 text-blue-600 hover:text-blue-800 font-medium transition-colors mb-6">
        <ArrowLeft className="w-4 h-4" />
        Terug naar chart
      </Link>

      <div className="bg-white rounded-2xl shadow-sm p-5 mb-5 flex gap-5">
        {/* Album art */}
        <div className="w-28 h-28 sm:w-36 sm:h-36 shrink-0 rounded-xl overflow-hidden bg-gray-100 shadow-sm">
          {data.imageUrl ? (
            <img src={data.imageUrl} alt={data.title} className="w-full h-full object-cover" />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-gray-400 text-4xl">♪</div>
          )}
        </div>

        <div className="flex flex-col justify-between min-w-0">
          <div>
            <h1 className="text-xl sm:text-2xl font-bold text-gray-900 mb-1 leading-tight">{data.title}</h1>
            <ArtistLinks artists={data.artists} className="text-base text-gray-600" />
          </div>

          {data.previewUrl && (
            <button
              className={`mt-3 flex items-center gap-2 px-4 py-2 rounded-full font-semibold transition-colors w-fit text-sm sm:text-base ${
                isPlaying
                  ? 'bg-blue-600 text-white hover:bg-blue-700 shadow-md'
                  : 'bg-gray-100 text-gray-700 hover:bg-blue-50 hover:text-blue-700'
              }`}
              onClick={() => toggle(data.songId, data.previewUrl!)}
            >
              {isPlaying ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
              {isPlaying ? 'Pauzeren' : 'Afspelen'}
            </button>
          )}
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-3 mb-5">
        <div className="bg-white rounded-xl shadow-sm p-4 text-center">
          <div className="text-2xl sm:text-3xl font-bold text-blue-600">{data.weeksInChart}</div>
          <div className="text-xs sm:text-sm text-gray-500 mt-1">Weken in lijst</div>
        </div>
        <div className="bg-white rounded-xl shadow-sm p-4 text-center">
          <div className="text-2xl sm:text-3xl font-bold text-blue-600">{data.highestPosition}</div>
          <div className="text-xs sm:text-sm text-gray-500 mt-1">Hoogste positie</div>
        </div>
        <div className="bg-white rounded-xl shadow-sm p-4 text-center">
          <div className="text-2xl sm:text-3xl font-bold text-blue-600">{data.totalPoints}</div>
          <div className="text-xs sm:text-sm text-gray-500 mt-1">Totaal punten</div>
        </div>
      </div>

      {data.chartHistory.length > 1 && (
        <div className="bg-white rounded-2xl shadow-sm p-5">
          <PositionChart history={data.chartHistory} />
        </div>
      )}
    </div>
  )
}
