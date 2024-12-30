<?php

namespace App\Http\Controllers;

use App\Models\Chart;
use Inertia\Inertia;
use Inertia\Response;

class ChartController extends Controller
{
    public function show(string $id): Response
    {
        $chart = Chart::with('songs.artists')->findOrFail($id);

        $chart->songs->each(function ($song) use ($chart) {
            $song->weeks_in_chart = $song->charts->count();
            $previousChart = $song->charts->where('date', '<', $chart->date)->sortByDesc('date')->first();
            if ($previousChart) {
                $song->position_change = $previousChart->pivot->order - $song->pivot->order;
            } else {
                $song->position_change = null;
            }
        });

        return Inertia::render('Chart', [
            'chart' => $chart
        ]);
    }
}
