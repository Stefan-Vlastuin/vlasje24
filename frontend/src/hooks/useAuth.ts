const TOKEN_KEY = 'vlasje24_admin_token'

export function useAuth() {
  const getToken = (): string | null => localStorage.getItem(TOKEN_KEY)
  const isAuthenticated = (): boolean => !!getToken()
  const login = (token: string) => localStorage.setItem(TOKEN_KEY, token)
  const logout = () => localStorage.removeItem(TOKEN_KEY)

  return { getToken, isAuthenticated, login, logout }
}
