<?php

namespace App\Http\Controllers;

use App\Models\Song;
use Inertia\Inertia;
use Inertia\Response;

class SongController extends Controller
{
    public function show(string $id): Response
    {
        return Inertia::render('Song', [
            'song' => Song::with('artists')->findOrFail($id)
        ]);
    }
}

