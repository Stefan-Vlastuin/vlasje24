import type { ReactNode } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { SearchBar } from './SearchBar'

interface Props {
  children: ReactNode
}

export function AppShell({ children }: Props) {
  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    `px-2 sm:px-3 py-1 rounded-full text-xs sm:text-sm font-medium transition-colors ${
      isActive
        ? 'bg-white/20 text-white'
        : 'text-blue-100 hover:text-white hover:bg-white/10'
    }`

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      <header className="bg-blue-700 shadow-md sticky top-0 z-40">
        <div className="max-w-3xl mx-auto px-4">
          <div className="py-3 flex items-center gap-2 sm:gap-4">
            <Link to="/chart/latest" className="text-2xl font-bold text-white hover:text-blue-100 transition-colors shrink-0">
              Vlasje24
            </Link>
            <nav className="flex gap-1 flex-1 min-w-0">
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
      <main className="max-w-3xl mx-auto px-4 py-6">
        {children}
      </main>
    </div>
  )
}
