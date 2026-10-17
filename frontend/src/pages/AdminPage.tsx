import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { CreateArtistForm } from '../components/admin/CreateArtistForm'
import { CreateSongForm } from '../components/admin/CreateSongForm'
import { CreateChartForm } from '../components/admin/CreateChartForm'

type Tab = 'artist' | 'song' | 'chart'

export function AdminPage() {
  const [tab, setTab] = useState<Tab>('artist')
  const { account, isLoading, logout } = useAuth()
  const navigate = useNavigate()

  if (isLoading) return <div className="min-h-screen bg-gray-50" />
  if (!account) return <Navigate to="/admin/login" replace />
  if (account.role !== 'ADMIN') return <Navigate to="/account" replace />

  async function handleLogout() {
    try {
      await logout()
    } catch {
      // AuthProvider removes the local account state even if the request fails.
    } finally {
      navigate('/admin/login', { replace: true })
    }
  }

  const tabs: { key: Tab; label: string }[] = [
    { key: 'artist', label: 'Artiest toevoegen' },
    { key: 'song', label: 'Nummer toevoegen' },
    { key: 'chart', label: 'Chart toevoegen' },
  ]

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-blue-600 text-white px-6 py-4 flex items-center justify-between gap-4">
        <Link to="/chart/latest" className="text-xl font-bold hover:text-blue-100">Vlasje24 Beheer</Link>
        <div className="flex items-center gap-4 text-sm">
          <Link to="/account" className="text-blue-100 hover:text-white">{account.username}</Link>
          <button onClick={handleLogout} className="text-blue-100 hover:text-white">Uitloggen</button>
        </div>
      </div>

      <div className="max-w-2xl mx-auto px-4 py-8">
        <div className="flex gap-2 mb-6">
          {tabs.map(t => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                tab === t.key
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-700 border border-gray-200 hover:bg-gray-50'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          {tab === 'artist' && <CreateArtistForm />}
          {tab === 'song' && <CreateSongForm />}
          {tab === 'chart' && <CreateChartForm />}
        </div>
      </div>
    </div>
  )
}
