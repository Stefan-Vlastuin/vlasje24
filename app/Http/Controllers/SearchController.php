<?php

namespace App\Http\Controllers;

use App\Models\Song;
use App\Models\Artist;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;

class SearchController extends Controller
{
    public function search(Request $request): JsonResponse
    {
        $query = $request->input('q');

        $songResults = Song::where('title', 'LIKE', "%{$query}%")
            ->get()
            ->map(function (Song $song) {
                return [
                    'id' => $song->id,
                    'type' => 'song',
                    'text' => $song->title,
                ];
            });

        $artistResults = Artist::where('name', 'LIKE', "%{$query}%")
            ->get()
            ->map(function (Artist $artist) {
                return [
                    'id' => $artist->id,
                    'type' => 'artist',
                    'text' => $artist->name,
                ];
            });

        $results = $songResults->concat($artistResults)->values();

        return response()->json($results);
    }
}
