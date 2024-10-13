<?php

namespace App\Http\Controllers;

use App\Models\Artist;
use Inertia\Inertia;
use Inertia\Response;

class ArtistController extends Controller
{
    public function show(string $id): Response
    {
        return Inertia::render('Artist', [
           'artist' => Artist::findOrFail($id)
        ]);
    }
}
