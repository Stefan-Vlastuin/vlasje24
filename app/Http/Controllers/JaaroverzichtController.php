<?php

namespace App\Http\Controllers;

use App\Models\Song;
use App\Models\Artist;
use Illuminate\Http\Request;
use Inertia\Inertia;
use Inertia\Response;

class JaaroverzichtController extends Controller
{
    public function index(Request $request): Response
    {
        $year = $request->input('year');
        $rankingType = $request->input('rankingType', 'weeks');

        $songs = Song::with('charts', 'artists')->get();
        $artists = Artist::with('songs.charts')->get();

        $rankedSongs = $this->rankSongs($songs, $rankingType, $year);
        $rankedArtists = $this->rankArtists($artists, $rankingType, $year);

        return Inertia::render('Jaaroverzicht', [
            'rankedSongs' => $rankedSongs,
            'rankedArtists' => $rankedArtists,
            'year' => $year,
            'rankingType' => $rankingType
        ]);
    }

    private function rankSongs($songs, $rankingType, $year)
    {
        return $songs->map(function ($song) use ($rankingType, $year) {
            $song->points = $this->calculatePoints($song, $rankingType, $year);
            return $song;
        })->sortByDesc('points')->values();
    }

    private function rankArtists($artists, $rankingType, $year)
    {
        return $artists->map(function ($artist) use ($rankingType, $year) {
            $artist->points = $artist->songs->sum(function ($song) use ($rankingType, $year) {
                return $this->calculatePoints($song, $rankingType, $year);
            });
            return $artist;
        })->sortByDesc('points')->values();
    }

    private function calculatePoints($song, $rankingType, $year)
    {
        $points = 0;
        foreach ($song->charts as $chart) {
            if ($year && $chart->date->year != $year) {
                continue;
            }
            switch ($rankingType) {
                case 'weeks':
                    $points += 1;
                    break;
                case 'highest':
                    $points = max($points, 25 - $chart->pivot->order);
                    break;
                case 'points':
                    $points += 25 - $chart->pivot->order;
                    break;
            }
        }
        return $points;
    }
}
