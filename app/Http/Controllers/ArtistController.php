<?php

namespace App\Http\Controllers;

use App\Models\Artist;
use Inertia\Inertia;
use Inertia\Response;

class ArtistController extends Controller
{
    public function show(string $id): Response
    {
        /** @var Artist $artist */
        $artist = Artist::with('songs.artists')->findOrFail($id);
        return Inertia::render('Artist', [
           'artist' => $artist,
           'songs' => $artist->songs
        ]);
    }
}
