import { useNavigate } from 'react-router-dom'
import { User } from 'lucide-react'
import type { TopArtistEntryDto } from '../../types/api'

type SortKey = 'points' | 'hits'

interface Props {
  entry: TopArtistEntryDto
  rank: number
  sort: SortKey
}

export function TopArtistEntryRow({ entry, rank, sort }: Props) {
  const navigate = useNavigate()

  const stats: { key: SortKey; label: string; value: number }[] = [
    { key: 'points', label: 'punten', value: entry.points },
    { key: 'hits',   label: 'hits',   value: entry.hitCount },
  ]

  return (
    <div
      className="flex items-center gap-3 sm:gap-4 p-3 rounded-xl bg-white shadow-sm hover:shadow-md hover:bg-blue-50 cursor-pointer transition-all"
      onClick={() => navigate(`/artist/${entry.artist.artistId}`)}
    >
      {/* Rank */}
      <div className="w-7 sm:w-9 text-center text-base sm:text-lg font-bold text-gray-400 shrink-0">
        {rank}
      </div>

      {/* Avatar */}
      <div className="w-14 h-14 sm:w-16 sm:h-16 shrink-0 rounded-full bg-blue-100 flex items-center justify-center shadow-sm">
        <User className="w-7 h-7 text-blue-500" />
      </div>

      {/* Name */}
      <div className="flex-1 min-w-0">
        <div className="font-semibold text-gray-900 truncate text-sm sm:text-base">
          {entry.artist.name}
        </div>
      </div>

      {/* Stats — desktop */}
      <div className="hidden sm:flex gap-6 shrink-0">
        {stats.map(({ key, label, value }) => (
          <div key={key} className="text-center">
            <div className={`text-sm font-bold ${sort === key ? 'text-blue-600' : 'text-gray-700'}`}>
              {value}
            </div>
            <div className={`text-xs ${sort === key ? 'text-blue-400' : 'text-gray-400'}`}>
              {label}
            </div>
          </div>
        ))}
      </div>

      {/* Active stat — mobile */}
      <div className="sm:hidden text-center shrink-0">
        <div className="text-sm font-bold text-blue-600">
          {stats.find(s => s.key === sort)?.value}
        </div>
        <div className="text-xs text-blue-400">
          {stats.find(s => s.key === sort)?.label}
        </div>
      </div>
    </div>
  )
}
