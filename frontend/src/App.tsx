import { Routes, Route, Navigate } from 'react-router-dom'
import { AppShell } from './components/layout/AppShell'
import { HomePage } from './pages/HomePage'
import { SongDetailPage } from './pages/SongDetailPage'
import { ArtistPage } from './pages/ArtistPage'
import { TopSongsPage } from './pages/TopSongsPage'
import { TopArtistsPage } from './pages/TopArtistsPage'
import { LoginPage } from './pages/LoginPage'
import { AdminPage } from './pages/AdminPage'
import { AccountLoginPage } from './pages/AccountLoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { VerifyEmailPage } from './pages/VerifyEmailPage'
import { ForgotPasswordPage } from './pages/ForgotPasswordPage'
import { ResetPasswordPage } from './pages/ResetPasswordPage'
import { AccountPage } from './pages/AccountPage'
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
                <Route path="/login" element={<AccountLoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/verify-email" element={<VerifyEmailPage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} />
                <Route path="/account" element={<AccountPage />} />
                <Route path="*" element={<Navigate to="/chart/latest" replace />} />
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
