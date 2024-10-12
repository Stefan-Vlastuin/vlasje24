<?php

namespace App\Http\Controllers;

use App\Models\Artist;
use Illuminate\Http\JsonResponse;

class ArtistController extends Controller
{
    public function getData(string $id): JsonResponse
    {
        $artist = Artist::with('songs.artists')->findOrFail($id);
        return response()->json($artist);
    }
}
