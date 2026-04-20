import { Link } from 'react-router-dom'
import type { ArtistDto } from '../../types/api'

interface Props {
  artists: ArtistDto[]
  className?: string
}

export function ArtistLinks({ artists, className = '' }: Props) {
  return (
    <span className={className}>
      {artists.map((artist, index) => (
        <span key={artist.artistId}>
          {index > 0 && ', '}
          <Link
            to={`/artist/${artist.artistId}`}
            className="text-blue-600 hover:text-blue-800 hover:underline transition-colors"
            onClick={e => e.stopPropagation()}
          >
            {artist.name}
          </Link>
        </span>
      ))}
    </span>
  )
}
