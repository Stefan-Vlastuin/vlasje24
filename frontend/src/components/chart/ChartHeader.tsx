import { Link } from 'react-router-dom'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { format } from 'date-fns'
import { nl } from 'date-fns/locale'

interface Props {
  date: string
  prevWeekId: number | null
  nextWeekId: number | null
}

export function ChartHeader({ date, prevWeekId, nextWeekId }: Props) {
  const formatted = format(new Date(date), 'd MMMM yyyy', { locale: nl })

  return (
    <div className="flex items-center justify-between mb-4">
      <div>
        {prevWeekId ? (
          <Link
            to={`/chart/${prevWeekId}`}
            className="flex items-center gap-1 text-blue-600 hover:text-blue-800 font-medium transition-colors text-sm sm:text-base"
          >
            <ChevronLeft className="w-5 h-5" />
            <span className="hidden sm:inline">Vorige week</span>
          </Link>
        ) : (
          <span className="flex items-center gap-1 text-gray-300 cursor-not-allowed text-sm sm:text-base">
            <ChevronLeft className="w-5 h-5" />
            <span className="hidden sm:inline">Vorige week</span>
          </span>
        )}
      </div>

      <h1 className="text-lg sm:text-xl font-bold text-gray-900">{formatted}</h1>

      <div>
        {nextWeekId ? (
          <Link
            to={`/chart/${nextWeekId}`}
            className="flex items-center gap-1 text-blue-600 hover:text-blue-800 font-medium transition-colors text-sm sm:text-base"
          >
            <span className="hidden sm:inline">Volgende week</span>
            <ChevronRight className="w-5 h-5" />
          </Link>
        ) : (
          <span className="flex items-center gap-1 text-gray-300 cursor-not-allowed text-sm sm:text-base">
            <span className="hidden sm:inline">Volgende week</span>
            <ChevronRight className="w-5 h-5" />
          </span>
        )}
      </div>
    </div>
  )
}
