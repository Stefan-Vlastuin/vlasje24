import { useState, type ReactNode } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { LogOut, Shield, UserRound } from 'lucide-react'
import { useAuth } from '../../auth/AuthContext'
import { SearchBar } from './SearchBar'

interface Props {
  children: ReactNode
}

export function AppShell({ children }: Props) {
  const { account, isLoading, logout } = useAuth()
  const [logoutPending, setLogoutPending] = useState(false)
  const navigate = useNavigate()
  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    `px-2 sm:px-3 py-1 rounded-full text-xs sm:text-sm font-medium transition-colors ${
      isActive
        ? 'bg-white/20 text-white'
        : 'text-blue-100 hover:text-white hover:bg-white/10'
    }`

  async function handleLogout() {
    setLogoutPending(true)
    try {
      await logout()
    } catch {
      // AuthProvider removes the local account state even if the request fails.
    } finally {
      setLogoutPending(false)
      navigate('/chart/latest')
    }
  }

  return (
    <div className="flex min-h-screen flex-col bg-gray-50 text-gray-900">
      <header className="bg-blue-700 shadow-md sticky top-0 z-40">
        <div className="max-w-3xl mx-auto px-4">
          <div className="pt-3 flex items-center gap-3">
            <Link to="/chart/latest" className="text-2xl font-bold text-white hover:text-blue-100 transition-colors shrink-0">
              Vlasje24
            </Link>
            <div className="ml-auto flex min-w-0 items-center gap-1.5 text-sm">
              {!isLoading && !account && (
                <>
                  <Link to="/login" className="rounded-full px-2.5 py-1.5 font-medium text-blue-100 hover:bg-white/10 hover:text-white">Inloggen</Link>
                  <Link to="/register" className="rounded-full bg-white px-2.5 py-1.5 font-medium text-blue-700 hover:bg-blue-50">Registreren</Link>
                </>
              )}
              {account && (
                <>
                  {account.role === 'ADMIN' && (
                    <Link to="/admin" title="Beheer" className="rounded-full p-2 text-blue-100 hover:bg-white/10 hover:text-white">
                      <Shield className="h-4 w-4" aria-hidden="true" />
                      <span className="sr-only">Beheer</span>
                    </Link>
                  )}
                  <Link to="/account" className="flex max-w-36 items-center gap-1.5 rounded-full px-2.5 py-1.5 font-medium text-blue-100 hover:bg-white/10 hover:text-white">
                    <UserRound className="h-4 w-4 shrink-0" aria-hidden="true" />
                    <span className="truncate">{account.username}</span>
                  </Link>
                  <button type="button" onClick={handleLogout} disabled={logoutPending} title="Uitloggen" className="rounded-full p-2 text-blue-100 hover:bg-white/10 hover:text-white disabled:opacity-50">
                    <LogOut className="h-4 w-4" aria-hidden="true" />
                    <span className="sr-only">Uitloggen</span>
                  </button>
                </>
              )}
            </div>
          </div>
          <div className="py-2 flex items-center gap-2 sm:gap-4">
            <nav className="flex gap-1 flex-1 min-w-0" aria-label="Hoofdnavigatie">
              <NavLink to="/chart/latest" className={navLinkClass}>Home</NavLink>
              <NavLink to="/songs" className={navLinkClass}>Nummers</NavLink>
              <NavLink to="/artists" className={navLinkClass}>Artiesten</NavLink>
            </nav>
            <div className="shrink-0">
              <SearchBar />
            </div>
          </div>
        </div>
      </header>
      <main className="mx-auto w-full max-w-3xl flex-1 px-4 py-6">
        {children}
      </main>
      <footer className="border-t border-gray-200 bg-white">
        <nav className="mx-auto flex max-w-3xl gap-4 px-4 py-4 text-sm text-gray-600" aria-label="Juridische informatie">
          <Link to="/privacy" className="hover:text-blue-700 hover:underline">Privacy</Link>
          <Link to="/huisregels" className="hover:text-blue-700 hover:underline">Huisregels</Link>
        </nav>
      </footer>
    </div>
  )
}
