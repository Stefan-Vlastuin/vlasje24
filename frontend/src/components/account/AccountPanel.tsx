import type { ReactNode } from 'react'

interface Props {
  title: string
  description?: string
  children: ReactNode
}

export function AccountPanel({ title, description, children }: Props) {
  return (
    <section className="max-w-md mx-auto bg-white rounded-2xl border border-gray-200 shadow-sm p-6 sm:p-8">
      <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
      {description && <p className="mt-2 text-sm leading-6 text-gray-600">{description}</p>}
      <div className="mt-6">{children}</div>
    </section>
  )
}

export const inputClass = 'mt-1 w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200'
export const primaryButtonClass = 'w-full rounded-lg bg-blue-600 px-4 py-2.5 font-medium text-white transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50'

export function FormError({ children }: { children: ReactNode }) {
  return <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{children}</p>
}

export function FormSuccess({ children }: { children: ReactNode }) {
  return <p role="status" className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-800">{children}</p>
}
