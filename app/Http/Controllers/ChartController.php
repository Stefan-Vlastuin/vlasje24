<?php

namespace App\Http\Controllers;

use App\Models\Chart;
use App\Models\Song;
use Inertia\Inertia;
use Inertia\Response;

class ChartController extends Controller
{
    public function show(string $id): Response
    {
        /** @var Chart $chart */
        $chart = Chart::with('songs.artists')->findOrFail($id);

        /** @var Chart|null $previousChart */
        $previousChart = Chart::with('songs')->find(((int) $id) - 1);

        /** @var Chart|null $nextChart */
        $nextChart = Chart::with('songs')->find(((int) $id) + 1);

        $chart->songs->each(function (Song $song) use ($previousChart, $chart) {
            if ($previousChart) {
                $previousSong = $previousChart->songs->firstWhere('id', $song->id);
                $song->position_change = $previousSong
                    ? $previousSong->pivot->order - $song->pivot->order
                    : null;
            } else {
                $song->position_change = null;
            }

            $song->nr_of_weeks = $song->charts()
                ->where('charts.id', '<=', $chart->id)
                ->count();

            // Fix the bug where selecting highest position shows the number of points
            $song->highest_position = $song->charts()
                ->where('charts.id', '<=', $chart->id)
                ->min('chart_song.order');
        });

        return Inertia::render('Chart', [
            'chart' => $chart,
            'previousChartId' => $previousChart ? $previousChart->id : null,
            'nextChartId' => $nextChart ? $nextChart->id : null
        ]);
    }
}
