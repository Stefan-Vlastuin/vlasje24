<?php

namespace App\Http\Controllers;

use App\Enums\RankingType;
use App\Models\Chart;
use App\Models\Song;
use App\Models\Artist;
use Illuminate\Http\Request;
use Illuminate\Support\Collection;
use Inertia\Inertia;
use Inertia\Response;

class RankingController extends Controller
{
    public function show(string $year, Request $request): Response
    {
        $year = $year === 'all' ? null : (int) $year;

        $years = Chart::all()->map(function (Chart $chart) {
            return $chart->date->year;
        })->unique()->sort()->values();

         if (isset($year) && !$years->contains($year)) {
             $year = null;
         }

        $rankingType = Rankingtype::tryFrom($request->input('rankingType')) ?? RankingType::POINTS;

        $songs = Song::with('charts', 'artists')->get();
        $artists = Artist::with('songs.charts')->get();

        /** @var Song $song */
        foreach ($songs as $song) {
            $song->points = $song->getPoints($year);
            $song->nrOfWeeks = $song->getNrOfWeeks($year);
            $song->highestPosition = $song->getHighestPosition($year);
        }

        /** @var Artist $artist */
        foreach ($artists as $artist) {
            $artist->points = $artist->getPoints($year);
            $artist->nrOfWeeks = $artist->getNrOfWeeks($year);
            $artist->highestPosition = $artist->getHighestPosition($year);
        }

        return Inertia::render('Ranking', [
            'songs' => $songs,
            'artists' => $artists,
            'year' => $year,
            'rankingType' => $rankingType,
            'years' => $years,
        ]);
    }
}
