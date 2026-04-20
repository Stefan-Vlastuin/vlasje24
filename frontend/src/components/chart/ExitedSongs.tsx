import { useNavigate } from 'react-router-dom'
import type { SongDto } from '../../types/api'
import { ArtistLinks } from '../shared/ArtistLinks'

interface Props {
  songs: SongDto[]
}

export function ExitedSongs({ songs }: Props) {
  const navigate = useNavigate()

  if (songs.length === 0) return null

  return (
    <div className="mt-8">
      <h2 className="text-base font-semibold text-gray-500 mb-3 border-t border-gray-200 pt-6">
        Uit de lijst gegaan
      </h2>
      <div className="space-y-2">
        {songs.map(song => (
          <div
            key={song.songId}
            className="flex items-center gap-3 p-2 rounded-lg bg-white hover:bg-gray-50 cursor-pointer transition-colors opacity-60 hover:opacity-100"
            onClick={() => navigate(`/song/${song.songId}`)}
          >
            <div className="w-10 h-10 shrink-0 rounded overflow-hidden bg-gray-100">
              {song.imageUrl ? (
                <img src={song.imageUrl} alt={song.title} className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-gray-400 text-xs">♪</div>
              )}
            </div>
            <div className="min-w-0">
              <div className="text-sm font-medium text-gray-800 truncate">{song.title}</div>
              <ArtistLinks artists={song.artists} className="text-xs text-gray-500" />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
