import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { api } from '../api/client'
import { ChartHeader } from '../components/chart/ChartHeader'
import { ChartEntryRow } from '../components/chart/ChartEntryRow'
import { ExitedSongs } from '../components/chart/ExitedSongs'

export function HomePage() {
  const { weekId } = useParams<{ weekId: string }>()
  const isLatest = !weekId || weekId === 'latest'

  const { data, isLoading, error } = useQuery({
    queryKey: isLatest ? ['chart', 'latest'] : ['chart', Number(weekId)],
    queryFn: () => isLatest ? api.getLatestChart() : api.getChart(Number(weekId)),
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
      </div>
    )
  }

  if (error || !data) {
    return (
      <div className="text-center py-16 text-red-400">
        Fout bij laden van de chart. Probeer het opnieuw.
      </div>
    )
  }

  return (
    <div>
      <ChartHeader
        date={data.date}
        prevWeekId={data.prevWeekId}
        nextWeekId={data.nextWeekId}
      />

      <div className="space-y-1">
        {data.entries.map(entry => (
          <ChartEntryRow key={entry.song.songId} entry={entry} />
        ))}
      </div>

      <ExitedSongs songs={data.exitedSongs} />
    </div>
  )
}
