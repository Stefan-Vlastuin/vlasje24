<?php

namespace App\Http\Controllers;

use App\Models\Song;
use App\Models\Artist;
use Illuminate\Http\Request;
use Illuminate\Support\Collection;
use Inertia\Inertia;
use Inertia\Response;

class RankingController extends Controller
{
    public function show(Request $request): Response
    {
        $year = $request->input('year');
        $rankingType = $request->input('rankingType', 'points');

        $songs = Song::with('charts', 'artists')->get();
        $artists = Artist::with('songs.charts')->get();

        $rankedSongs = $this->rankSongs($songs, $rankingType, $year);
        $rankedArtists = $this->rankArtists($artists, $rankingType, $year);

        return Inertia::render('Ranking', [
            'rankedSongs' => $rankedSongs,
            'rankedArtists' => $rankedArtists,
            'year' => $year,
            'rankingType' => $rankingType
        ]);
    }

    /**
     * @param Collection<Song> $songs
     * @param string $rankingType
     * @param ?int $year
     * @return Collection
     */
    private function rankSongs(Collection $songs, string $rankingType, ?int $year)
    {
        return $songs->map(function (Song $song) use ($rankingType, $year) {
            $song->points = $this->calculatePoints($song, $rankingType, $year);
            return $song;
        })->sortByDesc('points')->values();
    }

    /**
     * @param Collection<Artist> $artists
     * @param string $rankingType
     * @param ?int $year
     * @return Collection
     */
    private function rankArtists(Collection $artists, string $rankingType, ?int $year)
    {
        return $artists->map(function ($artist) use ($rankingType, $year) {
            $artist->points = $artist->songs->sum(function (Song $song) use ($rankingType, $year) {
                return $this->calculatePoints($song, $rankingType, $year);
            });
            return $artist;
        })->sortByDesc('points')->values();
    }

    private function calculatePoints(Song $song, string $rankingType, ?int $year): int
    {
        return match ($rankingType) {
            'weeks' => $song->nrOfWeeks,
            'highest' => $song->highestPosition,
            'points' => $song->points,
            default => 0,
        };
    }
}
