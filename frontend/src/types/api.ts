export interface ArtistDto {
  artistId: number
  name: string
}

export interface SongDto {
  songId: number
  title: string
  imageUrl: string | null
  previewUrl: string | null
  artists: ArtistDto[]
}

export interface ChartHistoryEntryDto {
  weekId: number
  date: string
  position: number
}

export interface SongDetailDto extends SongDto {
  weeksInChart: number
  highestPosition: number
  totalPoints: number
  chartHistory: ChartHistoryEntryDto[]
}

export interface ChartEntryDto {
  position: number
  positionChange: number | null
  weeksInChart: number
  song: SongDto
}

export interface ChartDto {
  weekId: number
  date: string
  prevWeekId: number | null
  nextWeekId: number | null
  entries: ChartEntryDto[]
  exitedSongs: SongDto[]
}

export interface TopSongEntryDto {
  song: SongDto
  points: number
  weeksInChart: number
  highestPosition: number
}

export interface TopSongsPageDto {
  songs: TopSongEntryDto[]
  hasMore: boolean
  total: number
}

export interface TopArtistEntryDto {
  artist: ArtistDto
  points: number
  hitCount: number
}

export interface TopArtistsPageDto {
  artists: TopArtistEntryDto[]
  hasMore: boolean
  total: number
}

export interface SearchResultDto {
  songs: SongDto[]
  artists: ArtistDto[]
}

export interface ArtistDetailDto {
  artistId: number
  name: string
  hitCount: number
  totalPoints: number
  songs: SongDetailDto[]
}

export interface CreatedDto {
  id: number
}

export interface AccountDto {
  userId: number
  username: string
  role: 'USER' | 'ADMIN'
  emailVerified: boolean
}

export interface CommentDto {
  commentId: number
  authorId: number
  username: string
  body: string
  createdAt: string
  updatedAt: string
  edited: boolean
}

export interface CommentPageDto {
  comments: CommentDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
