import { useState } from 'react'
import { api } from '../../api/client'
import { useAuth } from '../../hooks/useAuth'

export function CreateArtistForm() {
  const [name, setName] = useState('')
  const [status, setStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null)
  const [loading, setLoading] = useState(false)
  const { getToken } = useAuth()

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setStatus(null)
    setLoading(true)
    try {
      const res = await api.createArtist(name, getToken()!)
      setStatus({ type: 'success', message: `Artiest aangemaakt met ID ${res.id}` })
      setName('')
    } catch (err) {
      setStatus({ type: 'error', message: err instanceof Error ? err.message : 'Er ging iets mis' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Artiest toevoegen</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Naam</label>
          <input
            type="text"
            value={name}
            onChange={e => setName(e.target.value)}
            required
            placeholder="Naam van de artiest"
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        {status && (
          <p className={`text-sm ${status.type === 'success' ? 'text-green-600' : 'text-red-600'}`}>
            {status.message}
          </p>
        )}
        <button
          type="submit"
          disabled={loading}
          className="bg-blue-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          {loading ? 'Bezig...' : 'Artiest toevoegen'}
        </button>
      </form>
    </div>
  )
}
