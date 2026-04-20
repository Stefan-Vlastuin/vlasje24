import { useNavigate } from 'react-router-dom'
import { Play, Pause, TrendingUp, TrendingDown, Minus } from 'lucide-react'
import type { ChartEntryDto } from '../../types/api'
import { ArtistLinks } from '../shared/ArtistLinks'
import { useAudioPlayer } from '../../hooks/useAudioPlayer'

interface Props {
  entry: ChartEntryDto
}

export function ChartEntryRow({ entry }: Props) {
  const navigate = useNavigate()
  const { playingId, toggle } = useAudioPlayer()
  const isPlaying = playingId === entry.song.songId

  function renderTrend() {
    if (entry.positionChange === null) {
      return (
        <span className="text-xs font-bold text-white bg-blue-600 rounded px-1.5 py-0.5">
          NEW
        </span>
      )
    }
    if (entry.positionChange > 0) {
      return (
        <span className="flex items-center gap-0.5 text-green-600 text-sm font-semibold">
          <TrendingUp className="w-4 h-4" />
          {entry.positionChange}
        </span>
      )
    }
    if (entry.positionChange < 0) {
      return (
        <span className="flex items-center gap-0.5 text-red-500 text-sm font-semibold">
          <TrendingDown className="w-4 h-4" />
          {Math.abs(entry.positionChange)}
        </span>
      )
    }
    return <Minus className="w-4 h-4 text-gray-400" />
  }

  return (
    <div
      className="flex items-center gap-3 sm:gap-4 p-3 rounded-xl bg-white shadow-sm hover:shadow-md hover:bg-blue-50 cursor-pointer transition-all"
      onClick={() => navigate(`/song/${entry.song.songId}`)}
    >
      {/* Position */}
      <div className="w-7 sm:w-9 text-center text-base sm:text-lg font-bold text-gray-400 shrink-0">
        {entry.position}
      </div>

      {/* Image */}
      <div className="w-14 h-14 sm:w-16 sm:h-16 shrink-0 rounded-lg overflow-hidden bg-gray-100 shadow-sm">
        {entry.song.imageUrl ? (
          <img src={entry.song.imageUrl} alt={entry.song.title} className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400 text-lg">♪</div>
        )}
      </div>

      {/* Title & Artists */}
      <div className="flex-1 min-w-0">
        <div className="font-semibold text-gray-900 truncate text-sm sm:text-base">{entry.song.title}</div>
        <ArtistLinks artists={entry.song.artists} className="text-sm text-gray-500" />
      </div>

      {/* Trend */}
      <div className="w-12 sm:w-14 flex justify-center shrink-0">
        {renderTrend()}
      </div>

      {/* Weeks in chart — hidden on small screens */}
      <div className="hidden sm:block w-10 text-center text-sm text-gray-400 shrink-0" title="Weken in lijst">
        {entry.weeksInChart}w
      </div>

      {/* Play button */}
      <button
        className={`p-2 sm:p-2.5 rounded-full transition-colors shrink-0 ${
          isPlaying
            ? 'bg-blue-600 text-white hover:bg-blue-700 shadow-md'
            : 'bg-gray-100 text-gray-600 hover:bg-blue-100 hover:text-blue-700'
        }`}
        onClick={e => {
          e.stopPropagation()
          if (entry.song.previewUrl) {
            toggle(entry.song.songId, entry.song.previewUrl)
          }
        }}
        disabled={!entry.song.previewUrl}
        title={isPlaying ? 'Pauzeren' : 'Afspelen'}
      >
        {isPlaying ? <Pause className="w-4 h-4 sm:w-5 sm:h-5" /> : <Play className="w-4 h-4 sm:w-5 sm:h-5" />}
      </button>
    </div>
  )
}
