<?php

namespace App\Http\Controllers;

use App\Models\Song;
use Inertia\Inertia;
use Inertia\Response;

class SongController extends Controller
{
    public function show(string $id): Response
    {
        $song = Song::with('artists', 'charts')->findOrFail($id);
        $chartPositions = $song->charts->map(function ($chart) {
            return [
                'date' => $chart->date->format('Y-m-d'),
                'position' => $chart->pivot->order
            ];
        });

        return Inertia::render('Song', [
            'song' => $song,
            'chartPositions' => $chartPositions
        ]);
    }
}
