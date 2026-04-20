import { useNavigate } from 'react-router-dom'
import { Play, Pause } from 'lucide-react'
import type { TopSongEntryDto } from '../../types/api'
import { ArtistLinks } from '../shared/ArtistLinks'
import { useAudioPlayer } from '../../hooks/useAudioPlayer'

type SortKey = 'points' | 'weeks' | 'position'

interface Props {
  entry: TopSongEntryDto
  rank: number
  sort: SortKey
}

export function TopSongEntryRow({ entry, rank, sort }: Props) {
  const navigate = useNavigate()
  const { playingId, toggle } = useAudioPlayer()
  const isPlaying = playingId === entry.song.songId

  const stats: { key: SortKey; label: string; value: number }[] = [
    { key: 'points',   label: 'pnt',  value: entry.points },
    { key: 'weeks',    label: 'wkn',  value: entry.weeksInChart },
    { key: 'position', label: 'pos',  value: entry.highestPosition },
  ]

  return (
    <div
      className="flex items-center gap-3 sm:gap-4 p-3 rounded-xl bg-white shadow-sm hover:shadow-md hover:bg-blue-50 cursor-pointer transition-all"
      onClick={() => navigate(`/song/${entry.song.songId}`)}
    >
      {/* Rank */}
      <div className="w-7 sm:w-9 text-center text-base sm:text-lg font-bold text-gray-400 shrink-0">
        {rank}
      </div>

      {/* Image */}
      <div className="w-14 h-14 sm:w-16 sm:h-16 shrink-0 rounded-lg overflow-hidden bg-gray-100 shadow-sm">
        {entry.song.imageUrl
          ? <img src={entry.song.imageUrl} alt={entry.song.title} className="w-full h-full object-cover" />
          : <div className="w-full h-full flex items-center justify-center text-gray-400 text-lg">♪</div>
        }
      </div>

      {/* Title & Artists */}
      <div className="flex-1 min-w-0">
        <div className="font-semibold text-gray-900 truncate text-sm sm:text-base">{entry.song.title}</div>
        <ArtistLinks artists={entry.song.artists} className="text-sm text-gray-500" />
      </div>

      {/* Stats */}
      <div className="hidden sm:flex gap-3 shrink-0">
        {stats.map(({ key, label, value }) => (
          <div key={key} className="text-center w-12">
            <div className={`text-sm font-bold ${sort === key ? 'text-blue-600' : 'text-gray-700'}`}>
              {value}
            </div>
            <div className={`text-xs ${sort === key ? 'text-blue-400' : 'text-gray-400'}`}>
              {label}
            </div>
          </div>
        ))}
      </div>

      {/* Active stat on mobile */}
      <div className="sm:hidden text-center w-10 shrink-0">
        <div className="text-sm font-bold text-blue-600">
          {stats.find(s => s.key === sort)?.value}
        </div>
        <div className="text-xs text-blue-400">
          {stats.find(s => s.key === sort)?.label}
        </div>
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
          if (entry.song.previewUrl) toggle(entry.song.songId, entry.song.previewUrl)
        }}
        disabled={!entry.song.previewUrl}
        title={isPlaying ? 'Pauzeren' : 'Afspelen'}
      >
        {isPlaying
          ? <Pause className="w-4 h-4 sm:w-5 sm:h-5" />
          : <Play className="w-4 h-4 sm:w-5 sm:h-5" />
        }
      </button>
    </div>
  )
}
