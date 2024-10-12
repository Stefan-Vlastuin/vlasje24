<?php

namespace App\Http\Controllers;

use App\Models\Song;
use Illuminate\Http\JsonResponse;

class SongController extends Controller
{
    public function getData(string $id): JsonResponse
    {
        $song = Song::with('artists')->findOrFail($id);
        return response()->json($song);
    }
}

