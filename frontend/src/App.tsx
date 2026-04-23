import { Routes, Route, Navigate } from 'react-router-dom'
import { AppShell } from './components/layout/AppShell'
import { HomePage } from './pages/HomePage'
import { SongDetailPage } from './pages/SongDetailPage'
import { ArtistPage } from './pages/ArtistPage'
import { TopSongsPage } from './pages/TopSongsPage'
import { TopArtistsPage } from './pages/TopArtistsPage'
import { LoginPage } from './pages/LoginPage'
import { AdminPage } from './pages/AdminPage'
import { AudioPlayerContext, useAudioPlayerState } from './hooks/useAudioPlayer'

export default function App() {
  const audioPlayer = useAudioPlayerState()

  return (
    <AudioPlayerContext.Provider value={audioPlayer}>
      <Routes>
        <Route
          path="/*"
          element={
            <AppShell>
              <Routes>
                <Route path="/" element={<Navigate to="/chart/latest" replace />} />
                <Route path="/chart/latest" element={<HomePage />} />
                <Route path="/chart/:weekId" element={<HomePage />} />
                <Route path="/song/:songId" element={<SongDetailPage />} />
                <Route path="/artist/:artistId" element={<ArtistPage />} />
                <Route path="/songs" element={<TopSongsPage />} />
                <Route path="/artists" element={<TopArtistsPage />} />
              </Routes>
            </AppShell>
          }
        />
        <Route path="/admin/login" element={<LoginPage />} />
        <Route path="/admin" element={<AdminPage />} />
      </Routes>
    </AudioPlayerContext.Provider>
  )
}
