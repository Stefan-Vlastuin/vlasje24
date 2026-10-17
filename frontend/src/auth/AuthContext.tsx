import { createContext, useCallback, useContext, type ReactNode } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { api, ApiError } from '../api/client'
import type { AccountDto } from '../types/api'

const AUTH_QUERY_KEY = ['auth', 'me'] as const

interface AuthContextValue {
  account: AccountDto | null
  isLoading: boolean
  error: Error | null
  login: (identifier: string, password: string) => Promise<AccountDto>
  logout: () => Promise<void>
  deleteAccount: (password: string) => Promise<void>
  refresh: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const query = useQuery({
    queryKey: AUTH_QUERY_KEY,
    queryFn: async () => {
      try {
        return await api.getCurrentAccount()
      } catch (error) {
        if (error instanceof ApiError && error.status === 401) return null
        throw error
      }
    },
    retry: false,
  })

  const login = useCallback(async (identifier: string, password: string) => {
    const account = await api.login(identifier, password)
    queryClient.setQueryData(AUTH_QUERY_KEY, account)
    return account
  }, [queryClient])

  const logout = useCallback(async () => {
    try {
      await api.logout()
    } finally {
      queryClient.setQueryData(AUTH_QUERY_KEY, null)
    }
  }, [queryClient])

  const deleteAccount = useCallback(async (password: string) => {
    await api.deleteAccount(password)
    queryClient.setQueryData(AUTH_QUERY_KEY, null)
  }, [queryClient])

  const refresh = useCallback(async () => {
    await queryClient.invalidateQueries({ queryKey: AUTH_QUERY_KEY })
  }, [queryClient])

  return (
    <AuthContext.Provider value={{
      account: query.data ?? null,
      isLoading: query.isPending,
      error: query.error,
      login,
      logout,
      deleteAccount,
      refresh,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth moet binnen AuthProvider worden gebruikt')
  return context
}
