import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts'
import { format } from 'date-fns'
import { nl } from 'date-fns/locale'
import type { ChartHistoryEntryDto } from '../../types/api'

interface Props {
  history: ChartHistoryEntryDto[]
}

export function PositionChart({ history }: Props) {
  const data = history.map(entry => ({
    date: format(new Date(entry.date), 'd MMM', { locale: nl }),
    position: entry.position,
  }))

  return (
    <div className="mt-6">
      <h3 className="text-base font-semibold text-gray-700 mb-4">Positie in de tijd</h3>
      <ResponsiveContainer width="100%" height={250}>
        <LineChart data={data} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
          <XAxis dataKey="date" tick={{ fill: '#6b7280', fontSize: 12 }} />
          <YAxis
            reversed
            domain={[1, 24]}
            ticks={[1, 5, 10, 15, 20, 24]}
            tick={{ fill: '#6b7280', fontSize: 12 }}
            padding={{ top: 10, bottom: 10 }}
          />
          <Tooltip
            contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
            labelStyle={{ color: '#374151', fontWeight: 600 }}
            formatter={(value: number) => [`Positie ${value}`]}
          />
          <Line
            type="monotone"
            dataKey="position"
            stroke="#2563eb"
            strokeWidth={2}
            dot={{ fill: '#2563eb', r: 4 }}
            activeDot={{ r: 6 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}
